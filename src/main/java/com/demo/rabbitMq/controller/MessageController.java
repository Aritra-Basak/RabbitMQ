package com.demo.rabbitMq.controller;

import com.demo.rabbitMq.dto.User;
import com.demo.rabbitMq.publisher.RabbitMQJsonProducer;
import com.demo.rabbitMq.publisher.RabbitMQProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class MessageController {

    @Autowired
    private RabbitMQProducer rabbitMQProducer;

    @Autowired
    private RabbitMQJsonProducer rabbitMQJsonProducer;

    @GetMapping("/publish")
    public ResponseEntity<String> sendMessage(@RequestParam("message") String message){
        try{
            rabbitMQProducer.sendMessage(message);
            return ResponseEntity.ok("Success: Message Sent to Rabbit MQ Broker.");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Failure:"+e.getMessage());
        }
    }

    @PostMapping("/publish/json")
    public ResponseEntity<String>sendJsonMessage(@RequestBody User user){
        try{
            rabbitMQJsonProducer.sendJsonMessage(user);
            return ResponseEntity.ok("Success: Json Message Sent to Rabbit MQ Broker.");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Failure:"+e.getMessage());
        }
    }

    @PostMapping("/publish/dlq/json")
    public ResponseEntity<String>sendJsonDlqMessage(@RequestBody User user){
        try{
            rabbitMQJsonProducer.sendJsonMessageToSecondExchange(user);
            return ResponseEntity.ok("Success: Json Message Sent to Second Rabbit MQ Broker.");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Failure:"+e.getMessage());
        }
    }

    @PostMapping("/publish/main/json")
    public ResponseEntity<String>sendJsonMainMessage(@RequestBody User user){
        try{
            rabbitMQJsonProducer.sendJsonMessageToMainQueue(user);
            return ResponseEntity.ok("Success: Json Message Sent to Second Rabbit MQ Broker.");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Failure:"+e.getMessage());
        }
    }

    @GetMapping("/publish/main/message")
    public ResponseEntity<String>sendMainMessage(@RequestParam("message") String message){
        try{
            rabbitMQProducer.sendMessageSecondQueue(message);
            return ResponseEntity.ok("Success: Json Message Sent to Second Rabbit MQ Broker.");
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok("Failure:"+e.getMessage());
        }
    }
}
