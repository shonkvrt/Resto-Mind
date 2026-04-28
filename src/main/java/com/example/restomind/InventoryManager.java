package com.example.restomind;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// implements Serializable to save the object data in a file
public class InventoryManager implements Serializable{
    /* puts specific version to the object
    so if there is any changes so the version will stay the same while doing deserialization */
    private static final long serialVersionUID = 1L;
    private HashMap<String,List<Ingredient>> ingredients;
    private List<Dish> menu;

    public InventoryManager(){
        this.ingredients = new HashMap<>();
        this.menu = new ArrayList<>();
    }

    // add ingredient to the map (also based on date expiration)
    public void addIngredient(Ingredient ingredient){
        // if there is no kind of this ingredient then create new array and add it to the map
        if (!ingredients.containsKey(ingredient.getName())) {
            ingredients.put(ingredient.getName(), new ArrayList<>());
        }
        // add the ingredient to the list
        ingredients.get(ingredient.getName()).add(ingredient);
    }

    public void addDish(Dish dish){
        menu.add(dish);
    }

    // copy data for genetic algorithm
    public InventoryManager createCopy(){

        InventoryManager copy = new InventoryManager();

        /* creates sets (String, List<Ingredient>) of our data and then run through all the
         ingredients and copy them */
        for (Map.Entry<String,List<Ingredient>> ingredient : ingredients.entrySet()){
            List<Ingredient> copyList = new ArrayList<>();
            // create a copy of the ingredient
            for (Ingredient original : ingredient.getValue()) {
                copyList.add(new Ingredient(
                        original.getName(),
                        original.getAmount(),
                        original.getPricePerUnit(),
                        original.getExpirationDate()
                ));
            }

            // add the copy ingredient to the copy map
            copy.ingredients.put(ingredient.getKey(), copyList);
        }

        for (Dish dish : menu) {
            copy.addDish(dish);
        }

        return copy;
    }

    // checking if there are enough to cook the amount of this dish
    public boolean canMake(Dish dish, int amountToMake){

        for(Map.Entry<String,Double> ingredient : dish.getRecipe().entrySet()){

            double amountNeeded = ingredient.getValue();
            List<Ingredient> stockBatches = ingredients.get(ingredient.getKey());

            if(stockBatches == null){
                return false;
            }

            //sum all the specific ingredient
            double totalAvailable = 0;
            for (Ingredient ing : stockBatches) {
                totalAvailable += ing.getAmount();
            }

            if(totalAvailable < amountNeeded * amountToMake){
                return false;
            }
        }
        return true;
    }

    // subtract the amount of dishes from inventory
    public void subtractFromInventory(Dish dish, int amountToSold){

        for(Map.Entry<String,Double> ingredient : dish.getRecipe().entrySet()){

            double amountNeeded = ingredient.getValue() * amountToSold;
            List<Ingredient> stockBatches = ingredients.get(ingredient.getKey());

            if (stockBatches != null) {
                // sort by expiration date (the close one is the first)
                stockBatches.sort((a, b) -> a.getExpirationDate().compareTo(b.getExpirationDate()));

                for (Ingredient batch : stockBatches) {
                    if (amountNeeded <= 0) break;

                    double amountInBatch = batch.getAmount();
                    double amountTaken = Math.min(amountInBatch, amountNeeded);

                    batch.setAmount(amountInBatch - amountTaken);
                    amountNeeded -= amountTaken;
                }
                // clean batches that are empty
                stockBatches.removeIf(batch -> batch.getAmount() <= 0);
            }
        }

    }

    // calculate how much money we lost from expired waste food
    public double calculateExpiredWasteValue() {
        double totalWasteValue = 0;
        LocalDate today = LocalDate.now();

        for (List<Ingredient> batchList : ingredients.values()) {
            for (Ingredient ingredient : batchList){
                // if the ingredient is expired
                if (ingredient.getExpirationDate().isBefore(today.plusDays(1))) {
                    totalWasteValue += (ingredient.getAmount() * ingredient.getPricePerUnit());
                }
            }
        }
        return totalWasteValue;
    }

    // gets a days limit till expired and return ingredients that will expire before that date
    public List<Ingredient> getExpiringIngredients(int limitDaysTillExpired) {

        List<Ingredient> expiredIngredients = new ArrayList<>();

        // saves the current date and adds the limit days
        LocalDate limitDateTillExpired = LocalDate.now().plusDays(limitDaysTillExpired);

        for (List<Ingredient> batchList : ingredients.values()) {
            for (Ingredient ingredient : batchList){
                /* checks if the date expiration of this specific ingredient is before the limit date
                if it before then we add to the expiredList*/
                if (ingredient.getExpirationDate().isBefore(limitDateTillExpired)) {
                    expiredIngredients.add(ingredient);
                }
            }
        }
        return expiredIngredients;
    }
    // remove expired ingredients
    public void removeExpiredIngredients() {
        LocalDate today = LocalDate.now();

        for (String ingredientsName : ingredients.keySet()) {
            List<Ingredient> batches = ingredients.get(ingredientsName);
            // clean all batches that pass their expiration date
            batches.removeIf(ing -> ing.getExpirationDate().isBefore(today));
        }
        // clean all the ingredients that were expired completely
        ingredients.entrySet().removeIf(ingredient -> ingredient.getValue().isEmpty());
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

    // return the amount of specific ingredient
    public double getTotalAmountOfIngredient(String ingredientName) {
        List<Ingredient> stockBatches = ingredients.get(ingredientName);
        if (stockBatches == null){
            return 0;
        }

        double sum = 0;
        for (Ingredient batch : stockBatches) {
            sum += batch.getAmount();
        }
        return sum;
    }

    public Ingredient getFirstBatch(String name){
        return ingredients.get(name).get(0);
    }

    public List<Dish> getMenu() {
        return menu;
    }

    public HashMap<String, List<Ingredient>> getIngredients() {
        return ingredients;
    }

    public void printInventoryStatus() {
        System.out.println("inventory data report : ");
        if (this.ingredients == null || this.ingredients.isEmpty()){
            System.out.println("the inventory is empty ");
        }else{
            ingredients.forEach((name,ingredient) -> System.out.println(name + ": " + getTotalAmountOfIngredient(name)));
        }

        if (this.menu == null || this.menu.isEmpty()){
            System.out.println("no dishes in the menu");
        }else{
            System.out.println("amount of dishes : " + menu.size());
            menu.forEach(dish -> System.out.println(dish.getName() + " (price:  " + dish.getPrice() + " )"));
        }
        System.out.println("--------------------------------");
    }


}
