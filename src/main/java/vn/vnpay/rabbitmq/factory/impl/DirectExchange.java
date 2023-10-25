package vn.vnpay.rabbitmq.factory.impl;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.rabbitmq.annotation.Autowire;
import vn.vnpay.rabbitmq.annotation.Component;
import vn.vnpay.rabbitmq.annotation.CustomValue;
import vn.vnpay.rabbitmq.common.CommonConstant;
import vn.vnpay.rabbitmq.config.channel.ChannelPool;
import vn.vnpay.rabbitmq.factory.BaseExchange;

import java.util.HashMap;
import java.util.Map;

@Component
public class DirectExchange implements BaseExchange {

    private final Logger logger = LoggerFactory.getLogger(DirectExchange.class);
    @CustomValue("exchange.direct.name")
    private String exchangeDirect;
    @CustomValue("exchange.direct.routingKey")
    private String routingKey;
    @CustomValue("exchange.direct.queueName")
    private String queueName;
    @CustomValue("exchange.dead.letter.name")
    private String deadLetterExchange;
    @CustomValue("exchange.dead.letter.routingKey")
    private String deadLetterRoutingKey;
    @Autowire
    private ChannelPool channelPool;

    @Override
    public void createExchangeAndQueue() {
        Long start = System.currentTimeMillis();
        logger.info("Start createExchangeAndQueue in DirectExchange");
        Channel channel = null;
        try {
            channel = channelPool.getChannel();
            channel.exchangeDeclare(exchangeDirect, BuiltinExchangeType.DIRECT, true);
            channel.queueDeclare(queueName, true, false, false, this.argumentsDeadLetterQueue());
            channel.queueBind(queueName, exchangeDirect, routingKey);
            Long end = System.currentTimeMillis();
            logger.info(" Process createExchangeAndQueue in DirectExchange take {} milliSecond ", (end - start));
        } catch (Exception e) {
            logger.error("CreateExchangeAndQueue in DirectExchange failed with root cause ", e);
        } finally {
            if (channel != null) {
                channelPool.returnChannel(channel);
            }
        }
    }

    private Map<String, Object> argumentsDeadLetterQueue() {
        Map<String, Object> argumentsDeadLetter = new HashMap<>();
        argumentsDeadLetter.put(CommonConstant.X_DEAD_LETTER_EXCHANGE, deadLetterExchange);
        argumentsDeadLetter.put(CommonConstant.X_DEAD_LETTER_ROUTING_KEY, deadLetterRoutingKey);
        return argumentsDeadLetter;
    }

}
