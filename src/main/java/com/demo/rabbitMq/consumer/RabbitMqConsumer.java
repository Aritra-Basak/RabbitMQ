package com.demo.rabbitMq.consumer;

import com.demo.rabbitMq.dto.User;
import com.demo.rabbitMq.exception.InvalidJsonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class RabbitMqConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqConsumer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.dlq.exchange.name}")
    private String dlqExchange;

    @Value("${rabbitmq.dlq.routing.key}")
    private String dlqRoutingnKey;


    @RabbitListener(queues = {"${rabbitmq.queue.name}"})
    public void consume(String message){
        LOGGER.info(String.format("Message Received from Rabbit MQ is : %s",message));
    }

    @RabbitListener(queues = {"second.main.queue"})
    @Retryable(
            value = {InvalidJsonException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000)
    )
    public void receivedQueueMessage(String message) throws InvalidJsonException {
        LOGGER.info("2nd TIME RECEIVED MESSAGE: {}",message);
        if (message==null|| message.isEmpty()) {
            throw new InvalidJsonException("Invalid Message Sent.");
        }
        // Processing the message successfully.....
        System.out.println("Processed message: " + message);
    }

    @Recover
    public void recover(InvalidJsonException e, String message) {
        // Logic to handle the failure after max retries
        System.out.println("Message moved to DLQ after retries: " + message);
       rabbitTemplate.convertAndSend(dlqExchange, dlqRoutingnKey, message);
    }


}
