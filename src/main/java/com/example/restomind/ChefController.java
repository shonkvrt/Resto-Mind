package com.example.restomind;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.util.Map;

public class ChefController {

    @FXML private VBox workPlanArea; // area that shows daily work plan
    @FXML private VBox ordersArea;      // area shows received orders from waiter
    @FXML private Label statusLabel;
    @FXML private ListView<String> workPlanList; // the work plan
    @FXML private VBox orders;   // the actual orders

    private DayManager dayManager;

    @FXML
    public void initialize() {
        this.dayManager = HelloApplication.getDayManager();
        ordersArea.setVisible(false); // hide orders at the beginning
    }

    // push start day button
    @FXML
    protected void onStartDayClick() {
        dayManager.generatePlan(); // genetic algorithm
        WorkPlan plan = dayManager.getDailyPlan();

        // resets the work plan and put the new one
        workPlanList.getItems().clear();
        for (Map.Entry<String, Integer> dish : plan.getPlan().entrySet()) {
            workPlanList.getItems().add(dish.getKey() + ": " + dish.getValue() + " units");
        }
        statusLabel.setText("work plan is ready, start the preparation");
    }


    // refreshes the screen every few seconds for new orders
    public void refreshOrders() {
        // clears for refresh but then adds the new orders with the old ones
        orders.getChildren().clear();

        //build order card for every order
        for (Order order : dayManager.getOrders()) {
            // space between everything in this order card is 10 pixel (title,list,button)
            VBox orderCard = new VBox(10);
            orderCard.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 15; -fx-border-color: black;");

            Label title = new Label("#order " + order.getOrderId());
            orderCard.getChildren().add(title);

            // shows all the dishes of the order
            for (Map.Entry<String, Integer> dish : order.getOrder().entrySet()) {
                orderCard.getChildren().add(new Label("- " + dish.getKey() + ": " + dish.getValue()));
            }

            Button doneBtn = new Button("Done");
            doneBtn.setOnAction(e -> {
                dayManager.completeOrder(order.getOrderId()); // removes from orders to cook
                refreshOrders(); // refresh screen
            });

            orderCard.getChildren().add(doneBtn);
            orders.getChildren().add(orderCard);
        }
    }

    // push button finish preparation
    @FXML
    protected void onFinishPrepClick() {
        dayManager.prepareDishesForDay(); // takes the ingredients from inventory to the prepared dishes
        workPlanArea.setVisible(false);
        ordersArea.setVisible(true);
        statusLabel.setText("waiting for orders from waiters...");

        // start refresh every few seconds for checking new orders
        refreshOrders();
    }
}
