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

        System.out.println(amountPlans + " plans have been created");
        return plans;
    }

    public void fitnessScoreForPlans(List<WorkPlan> plans) {
        for (WorkPlan plan : plans) {
            // creates copy of the inventory
            InventoryManager copyInventory = inventory.createCopy();
            double fitnessScore = 0;


            for (Dish dish : menu) {

                /* takes the from the hash table the amount the plan gave to the dish
                and if the dish wasn't found so the default is 0 (getOrDefault) */
                int plannedAmount = plan.getPlan().getOrDefault(dish.getName(), 0);
                int avgDemand = dish.getavgDemand();
                
                if (copyInventory.canMake(dish, plannedAmount)) {
                    
                    copyInventory.subtractFromInventory(dish, plannedAmount);

                    /*calculates the fitness score by multiplying the amount of sales that is limit is the avgDemand
                    by the price*/
                    int actualAmountOfSales = Math.min(plannedAmount, avgDemand);
                    fitnessScore += (actualAmountOfSales * dish.getPrice());

                    /* if the amount of the plan is bigger than the avg so we will reduce the fitness score
                    take the estimated dishes that would go to waste and multiply it by half of the dish price
                    ( the estimated cost of the dish to the restaurant ) */
                    if (plannedAmount > avgDemand) {
                        fitnessScore -= (plannedAmount - avgDemand) * (dish.getPrice() * 0.5);
                    }
                } else {
                    /* reduce fitness score by a big number
                    because there was not enough ingredients for the plan */
                    fitnessScore -= 500;
                }
            }

            // update fitness score for this specific plan
            plan.setFitness(fitnessScore);
        }
    }
}
