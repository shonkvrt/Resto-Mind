package com.example.restomind;

import java.util.HashMap;

public class DayManager {
    private WorkPlan dailyPlan;
    private InventoryManager inventory;
    private String currentHoliday;
    // String: dish name, Integer: prepared dishes stock
    private HashMap<String, Integer> preparedDishesStock = new HashMap<>();

    public DayManager(InventoryManager inventory) {
        this.inventory = inventory;
        this.currentHoliday = Calendar.getTodayHoliday();
    }

    // read by the cooks at morning
    public void generatePlan() {
        OptimizationEngine engine = new OptimizationEngine(inventory, inventory.getMenu());
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
        for (Dish dish : inventory.getMenu()) {
            int amountToPrepare = dailyPlan.getAmountForDish(dish.getName());

            inventory.subtractFromInventory(dish, amountToPrepare);
            preparedDishesStock.put(dish.getName(), amountToPrepare);

        }
    }

    // return true if prepared dish stock isn't over yet else false
    public boolean executeSale(Dish dish) {
        int countDish = preparedDishesStock.get(dish.getName());

        if (countDish > 0) {
            // takes from the prepared dish stock 1 dish
            preparedDishesStock.put(dish.getName(), countDish - 1);
            dish.recordSale(); // for updates
            return true;
        } else {
            return false;
        }
    }

    // update demand avg and action demand boost at the end of the day
    public void closeDay() {
        for (Dish dish : inventory.getMenu()) {
            int sales = dish.getAndResetDailySales();
            int action = dailyPlan.getActionForDish(dish.getName());

            // update demands data
            dish.updateDemandDayType(currentHoliday, Calendar.RestaurantClock.getBusinessDate().getDayOfWeek(), sales);
            dish.updateActionDemandBoost(action, sales, Calendar.RestaurantClock.getBusinessDate().getDayOfWeek(), currentHoliday);
        }
        inventory.removeExpiredIngredients();
        inventory.saveDataToFile("restaurant_data.dat");
    }

    public WorkPlan getDailyPlan() { return dailyPlan; }
    public InventoryManager getInventory() { return inventory; }
}
