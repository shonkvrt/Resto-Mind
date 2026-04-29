package com.example.restomind;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OptimizationEngine {
    private InventoryManager inventory;
    private List<Dish> menu;
    private int amountPlans = 100; // amount of random plans
    private String currentDayType = "REGULAR";

    public OptimizationEngine(InventoryManager inventory, List<Dish> menu) {
        this.inventory = inventory;
        this.menu = menu;
    }

    // the function that will run the genetic algorithm and give us the best plan
    public WorkPlan runGeneticAlgorithm(int generations) {

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

                double avgDemand = dish.getDemand(currentDayType, LocalDate.now().getDayOfWeek());
                // random amount limited close to our avgDemand (a little bit above for trying)
                int randomAmount = random.nextInt((int)(avgDemand*1.5)+5);
                newPlan.addDishAmount(dish.getName(), randomAmount);
                newPlan.addDishAction(dish.getName(), random.nextInt(3));
            }

            plans.add(newPlan);
        }

        return plans;
    }

    // gives fitness score for each plan
    public void fitnessScoreForPlans(List<WorkPlan> plans) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        for (WorkPlan plan : plans) {
            // creates copy of the inventory
            InventoryManager copyInventory = inventory.createCopy();
            double fitnessScore = 0;


            for (Dish dish : menu) {

                /* takes from the hash table the amount the plan gave to the dish
                and if the dish wasn't found so the default is 0 (getOrDefault) */
                int plannedAmount = plan.getPlan().get(dish.getName());
                int actionType = plan.getActions().get(dish.getName());

                double demand = dish.getDemand(currentDayType,today);
                // the avg demand boost this action gives us
                double demandBoost = dish.getDemandBoost(actionType);

                double dishPrice = dish.getPrice();
                if(actionType == 2){
                    dishPrice *= 0.8;
                }

                demand *= demandBoost;

                if (copyInventory.canMake(dish, plannedAmount)) {
                    
                    copyInventory.subtractFromInventory(dish, plannedAmount);

                    /*calculates the fitness score by multiplying the amount of sales that is limit is the avgDemand
                    by the price*/
                    int actualAmountOfSales = (int) Math.min(plannedAmount, demand);
                    double profit = (actualAmountOfSales * dishPrice);
                    double ingredientsCost = plannedAmount * dish.calculateDishIngredientsCost(inventory);

                    fitnessScore += (profit - ingredientsCost);
                } else {
                    /* reduce fitness score by a big number
                    because there was not enough ingredients for the plan */
                    fitnessScore -= 500;
                }
            }
            // calculate how much money we will lose on expired food in the copy inventory
            double wasteFoodCost = copyInventory.calculateExpiredWasteValue();
            fitnessScore -= wasteFoodCost * 1.5;
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

        // elitism, keep the 5 best plans with no change
        for (int i = 0; i < 5; i++) {
            nextGeneration.add(bestPlans.get(i));
        }

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
            String dishName = dish.getName();
            /* choose randomly from which parent to take their amount of this specific dish
               and their action */
            if (random.nextBoolean()) {
                child.addDishAmount(dishName, p1.getAmountForDish(dishName));
                child.addDishAction(dishName, p1.getActionForDish(dishName));
            } else {
                child.addDishAmount(dishName, p2.getAmountForDish(dishName));
                child.addDishAction(dishName, p2.getActionForDish(dishName));
            }
        }
        return child;
    }

    // creates a mutation plan that randomly change the amount ( to try new things )
    private void Mutation(WorkPlan plan) {
        Random random = new Random();
        Dish randomDish = menu.get(random.nextInt(menu.size()));
        String dishName = randomDish.getName();
        if (random.nextBoolean()) {
            // mutation in the amount of the dish
            int currentAmount = plan.getAmountForDish(dishName);
            plan.addDishAmount(dishName, Math.max(0, currentAmount + random.nextInt(11) - 5));
        } else {
            // mutation in the action to the dish
            plan.addDishAction(dishName, random.nextInt(3));
        }
    }

    public void setCurrentDayType(String currentDayType) {
        this.currentDayType = currentDayType;
    }
}
