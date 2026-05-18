package com.example.restomind;

import java.util.HashMap;
import java.util.Map;

class WorkPlan {
    // String : name of the dish, Integer : the amount of units
    private HashMap<String, Integer> plan;
    // String : name of the dish, Integer : type of action (0: Normal,1: Upsell,2: Discount)
    private HashMap<String, Integer> actions;
    // String : name of the dish, Integer : discount percentage (e.g. 20 = 20% off)
    // only when action == 2
    private HashMap<String, Integer> discounts;
    private double fitness; // the score of the plan

    public WorkPlan() {
        this.plan = new HashMap<>();
        this.actions = new HashMap<>();
        this.discounts = new HashMap<>();
        this.fitness = 0;
    }
    // adds the name and the amount of the dish to our map (build our work plan)
    public void addDishAmount(String dishName, int amount ) {
        plan.put(dishName, amount);
    }

    // adds the name and the type of action for the dish to our map
    public void addDishAction(String dishName, int actionType) {
        actions.put(dishName, actionType);
    }

    // sets the discount percentage for a dish (only used when action == 2)
    public void addDishDiscount(String dishName, int discountPercent) {
        discounts.put(dishName, discountPercent);
    }

    public int getAmountForDish(String dishName) {
        return plan.getOrDefault(dishName, 0);
    }

    public int getActionForDish(String dishName) {
        return actions.getOrDefault(dishName, 0);
    }

    // returns discount percentage for a dish, defaults to 0 if not set
    public int getDiscountForDish(String dishName) {
        return discounts.getOrDefault(dishName, 0);
    }

    public HashMap<String, Integer> getPlan() {
        return plan;
    }

    public HashMap<String, Integer> getActions() {
        return actions;
    }

    public HashMap<String, Integer> getDiscounts() {
        return discounts;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}
