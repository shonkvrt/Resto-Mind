package com.example.restomind;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class OptimizationEngine {
    private InventoryManager inventory;
    // menu helper for calculations
    private List<Dish> menuList;
    private int amountPlans = 100; // amount of random plans
    private String currentDayType = "REGULAR";

    public OptimizationEngine(InventoryManager inventory, Collection<Dish> menuList) {
        this.inventory = inventory;
        this.menuList = new ArrayList<>(menuList);// create the menu list one time
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

        List<Dish> dishesList = new ArrayList<>(inventory.getMenuDishes());

        for (int i = 0; i < amountPlans; i++) {
            WorkPlan newPlan = new WorkPlan();
            int recommendationsCount = 0;
            // shuffles menu
            Collections.shuffle(dishesList);
            // for every dish in the menu we will give a random amount
            for (Dish dish : dishesList) {

                double avgDemand = dish.getDemand(currentDayType, LocalDate.now().getDayOfWeek());
                // random amount limited close to our avgDemand (a little bit above for trying)
                int randomAmount = random.nextInt((int)(avgDemand*1.5)+5);
                newPlan.addDishAmount(dish.getName(), randomAmount);

                // takes the first dishes from the shuffle menu and make them chef recommendation
                if (recommendationsCount < 2) {
                    newPlan.addDishAction(dish.getName(), 1); // Chef's Recommendation[cite: 1, 2]
                    recommendationsCount++;
                } else {
                    // rest of the dishes will be regular or with a discount
                    newPlan.addDishAction(dish.getName(), random.nextBoolean() ? 0 : 2);
                }
            }

            plans.add(newPlan);
        }

        return plans;
    }

    // gives fitness score for each plan
    public void fitnessScoreForPlans(List<WorkPlan> plans) {
        LocalDate businessDate = Calendar.RestaurantClock.getBusinessDate();
        DayOfWeek businessDay = businessDate.getDayOfWeek();

        for (WorkPlan plan : plans) {
            // creates copy of the inventory
            InventoryManager copyInventory = inventory.createCopy();
            double fitnessScore = 0;


            for (Dish dish : inventory.getMenuDishes()) {

                /* takes from the hash table the amount the plan gave to the dish
                and if the dish wasn't found so the default is 0 (getOrDefault) */
                int plannedAmount = plan.getPlan().get(dish.getName());
                int actionType = plan.getActions().get(dish.getName());

                double demand = dish.getDemand(currentDayType,businessDay);
                // the avg demand boost this action gives us
                double demandBoost = dish.getDemandBoost(actionType);

                double dishPrice = dish.getPrice();

                // in discount action we mainly want to focus on expiring food
                if(actionType == 2){
                    dishPrice *= 0.8;
                    double savingPotential = inventory.calculateExpiringValueForDish(dish,2);
                    double totalIngredientsCost = plannedAmount * dish.calculateDishIngredientsCost(inventory);

                    double dynamicBoost = 1.0;
                    if (totalIngredientsCost > 0) {
                        // the ratio between the value of saving and the ingredients cost
                        dynamicBoost = (savingPotential / totalIngredientsCost);
                    }
                    // takes the boost that calculate the profit from the saving potential up to the demand
                    demandBoost = Math.min(demandBoost, dynamicBoost);
                    fitnessScore += savingPotential;

                }
                // the actual demand for dish with type of action and day
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
                    fitnessScore -= 10000;
                }
            }
            // calculate how much money we will lose on expired food in the copy inventory
            double wasteFoodCost = copyInventory.calculateExpiredWasteValue(LocalDate.now().plusDays(1));
            fitnessScore -= wasteFoodCost * 2.0;
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
        int recommendationsCount = 0;

        for (Dish dish : menuList) {
            String dishName = dish.getName();
            int action = random.nextBoolean() ? p1.getActionForDish(dishName) : p2.getActionForDish(dishName);

            if (action == 1) {
                if (recommendationsCount < 2) {
                    recommendationsCount++;
                } else {
                    action = 0;
                }
            }

            /* choose randomly from which parent to take their amount of this specific dish
               and their action */
            child.addDishAmount(dishName, random.nextBoolean() ? p1.getAmountForDish(dishName) : p2.getAmountForDish(dishName));
            child.addDishAction(dishName, action);

        }
        return child;
    }

    // creates a mutation plan that randomly change the amount ( to try new things )
    private void Mutation(WorkPlan plan) {
        Random random = new Random();
        Dish randomDish = menuList.get(random.nextInt(menuList.size()));
        String dishName = randomDish.getName();
        if (random.nextBoolean()) {
            // mutation in the amount of the dish
            int currentAmount = plan.getAmountForDish(dishName);
            plan.addDishAmount(dishName, Math.max(0, currentAmount + random.nextInt(11) - 5));
        } else {
            // mutation in the action to the dish
            int newAction = random.nextInt(3);

            if (newAction == 1) {
                // check how many dishes with chef recommendation
                long count = plan.getActions().values().stream().filter(a -> a == 1).count();
                if (count >= 2) {
                    newAction = random.nextBoolean() ? 0 : 2;
                }
            }
        }
    }

    public void setCurrentDayType(String currentDayType) {
        this.currentDayType = currentDayType;
    }
}
