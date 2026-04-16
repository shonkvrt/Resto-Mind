package com.example.restomind;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// implements Serializable to save the object data in a file
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

        /* creates sets (String, Ingredient) of our data and then run through all the
         ingredients and copy them */
        for (Map.Entry<String,Ingredient> ingredient : ingredientsHashMap.entrySet()){
            Ingredient originalIngredient = ingredient.getValue();
            // create a copy of the ingredient
            Ingredient copyIngredient = new Ingredient(
                    originalIngredient.getName(),
                    originalIngredient.getAmout(),
                    originalIngredient.getPricePerUnit(),
                    originalIngredient.getExpirationDate()

            );

            // add the copy ingredient to the copy map
            copy.addIngredient(copyIngredient);
        }
        return copy;
    }

    // checking if there are enough to cook the amount of this dish
    public boolean canMake(Dish dish, int amountToMake){
        for(Map.Entry<String,Double> ingredient : dish.getRecipe().entrySet()){
            double amountIngredientPerDish = ingredient.getValue();
            Ingredient ingredientInInventory = ingredientsHashMap.get(ingredient.getKey());
            if(ingredientInInventory == null || amountIngredientPerDish * amountToMake > ingredientInInventory.getAmout()){
                return false;
            }
        }
        return true;
    }

    // subtract the amount of dishes from inventory
    public void subtractFromInventory(Dish dish, int amountToSold){
        for(Map.Entry<String,Double> ingredient : dish.getRecipe().entrySet()){
            double amountIngredientsNeeded = ingredient.getValue() * amountToSold;
            Ingredient ingredientInInventory = ingredientsHashMap.get(ingredient.getKey());
            if(ingredientInInventory != null){
                ingredientInInventory.setAmout(ingredientInInventory.getAmout() - amountIngredientsNeeded);
            }
        }

    }

    // gets a days limit till expired and return ingredients that will expire before that date
    public List<Ingredient> getExpiringIngredients(int limitDaysTillExpired) {

        List<Ingredient> expiredIngredients = new ArrayList<>();
        // saves the current date and adds the limit days
        LocalDate limitDateTillExpired = LocalDate.now().plusDays(limitDaysTillExpired);

        for (Ingredient ingredient : ingredientsHashMap.values()) {

            /* checks if the date expiration of this specific ingredient is before the limit date
            if it before then we add to the expiredList*/
            if (ingredient.getExpirationDate().isBefore(limitDateTillExpired)) {
                expiredIngredients.add(ingredient);
            }
        }
        return expiredIngredients;
    }
    // remove expired ingredients
    public void removeExpiredIngredients() {
        LocalDate today = LocalDate.now();

        List<String> toRemoveIngredients = new ArrayList<>();


        for (Ingredient ingredient : ingredientsHashMap.values()) {
            if (ingredient.getExpirationDate().isBefore(today)) {
                toRemoveIngredients.add(ingredient.getName());
            }
        }

        for (String name : toRemoveIngredients) {
            ingredientsHashMap.remove(name);
            System.out.println(name + "removed since expired");
        }
    }

    // Serialization
    // save the object data to a file so when you close the program the data won't disappear
    public void saveDataToFile(String fileName) {

        /* FileOutputStream connect to the specific file,
         ObjectOutputStream takes care about transfer objects*/
        try (ObjectOutputStream fileConnection = new ObjectOutputStream(new FileOutputStream(fileName))) {

            // takes the InventoryManager object and writes it into the file
            fileConnection.writeObject(this);
            System.out.println("Inventory saved successfully!");
        } catch (IOException e) {
            System.out.println("Inventory isn't saved, try again");
            e.printStackTrace();
        }
    }
    // Deserialization
    // takes the object data from a specific file and return it
    public static InventoryManager loadDataFromFile(String fileName) {

        try (ObjectInputStream fileConnection = new ObjectInputStream(new FileInputStream(fileName))) {
            return (InventoryManager) fileConnection.readObject();
        }
        /* if the class that had been written in the file doesn't exist the error will be caught
           because it is ClassNotFoundException */
        catch (IOException | ClassNotFoundException e) {
            return new InventoryManager(); // returns empty object so it won't crash later
        }
    }

    public HashMap<String, Ingredient> getIngredientsHashMap() {
        return ingredientsHashMap;
    }
}
