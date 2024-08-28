package com.demo.rabbitMq.exception;

public class InvalidJsonException extends Exception{
    private static final long serialVersionUID = -3154618962130084535L;
    public InvalidJsonException() {
        super();
    }
    public InvalidJsonException(String message) {
        super(message);
    }
}
