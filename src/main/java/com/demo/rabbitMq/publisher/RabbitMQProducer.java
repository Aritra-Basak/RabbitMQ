package com.demo.rabbitMq.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class used to send/publish message to the Rabbit MQ Broker
 * @author Aritra
 * */
@Service
public class RabbitMQProducer  {

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;


    public void sendMessage(String message){
    LOGGER.info(String.format("Message Sent -> %s",message));
    //using the convertAndSend to send the message to the exchange via the routingKey
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }
}
