package com.example.restomind;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OptimizationEngine {
    private InventoryManager inventory;
    private List<Dish> menu;
    private int amountPlans = 100; // amount of random plans

    public OptimizationEngine(InventoryManager inventory, List<Dish> menu) {
        this.inventory = inventory;
        this.menu = menu;
    }

    // the function that will run the optimization
    public void runOptimization() {

    }

    // creates all the initial plans with random amounts to each dish
    public List<WorkPlan> createInitialPlans() {
        List<WorkPlan> plans = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < amountPlans; i++) {
            WorkPlan newPlan = new WorkPlan();

            // for every dish in the menu we will give a random amount
            for (Dish dish : menu) {
                
                // random amount between 0 to the avg demand + 20% to try above the avg
                int maxAmount = (int) (dish.getavgDemand() * 1.2);
                
                // if avg demand is not given
                if (maxAmount <= 0){
                    maxAmount = 10;
                }
                
                int randomAmount = random.nextInt(maxAmount);
                newPlan.addDishAmount(dish.getName(), randomAmount);
            }

            plans.add(newPlan);
        }

        System.out.println(amountPlans + "plans have been created");
        return plans;
    }
}
