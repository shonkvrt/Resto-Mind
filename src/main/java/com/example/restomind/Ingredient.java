package com.example.restomind;
import java.io.Serializable;
import java.time.LocalDate;

/* implements Serializable because InventoryManager saves also Ingredient into the file
    so it also has to implements Serializable*/
public class Ingredient implements Serializable {
    private String name;
    private double amout;
    private double pricePerUnit;
    private LocalDate expirationDate; // localDate is a date type that shows (day,month,year)

    public Ingredient(String name, double amout, double pricePerUnit,LocalDate expirationDate){
        this.name = name;
        this.amout = amout;
        this.pricePerUnit = pricePerUnit;
        this.expirationDate = expirationDate;
    }

    public String getName() {
        return name;
    }

    public double getAmout() {
        return amout;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setAmout(double amout) {
        this.amout = amout;
    }
}
