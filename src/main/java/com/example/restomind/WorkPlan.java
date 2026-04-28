package com.example.restomind;

import java.util.HashMap;
import java.util.Map;

class WorkPlan {
    // String : name of the dish, Integer : the amount of units
    private HashMap<String, Integer> plan;
    // String : name of the dish, Integer : type of action (0: Normal,1: Upsell,2: HappyHour)
    private HashMap<String, Integer> actions;
    private double fitness; // the score of the plan

    public WorkPlan() {
        this.plan = new HashMap<>();
        this.actions = new HashMap<>();
        this.fitness = 0;
    }
    // adds the name and the amount of the dish to our map (build our work plan)
    public void addDishAmount(String dishName, int amount ) {
        plan.put(dishName, amount);
    }

    // adds the name and the type of action for the dist to our map
    public void addDishAction(String dishName, int actionType) {
        actions.put(dishName, actionType);
    }

    public int getAmountForDish(String dishName) {
        return plan.getOrDefault(dishName, 0);
    }

    public int getActionForDish(String dishName) {
        return actions.getOrDefault(dishName, 0);
    }

    public void printPlan(int num) {
        System.out.println("plan number " + num + " :");
        for (Map.Entry<String, Integer> entry : plan.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("---------------------------");
    }

    public HashMap<String, Integer> getPlan() {
        return plan;
    }

    public HashMap<String, Integer> getActions() {
        return actions;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}
