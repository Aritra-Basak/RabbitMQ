package com.demo.rabbitMq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Configuration class to configure Rabbit MQ Broker.
 * @author Aritra
 * */
@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.queue.name}")
    private String queue;

    @Value("${rabbitmq.queue.json.name}")
    private String jsonQueue;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Value("${rabbitmq.routing.json.key}")
    private String routingJsonKey;

    @Value("${rabbitmq.exchange.name2}")
    private String secondExchange;

    @Value("${rabbitmq.routing.key2}")
    private String secondRoutingnKey;

    @Value("${rabbitmq.queue.name2}")
    private String secondQueue;

    @Value("${rabbitmq.dlq.exchange.name}")
    private String dlqExchange;

    @Value("${rabbitmq.dlq.routing.key}")
    private String dlqRoutingnKey;

    @Value("${rabbitmq.dlq.queue.name}")
    private String dlqQueue;


    @Value("${rabbitmq.main.queue}")
    private String mainQueue;

    @Value("${rabbitmq.main.exchange}")
    private String mainExchange;

    @Value("${rabbitmq.main.key}")
    private String mainRoutingKey;

    @Value("${rabbitmq.queue.retry}")
    private String retryQueue;

    @Value("${rabbitmq.exchange.retry}")
    private String retryExchange;

    @Value("${rabbitmq.routing.key.retry}")
    private String retryRoutingKey;



    /*
    * Spring Bean to configure the Rabbit MQ queue
    * */
    @Bean
    public Queue queue(){
        return new Queue(queue);
    }

    /*
     * Spring Bean to configure the Rabbit MQ queue to store json Messages
     * */
    @Bean
    public Queue jsonQueue(){
        return new Queue(jsonQueue);
    }

    /*
     * Spring Bean to configure the Rabbit MQ exchange
     *  A TopicExchange routes messages to one or more queues based on a pattern matching between the routing key of the message and the binding key of the queue.
     * */
    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(exchange);
    }

    /*
     * Spring Bean to configure the Rabbit MQ binding process of exchange with the respective Queue using Routing Key.
     * */
    @Bean
    public Binding binding(){
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(routingKey);
    }

    @Bean
    public Binding jsonBinding(){
        return BindingBuilder
                .bind(jsonQueue())
                .to(exchange())
                .with(routingJsonKey);
    }

    /**
     *A DirectExchange routes messages to queues based on an exact match between the message's routing key and the queue's binding key.
     Best for scenarios where you have a clear and direct mapping between routing keys and queues.     *
     *
     * */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlqExchange);
    }

    @Bean
    public Queue dlq() {
        return QueueBuilder.durable(dlqQueue).build();
    }
    @Bean
    public Binding DLQbinding() {
        return BindingBuilder
                .bind(dlq())
                .to(deadLetterExchange())
                .with(dlqRoutingnKey);
    }

    @Bean
    public DirectExchange seocndExchange() {
        return new DirectExchange(secondExchange);
    }

    //The durable() method specifies that the queue should be durable, meaning it will survive a broker restart. A durable queue is persisted to disk, ensuring messages are not lost even if RabbitMQ restarts.
    //.withArgument("x-dead-letter-exchange", dlqExchange): This method adds a custom argument to the queue. In this case, the argument is "x-dead-letter-exchange", which specifies the name of the dead-letter exchange (DLX).
    //.withArgument("x-dead-letter-routing-key", dlqRoutingKey):This line adds another custom argument, "x-dead-letter-routing-key", specifying the routing key used when the message is sent to the dead-letter exchange.
    @Bean
    public Queue secondQueue() {
        return QueueBuilder
                .durable(secondQueue)
                .withArgument("x-dead-letter-exchange", dlqExchange)
                .withArgument("x-dead-letter-routing-key", dlqRoutingnKey).build();
    }

    @Bean
    public Binding secondBinding() {
        return BindingBuilder.bind(secondQueue()).to(seocndExchange()).with(secondRoutingnKey);
    }


    @Bean
    public MessageConverter converter(){
        return new Jackson2JsonMessageConverter();
    }

    /*
    * In behind RabbitTemplate is being manipulated to convert java object to Json format and vice versa.
    * */
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

///********************************** Retry Mechanism *****************************************************
    //2 exchanges -- 3 queues
// Main queue
@Bean
public Queue mainQueue() {
    return QueueBuilder.durable(mainQueue)
            .withArgument("x-dead-letter-exchange", dlqExchange)
            .withArgument("x-dead-letter-routing-key", retryRoutingKey)
            .build();
}

// Retry queue
@Bean
public Queue retryQueue() {
    return QueueBuilder.durable(retryQueue)
            .withArgument("x-dead-letter-exchange", mainExchange)  // Route back to main exchange
            .withArgument("x-dead-letter-routing-key", mainRoutingKey)  // Route back to main queue
            .withArgument("x-message-ttl", 5000)  // TTL for retry (in milliseconds)
            .build();
}

// Dead Letter Queue (DLQ)
@Bean
public Queue dlqQueue() {
    return QueueBuilder.durable(dlqQueue).build();
}

// Main exchange
@Bean
public DirectExchange mainExchange() {
    return new DirectExchange(mainExchange);
}

// Dead letter exchange (DLX)
@Bean
public DirectExchange dlxExchange() {
    return new DirectExchange(dlqExchange);
}

// Bind main queue to main exchange
@Bean
public Binding mainBinding() {
    return BindingBuilder.bind(mainQueue()).to(mainExchange()).with(mainRoutingKey);
}

// Bind retry queue to dead letter exchange (DLX)
@Bean
public Binding retryBinding() {
    return BindingBuilder.bind(retryQueue()).to(dlxExchange()).with(retryRoutingKey);
}

// Bind DLQ to dead letter exchange (DLX)
@Bean
public Binding dlqBinding() {
    return BindingBuilder.bind(dlqQueue()).to(dlxExchange()).with(dlqRoutingnKey);
}

}
