package com.demo.rabbitMq.publisher;

import com.demo.rabbitMq.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQJsonProducer {
    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.json.key}")
    private String routingJsonKey;


    @Value("${rabbitmq.exchange.name2}")
    private String secondExchange;

    @Value("${rabbitmq.routing.key2}")
    private String secondRoutingnKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQJsonProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendJsonMessage(User user){
        LOGGER.info(String.format("Json Message sent -> %s",user.toString()));
        rabbitTemplate.convertAndSend(exchange,routingJsonKey,user);
    }

    public void sendJsonMessageToSecondExchange(User user){
        LOGGER.info(String.format("Json Message sent to second exchange -> %s",user.toString()));
        rabbitTemplate.convertAndSend(secondExchange,secondRoutingnKey,user);
    }

    public void sendJsonMessageToMainQueue(User user){
        LOGGER.info(String.format("Json Message sent to main exchange -> %s",user.toString()));
        rabbitTemplate.convertAndSend("main.exchange","main.queue",user);
    }



}
