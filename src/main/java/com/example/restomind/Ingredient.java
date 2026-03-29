package com.example.restomind;
import java.io.Serializable;
import java.time.LocalDate;

/* implements Serializable because InventoryManager saves also Ingredient into the file
    so it also has to implements Serializable*/
public class Ingredient implements Serializable {
    private String name;
    private double amount;
    private double pricePerUnit;
    private LocalDate expirationDate; // localDate is a date type that shows (day,month,year)

    public Ingredient(String name, double amount, double pricePerUnit,LocalDate expirationDate){
        this.name = name;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
        this.expirationDate = expirationDate;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }
}
