package com.example.restomind;
import java.io.Serializable;
import java.time.LocalDate;

// To save the data in the hard disk we should implement Serializable
public class Ingredient implements Serializable {
    private String name;
    private double amount;
    private double pricePerUnit;
    private LocalDate expirationDate; // LocalDate is a date type that shows (day,month,year)

    public Ingredient(String name, double amount, double pricePerUnit,LocalDate expirationDate){
        this.name = name;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
        this.expirationDate = expirationDate;
    }
}
