package com.example.restomind;

import java.io.Serializable;
// enum for days in the week
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

public class Dish implements Serializable{
    private static final long serialVersionUID = 1L;
    private String name;
    private double price;
    // String: name of the ingredient, Double: amount of the ingredient
    private HashMap<String, Double> recipe;

    // object for the demand data
    private static class DemandData implements Serializable {
        double avgDemand;
        int count;

        public DemandData(double avgDemand, int count) {
            this.avgDemand = avgDemand;
            this.count = count;
        }
    }

    // object for efficiency action data
    private static class ActionData implements Serializable {
        double demandBoost;
        int count;

        public ActionData(double demandBoost, int count) {
            this.demandBoost = demandBoost;
            this.count = count;
        }
    }
    /* String: the day type (regular, passover), DayOfWeek : the day in the week
       DemandData: the count and the avg demand for every type day*/
    private HashMap<String, HashMap<DayOfWeek, DemandData>> demandDayType;
    /* Integer: type of action (0: Normal,1: Upsell,2: HappyHour)
       ActionData: the count and the demand boost (how much bigger demand than normal)*/
    private HashMap<Integer, ActionData> actionDemandBoost;


    public Dish(String name, double price, int initialDemand) {
        this.name = name;
        this.price = price;
        this.recipe = new HashMap<>();
        this.demandDayType = new HashMap<>();
        this.actionDemandBoost = new HashMap<>();

        // initial the avg demand for no holiday days of the week
        HashMap<DayOfWeek, DemandData> regularDemands = new HashMap<>();

        for (DayOfWeek day : DayOfWeek.values()) {
            regularDemands.put(day, new DemandData(initialDemand, 1));
        }

        demandDayType.put("REGULAR", regularDemands);

        // default actions
        actionDemandBoost.put(0, new ActionData(1.0,1)); // Regular
        actionDemandBoost.put(1, new ActionData(1.2, 1)); // Upsell
        actionDemandBoost.put(2, new ActionData(1.5, 1)); // Happy Hour
    }

    // adds ingredient to the hash map recipe
    public void addIngredientToRecipe(String name, double amount) {
        recipe.put(name, amount);
    }

    // update avg demand in end of a type of day
    public void updateDemandDayType(String holidayName, DayOfWeek day, int amountSales) {

        // if holiday isn't exist then create holiday with initial regular data
        if (!demandDayType.containsKey(holidayName)) {
            createNewHoliday(holidayName);
        }

        double currentDemand = getDemand(holidayName,day);
        int count = getdayTypeCount(holidayName,day);

        // adds the new day data to the avg demand of the day
        double newDemand = ((currentDemand * count) + amountSales) / (count + 1);

        demandDayType.get(holidayName).put(day, new DemandData(newDemand,count + 1));
    }

    public void updateActionDemandBoost(int actionType, int amountSales, DayOfWeek day, String holidayName) {
        if (actionType == 0) return; // normal action doesn't change is efficiency (in relation to normal)

        double demand = getDemand(holidayName, day);
        if (demand <= 0) return;

        // checks the demand boost of action type
        double demandBoostToday = (double) amountSales / demand;

        double currentDemandBoost = getDemandBoost(actionType);
        int count = getActionCount(actionType);

        // update avg demand boost for action type
        double newEfficiency = ((currentDemandBoost * count) + demandBoostToday) / (count + 1);

        actionDemandBoost.put(actionType, new ActionData(newEfficiency,count + 1));
    }

    // creates new holiday with initial data as regular days
    private void createNewHoliday(String holidayName) {
        HashMap<DayOfWeek, DemandData> newDemands = new HashMap<>();

        for (DayOfWeek day : DayOfWeek.values()) {
            newDemands.put(day, new DemandData(getDemand("REGULAR",day),1));
        }

        demandDayType.put(holidayName, newDemands);
    }

    // calculate how much money the dish cost restaurant
    public double calculateDishIngredientsCost(InventoryManager inventory){
        double cost = 0;
        for (Map.Entry<String,Double> ingredient : recipe.entrySet()){
            Ingredient currentIngredient = inventory.getFirstBatch(ingredient.getKey());
            cost += currentIngredient.getPricePerUnit() * ingredient.getValue();
        }
        return cost;
    }

    /* gets the demand of specific day in a specific holiday and
       if holiday isn't exist or doesn't have any data to specific day then
       use the regular day */
    public double getDemand(String holidayName, DayOfWeek day) {
        // if holiday isn't exist, then use regular data
        if(demandDayType.get(holidayName) == null){
            return demandDayType.get("REGULAR").get(day).avgDemand;
        }
        return demandDayType.get(holidayName).get(day).avgDemand;
    }

    public int getdayTypeCount(String holidayName, DayOfWeek day) {
        if(demandDayType.get(holidayName) == null){
            return demandDayType.get("REGULAR").get(day).count;
        }
        return demandDayType.get(holidayName).get(day).count;
    }

    public double getDemandBoost(int actionType){
        return actionDemandBoost.get(actionType).demandBoost;
    }

    public int getActionCount(int actionType){
        return actionDemandBoost.get(actionType).count;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public HashMap<String, Double> getRecipe() {
        return recipe;
    }

    public HashMap<String, HashMap<DayOfWeek, DemandData>> getDemandDayType() {
        return demandDayType;
    }

    public HashMap<Integer, ActionData> getActionDemandBoost() {
        return actionDemandBoost;
    }
}
