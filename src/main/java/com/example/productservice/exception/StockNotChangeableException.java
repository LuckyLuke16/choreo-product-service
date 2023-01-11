package com.example.productservice.exception;

public class StockNotChangeableException extends RuntimeException {

    public StockNotChangeableException() {super("Item stock could not be changed");}
}
