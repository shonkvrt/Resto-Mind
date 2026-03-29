package com.example.restomind;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class InventoryManager implements Serializable{

    private HashMap<String,Ingredient> ingredientsHashMap;

    public InventoryManager(){
        this.ingredientsHashMap = new HashMap<>();
    }

    // add ingredient to the map
    public void addIngredient(Ingredient ingredient){
        ingredientsHashMap.put(ingredient.getName(),ingredient);
    }

    // get Ingredient from the map
    public Ingredient getIngredient(String name){
        return ingredientsHashMap.get(name);
    }

    // copy data for genetic algorithm
    public InventoryManager createCopy(){
        InventoryManager copy = new InventoryManager();
        /* creates sets (String, Ingredient) of our data that we can go through all the
         ingredients and copy them */
        for (Map.Entry<String,Ingredient> ingredient : ingredientsHashMap.entrySet()){
            Ingredient originalIngredient = ingredient.getValue();

            Ingredient copyIngredient = new Ingredient(
                    originalIngredient.getName(),
                    originalIngredient.getAmount(),
                    originalIngredient.getPricePerUnit(),
                    originalIngredient.getExpirationDate()

            );

            copy.addIngredient(copyIngredient);
        }
        return copy;
    }
}
