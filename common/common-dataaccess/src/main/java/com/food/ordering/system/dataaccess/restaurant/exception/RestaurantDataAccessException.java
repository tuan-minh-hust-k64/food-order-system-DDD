package com.food.ordering.system.dataaccess.restaurant.exception;

public class RestaurantDataAccessException extends RuntimeException{
    public RestaurantDataAccessException() {
    }

    public RestaurantDataAccessException(String message) {
        super(message);
    }

    public RestaurantDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
