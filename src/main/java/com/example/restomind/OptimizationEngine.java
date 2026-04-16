package com.example.restomind;

import java.util.ArrayList;
import java.util.List;

public class OptimizationEngine {
    private InventoryManager inventory;
    private List<Dish> menu;
    private int plansSize = 100; // size of random plans

    public OptimizationEngine(InventoryManager inventory, List<Dish> menu) {
        this.inventory = inventory;
        this.menu = menu;
    }

    // the function that will run the optimization
    public void runOptimization() {

    }
}
