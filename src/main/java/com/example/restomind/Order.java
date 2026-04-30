package com.example.restomind;

import java.util.HashMap;

public class Order {
    private static int idCounter = 1;
    private int orderId;
    // String: name of dish,Integer: amount of dish
    private HashMap<String, Integer> order;

    public Order() {
        this.orderId = idCounter++;
        this.order = new HashMap<>();
    }

    public void addItem(String dishName, int amount) {
        order.put(dishName, order.getOrDefault(dishName, 0) + amount);
    }

    public int getOrderId() {
        return orderId;
    }

    public HashMap<String, Integer> getOrder() {
        return order;
    }
}
