# RabbitMQ Integrated with Spring Boot Application
This project demonstrates the integration of RabbitMQ with a Spring Boot application. It covers the setup of RabbitMQ using Docker, configuration of exchanges and queues, message publishing and consumption, as well as handling Dead Letter Queues (DLQ).

## Project Overview
1. Pull RabbitMQ Docker Image:
First, pull the RabbitMQ image with the management plugin from Docker Hub using the following command:
### docker pull rabbitmq:3.13.7-management
This command fetches the RabbitMQ image with the version 3.13.7, including the management plugin.

2. Deploy RabbitMQ Using Docker:
Deploy RabbitMQ as a Docker container using the following command:
### docker run --rm -it -p 15672:15672 -p 5672:5672 rabbitmq:3.13.6-management
This command starts a RabbitMQ instance with the management plugin enabled, accessible at http://localhost:15672.

3. RabbitMQ Configuration:
The project includes comprehensive configuration for RabbitMQ, including the setup of exchanges, queues, and their respective bindings. This setup is essential for managing the flow of messages within the application.

4. Message Publisher and Consumer:
The application features both a publisher and a consumer, allowing for the publishing and receiving of messages from the configured queues. This demonstrates basic message handling capabilities in RabbitMQ.

5. Dead Letter Queue (DLQ) Functionality:
The project showcases the automatic handling of messages through a Dead Letter Queue (DLQ) in case of a failure at the consumer end. This is an essential feature for managing failed messages and ensuring message reliability.

6. Manual Retry and DLQ Mechanism:
Additionally, the project demonstrates a manual mechanism for retrying failed messages and pushing them into the DLQ. This allows for more granular control over message processing and error handling.

Getting Started
To run this project, ensure you have Docker installed and use the provided command to start the RabbitMQ container. Also a Spring Boot Application. Follow the project's documentation for more details on setup and usage.

