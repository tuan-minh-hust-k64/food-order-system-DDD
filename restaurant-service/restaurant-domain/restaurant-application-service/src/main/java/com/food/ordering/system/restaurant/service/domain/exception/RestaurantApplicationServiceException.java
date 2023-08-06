package com.food.ordering.system.restaurant.service.domain.exception;

public class RestaurantApplicationServiceException extends RuntimeException{
    public RestaurantApplicationServiceException(String message) {
        super(message);
    }

    public RestaurantApplicationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
