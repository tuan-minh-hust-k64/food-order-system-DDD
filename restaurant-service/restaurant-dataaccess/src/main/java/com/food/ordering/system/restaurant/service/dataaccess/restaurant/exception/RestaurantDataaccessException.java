package com.food.ordering.system.restaurant.service.dataaccess.restaurant.exception;

public class RestaurantDataaccessException extends RuntimeException{
    public RestaurantDataaccessException() {
        super();
    }

    public RestaurantDataaccessException(String message) {
        super(message);
    }

    public RestaurantDataaccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
