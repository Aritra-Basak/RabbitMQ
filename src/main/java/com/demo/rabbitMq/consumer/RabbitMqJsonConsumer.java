package com.demo.rabbitMq.consumer;

import com.demo.rabbitMq.dto.User;
import com.demo.rabbitMq.exception.InvalidJsonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.Channel;


import java.io.IOException;

@Service
public class RabbitMqJsonConsumer {

    @Value("${rabbitmq.queue.name2}")
    private String secondQueue;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MessageConverter messageConverter;

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqJsonConsumer.class);

    @RabbitListener(queues = {"${rabbitmq.queue.json.name}"})
    public void consumeJsonMessage(User user) {
        LOGGER.info(String.format("Json Message Received from Rabbit MQ is : %s",user.toString()));

    }

    @RabbitListener(queues = {"${rabbitmq.queue.name2}"})
    public void recievedMessage(User user) throws InvalidJsonException {
        LOGGER.info("Recieved Message From RabbitMQ: " + user.toString());
        //explicitly throwing an error to put the message in dlq from normal queue.
        //the retrial count and time gap between each retrial is mentioned in the application.properties file
        if (user.getId()< 0) {
            throw new InvalidJsonException("Invalid JSon, User ID cannot be less than 0");
            // System.out.println("RECEIVED MESSAGE WITH ERROR");
        }
    }


    /**
     * Demonstrating the manual retry functionality and pushing the message to DLQ after unsuccessful retry.
     * */
    @RabbitListener(queues = "main.queue", ackMode = "MANUAL")
    public void processMessage(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        boolean ackOrNackSent = false;
        try {
            User user = (User) messageConverter.fromMessage(message);
            LOGGER.info("RECEIVED MESSAGE FROM THE MAIN QUEUE: {}", message);
            if (user.getId() < 0) {
                throw new InvalidJsonException("Invalid JSON, User ID cannot be less than 0");
            }
            // If we reach here, processing succeeded
            LOGGER.info("SUCCESSFULLY CONSUMED MESSAGE: {}",user.toString() );
            channel.basicAck(deliveryTag, false);
            ackOrNackSent = true;
        } catch (InvalidJsonException e) {
            LOGGER.warn("Invalid JSON in message with delivery tag: {}. Error: {}", deliveryTag, e.getMessage());
            handleInvalidMessage(message, channel, deliveryTag);
            ackOrNackSent = true;
        } catch (Exception e) {
            LOGGER.error("Error processing message with delivery tag: {}. Error: {}", deliveryTag, e.getMessage());
            handleFailedProcessing(channel, deliveryTag);
            ackOrNackSent = true;
        } finally {
            //to ensure we always attempt to acknowledge or reject the message.
            if (!ackOrNackSent) {
                try {
                    LOGGER.warn("No ack/nack sent for message with delivery tag: {}. Rejecting message.", deliveryTag);
                    channel.basicReject(deliveryTag, false);
                } catch (Exception e) {
                    LOGGER.error("Failed to reject message with delivery tag: {}. Error: {}", deliveryTag, e.getMessage());
                }
            }
        }
    }

    private void handleInvalidMessage(Message message, Channel channel, long deliveryTag) {
        try {
            Integer retryCount = (Integer) message.getMessageProperties().getHeaders().getOrDefault("x-retry-count", 0);

            if (retryCount < 3) {
                // Increment retry count
                message.getMessageProperties().getHeaders().put("x-retry-count", retryCount + 1);
                // Republish to the retry queue
                rabbitTemplate.send("dlx.exchange", "retry.queue", message);
                LOGGER.info("Message with delivery tag: {} sent to retry queue. Retry count: {}", deliveryTag, retryCount + 1);
            } else {
                // Max retries reached, send to DLQ
                rabbitTemplate.send("dlx.exchange", "dlq.queue", message);
                LOGGER.info("Message with delivery tag: {} sent to DLQ after max retries", deliveryTag);
            }
            // Ack the original message
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            LOGGER.error("Error handling invalid message with delivery tag: {}. Error: {}", deliveryTag, e.getMessage());
            try {
                channel.basicReject(deliveryTag, false);
            } catch (Exception rejectError) {
                LOGGER.error("Failed to reject message after handling error. Delivery tag: {}", deliveryTag);
            }
        }
    }

    private void handleFailedProcessing(Channel channel, long deliveryTag) {
        try {
            // Reject the message and don't requeue
            channel.basicReject(deliveryTag, false);
            LOGGER.info("Rejected message with delivery tag: {} due to processing failure", deliveryTag);
        } catch (Exception e) {
            LOGGER.error("Failed to reject message with delivery tag: {}. Error: {}", deliveryTag, e.getMessage());
        }
    }
}







