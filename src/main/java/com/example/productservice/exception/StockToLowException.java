package com.example.productservice.exception;

public class StockToLowException extends RuntimeException {
    public StockToLowException(Integer id) {
        super("Stock of item with id" + id + " is not enough");
    }
}
