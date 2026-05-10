package com.example.restomind;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.Map;

public class ChefController {

    @FXML private VBox workPlanArea; // area that shows daily work plan
    @FXML private VBox ordersArea;      // area shows received orders from waiter
    @FXML private Label statusLabel;
    @FXML private ListView<String> workPlanList; // the work plan
    @FXML private VBox orders;   // the actual orders

    private DayManager dayManager;

    // at the start of the screen refresh the screen to see when work day start
    @FXML
    public void initialize() {
        this.dayManager = HelloApplication.getDayManager();

        // refresh every 3 seconds to see if there are new orders
        Timeline autoRefresh = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            refreshOrders();
        }));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
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
            orderCard.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 15; -fx-border-color: black; -fx-border-radius: 10; -fx-background-radius: 10;");

            Label title = new Label("#order " + order.getOrderId());
            title.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 16px;");
            orderCard.getChildren().add(title);

            // shows all the dishes of the order
            for (Map.Entry<String, Integer> dish : order.getOrder().entrySet()) {
                Label dishLabel = new Label("- " + dish.getKey() + ": " + dish.getValue());
                dishLabel.setStyle("-fx-text-fill: #000000; -fx-font-size: 14px;");
                orderCard.getChildren().add(dishLabel);
            }

            Button doneBtn = new Button("Done");
            doneBtn.setOnAction(e -> {
                dayManager.markOrderAsReady(order);

                dayManager.completeOrder(order); // removes from orders to cook
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
        dayManager.setServiceStarted(true);
        workPlanArea.setVisible(false);
        ordersArea.setVisible(true);
        statusLabel.setText("waiting for orders from waiters...");

        // start refresh every few seconds for checking new orders
        refreshOrders();
    }

    // do the function needed to close the day and be prepared to the next one
    @FXML
    protected void onCloseDayClick() {

        // if there are still orders left so you cant finish the day
        if (dayManager.hasOrders()) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Cannot Close Day");
            errorAlert.setHeaderText("Pending Orders Detected");
            errorAlert.setContentText("You cannot close the day until all orders are marked as 'Done'.");
            errorAlert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("End of Day");
        alert.setHeaderText("Are you sure you want to close the day?");
        alert.setContentText("This will update learning and save the inventory.");

        // if the response is ok then close the day
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                dayManager.closeDay();
                statusLabel.setText("Day closed successfully. Data saved.");

                // Close the application or reset view
                System.exit(0);
            }
        });
    }

}
