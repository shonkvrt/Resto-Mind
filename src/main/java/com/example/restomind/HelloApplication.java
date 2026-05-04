package com.example.restomind;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private static DayManager dayManager;

    @Override
    public void start(Stage stage) {
        InventoryManager inventory = InventoryManager.loadDataFromFile("restaurant_data.dat");
        dayManager = new DayManager(inventory);

        // loads the chef and waiter views screen
        FXMLLoader chefLoader = new FXMLLoader(HelloApplication.class.getResource("chef-view.fxml"));
        FXMLLoader waiterLoader = new FXMLLoader(HelloApplication.class.getResource("waiter-view.fxml"));

        try{
            Scene scene = new Scene(chefLoader.load(), 900, 700);
            Stage chefStage = new Stage();
            chefStage.setTitle("RestoMind - Chef Terminal");
            chefStage.setScene(scene);
            chefStage.show();

            Stage waiterStage = new Stage();
            Scene waiterScene = new Scene(waiterLoader.load(), 900, 700);
            waiterStage.setTitle("RestoMind - Waiter Terminal");
            waiterStage.setScene(waiterScene);
            waiterStage.show();

        } catch (IOException e) {
            System.out.println("error with running chef view screen");
            throw new RuntimeException(e);
        }

    }

    public static DayManager getDayManager() {
        return dayManager;
    }

    public static void main(String[] args) {
        launch();
    }
}
