package com.example.restomind;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Dish implements Serializable{
    private String name;
    private double price;
    // String: name of the ingredient, Double: amount of the ingredient
    private HashMap<String, Double> recipe;

    public Dish(String name, double price) {
        this.name = name;
        this.price = price;
        this.recipe = new HashMap<>();
    }

    // adds ingredient to the hash map recipe
    public void addIngredientToRecipe(String name, double amount) {
        recipe.put(name, amount);
    }

    // calculate how much money the dish cost restaurant
    public double calculateDishIngredientsCost(InventoryManager inventory){
        double cost = 0;
        for (Map.Entry<String,Double> ingredient : recipe.entrySet()){
            Ingredient currentIngredient = inventory.getIngredient(ingredient.getKey());
            double amount = ingredient.getValue();
            cost += currentIngredient.getPricePerUnit() * amount;
        }
        return cost;
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
}
