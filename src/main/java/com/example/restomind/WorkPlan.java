package com.example.restomind;

import java.util.HashMap;
import java.util.Map;

class WorkPlan {
    // String : name of the dish, Integer : the amount of units
    private HashMap<String, Integer> plan;
    private double fitness; // the score of the plan

    public WorkPlan() {
        this.plan = new HashMap<>();
        this.fitness = 0;
    }
    // adds the name and the amount of the dish to our map (build our work plan)
    public void addDishAmount(String dishName, int amount ) {
        plan.put(dishName, amount);
    }

    public void printPlan() {
        System.out.println("all plans :");
        for (Map.Entry<String, Integer> entry : plan.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("---------------------------");
    }

    public HashMap<String, Integer> getPlan() {
        return plan;
    }

    public double getFitness() {
        return fitness;
    }
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}
