package com.example.restomind;
import java.io.Serializable;
import java.time.LocalDate;

/* implements Serializable because InventoryManager saves also Ingredient into the file
    so it also has to implements Serializable*/
public class Ingredient implements Serializable {
    private String name;
    private double amoutInKg;
    private double pricePerKg;
    private LocalDate expirationDate; // localDate is a date type that shows (day,month,year)

    public Ingredient(String name, double amoutInKg, double pricePerKg,LocalDate expirationDate){
        this.name = name;
        this.amoutInKg = amoutInKg;
        this.pricePerKg = pricePerKg;
        this.expirationDate = expirationDate;
    }

    public String getName() {
        return name;
    }

    public double getAmoutInKg() {
        return amoutInKg;
    }

    public double getPricePerKg() {
        return pricePerKg;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setAmoutInKg(double amoutInKg) {
        this.amoutInKg = amoutInKg;
    }
}
