package com.example.restomind;

import java.util.HashMap;

public class Order {
    private static int idCounter = 1;
    private int orderId;
    // String: name of dish,Integer: amount of dish
    private HashMap<String, Integer> order;
    private int hardDishesCount;


    public Order() {
        this.orderId = idCounter++;
        this.order = new HashMap<>();
        this.hardDishesCount = 0;
    }

    public void addItem(Dish dish) {
        order.put(dish.getName(), order.getOrDefault(dish.getName(), 0) + 1);
        if (dish.isHardToMake()) {
            hardDishesCount++;
        }
    }

    // removes a dish from the order
    public void removeItem(Dish dish) {
        if (order.containsKey(dish.getName())) {
            int currentAmount = order.get(dish.getName());
            if (currentAmount > 0) {
                order.put(dish.getName(), currentAmount - 1);
                if (dish.isHardToMake()) {
                    hardDishesCount--;
                }
            }
            // if dish amount in order is 0, then remove dish from order
            if (order.get(dish.getName()) == 0) {
                order.remove(dish.getName());
            }
        }
    }

    // remove entire dish with an X
    public void removeAllOfDish(Dish dish) {
        if (order.containsKey(dish.getName())) {
            int amount = order.get(dish.getName());
            if (dish.isHardToMake()) {
                hardDishesCount -= amount;
            }
            order.remove(dish.getName());
        }
    }

    public int getOrderId() {
        return orderId;
    }

    public HashMap<String, Integer> getOrder() {
        return order;
    }

    public int getHardDishesCount() {
        return hardDishesCount;
    }
}
