package com.example.restomind;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OptimizationEngine {
    private InventoryManager inventory;
    private List<Dish> menu;
    private int amountPlans = 10; // amount of random plans

    public OptimizationEngine(InventoryManager inventory, List<Dish> menu) {
        this.inventory = inventory;
        this.menu = menu;
    }

    // the function that will run the genetic algorithm and give us the best plan
    public WorkPlan runOptimization(int generations) {

        List<WorkPlan> plans = createInitialPlans();

        for (int i = 0; i < generations; i++) {
            fitnessScoreForPlans(plans);
            sortPlans(plans);

            // shows improvement of the results
            if (i % 10 == 0) {
                System.out.println("generation " + i + " score: " + plans.get(0).getFitness());
            }

            List<WorkPlan> theBest = selectTheBest(plans);
            plans = evolve(theBest);
        }

        fitnessScoreForPlans(plans);
        sortPlans(plans);

        return plans.get(0);
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

    // gives fitness score for each plan
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
    // sorting the plans from the highest to the lowest by the fitness score
    public void sortPlans(List<WorkPlan> plans) {
        plans.sort((p1, p2) -> Double.compare(p2.getFitness(), p1.getFitness()));
    }
    // we take the highest plans
    public List<WorkPlan> selectTheBest(List<WorkPlan> plans) {
        return new ArrayList<>(plans.subList(0, amountPlans / 2));
    }
    /* takes the best plans and creates plans evolve with two good plans
    till it back to the original amount of plans*/
    public List<WorkPlan> evolve(List<WorkPlan> bestPlans) {
        List<WorkPlan> nextGeneration = new ArrayList<>();
        Random random = new Random();

        // fill back to 100 plans 
        while (nextGeneration.size() < amountPlans) {

            WorkPlan parent1 = bestPlans.get(random.nextInt(bestPlans.size()));
            WorkPlan parent2 = bestPlans.get(random.nextInt(bestPlans.size()));

            WorkPlan child = crossover(parent1, parent2);


            if (random.nextDouble() < 0.05) {
                Mutation(child);
            }

            nextGeneration.add(child);
        }
        return nextGeneration;
    }

    // crossover two plans to one
    private WorkPlan crossover(WorkPlan p1, WorkPlan p2) {
        WorkPlan child = new WorkPlan();
        Random random = new Random();

        for (Dish dish : menu) {
            String name = dish.getName();
            // choose randomly from which parent to take their amount of this specific dish
            int amount = random.nextBoolean() ? p1.getPlan().get(name) : p2.getPlan().get(name);
            child.addDishAmount(name, amount);
        }
        return child;
    }

    // creates a mutation plan that randomly change the amount ( to try new things )
    private void Mutation(WorkPlan plan) {
        Random random = new Random();
        Dish randomDish = menu.get(random.nextInt(menu.size()));
        int currentAmount = plan.getPlan().get(randomDish.getName());
        // add or subtract from the current amount
        plan.addDishAmount(randomDish.getName(), Math.max(0, currentAmount + random.nextInt(11) - 5));
    }
}
