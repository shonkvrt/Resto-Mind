package com.example.restomind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DayManager {
    private WorkPlan dailyPlan;
    private InventoryManager inventory;
    private String currentHoliday;
    // String: dish name, Integer: prepared dishes stock
    private HashMap<String, Integer> preparedDishesStock = new HashMap<>();
    private List<Order> orders = new ArrayList<>();
    // shows if service as started (waiters starts their shift)
    private boolean serviceStarted = false;
    private List<Order> readyOrders = new ArrayList<>();
    private int kitchenHardDishesLoad = 0;

    public DayManager(InventoryManager inventory) {
        this.inventory = inventory;
        this.currentHoliday = Calendar.getTodayHoliday();
    }

    // read by the cooks at morning
    public void generatePlan() {
        OptimizationEngine engine = new OptimizationEngine(inventory, inventory.getMenuDishes());
        engine.setCurrentDayType(currentHoliday);
        this.dailyPlan = engine.runGeneticAlgorithm(100);
    }

    // read by the waiters for action to provide for the dish
    public String getSuggestedAction(Dish dish) {
        int action = dailyPlan.getActionForDish(dish.getName());
        switch(action) {
            case 1: return "Chef Recommendation";
            case 2: return "Discount";
            default: return "Regular";
        }
    }

    // remove dishes plan from inventory and add them to dishes that are ready
    public void prepareDishesForDay() {
        for (Dish dish : inventory.getMenuDishes()) {
            int amountToPrepare = dailyPlan.getAmountForDish(dish.getName());

            if (inventory.canMake(dish, amountToPrepare)) {
            inventory.subtractFromInventory(dish, amountToPrepare);
            preparedDishesStock.put(dish.getName(), amountToPrepare);
            }
        }
    }

    // process entire order from waiter
    public boolean processOrder(Order order) {
        // checks if there is enough prepared dishes
        for (Map.Entry<String, Integer> dish : order.getOrder().entrySet()) {
            if (preparedDishesStock.get(dish.getKey()) < dish.getValue()) {
                return false; // there is not enough prepared dishes
            }
        }

        // if there is enough then remove from prepared dishes the order
        for (Map.Entry<String, Integer> item : order.getOrder().entrySet()) {
            preparedDishesStock.put(item.getKey(), preparedDishesStock.get(item.getKey()) - item.getValue());

            Dish dish = inventory.getDishByName(item.getKey());
            // update dish sales
            dish.recordSale(item.getValue());
        }

        kitchenHardDishesLoad += order.getHardDishesCount();
        orders.add(order);
        return true;
    }

    // checks if kitchen has a lot of dishes to make
    public boolean checkKitchenLoad(int limitHardDishes,int HardDishesInOrder) {
        return kitchenHardDishesLoad + HardDishesInOrder > limitHardDishes;
    }

    // update demand avg and action demand boost at the end of the day
    public void closeDay() {
        for (Dish dish : inventory.getMenuDishes()) {
            int sales = dish.getAndResetDailySales();
            int action = dailyPlan.getActionForDish(dish.getName());

            // update demands data
            dish.updateDemandDayType(currentHoliday, Calendar.RestaurantClock.getBusinessDate().getDayOfWeek(), sales);
            dish.updateActionDemandBoost(action, sales, Calendar.RestaurantClock.getBusinessDate().getDayOfWeek(), currentHoliday);
        }
        inventory.removeExpiredIngredients();
        inventory.saveDataToFile("restaurant_data.dat");
    }

    public void markOrderAsReady(Order order) {
        readyOrders.add(order);
    }

    public List<Order> getReadyOrders() {
        return readyOrders;
    }

    public void deliverOrder(int orderId) {
        readyOrders.removeIf(o -> o.getOrderId() == orderId);
    }

    public void completeOrder(Order order) {
        if (order == null) return;
        // substruct from the hard dishes the ready order
        kitchenHardDishesLoad -= order.getHardDishesCount();
        // remove order from orders
        orders.remove(order);
        // add the order to the ready order
        readyOrders.add(order);
    }

    public void setServiceStarted(boolean status) {
        this.serviceStarted = status;
    }

    public boolean isServiceStarted() {
        return serviceStarted;
    }

    public WorkPlan getDailyPlan() {
        return dailyPlan;
    }

    public InventoryManager getInventory() {
        return inventory;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public int getPreparedStock(Dish dish) {
        return preparedDishesStock.getOrDefault(dish.getName(), 0);
    }

    public boolean hasOrders() {
        return !orders.isEmpty();
    }
}
