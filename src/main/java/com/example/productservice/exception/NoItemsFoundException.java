package com.example.productservice.exception;

public class NoItemsFoundException extends RuntimeException{

    public NoItemsFoundException() {
        super("No items found");
    }
}
