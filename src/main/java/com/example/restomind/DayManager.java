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
            case 1: return "Upsell";
            case 2: return "Happy Hour";
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

            // גישה ישירה למנה בלי לולאת חיפוש![cite: 9, 14]
            Dish dish = inventory.getDishByName(item.getKey());
            // update dish sales
            dish.recordSale(item.getValue());
        }

        // הוספה לתור של הטבחים
        orders.add(order);
        return true;
    }

    // return true if prepared dish stock isn't over yet else false
    public boolean executeSale(Dish dish) {
        int countDish = preparedDishesStock.getOrDefault(dish.getName(), 0);

        if (countDish > 0) {
            // takes from the prepared dish stock 1 dish
            preparedDishesStock.put(dish.getName(), countDish - 1);
            dish.recordSale(1); // for updates
            return true;
        } else {
            return false;
        }
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

    public void completeOrder(int orderId) {
        orders.removeIf(o -> o.getOrderId() == orderId);
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
}
