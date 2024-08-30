package com.demo.rabbitMq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class RabbitMqApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitMqApplication.class, args);
	}

	//docker command to run the rabbitmq 3.13.6-management version.
	//docker run --rm -it -p 15672:15672 -p 5672:5672 rabbitmq:3.13.6-management
}
