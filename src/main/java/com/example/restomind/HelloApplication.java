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

        // loads the chef view screen
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("chef-view.fxml"));

        try{
            Scene scene = new Scene(fxmlLoader.load(), 900, 700);

            stage.setTitle("RestoMind - Chef Terminal");
            stage.setScene(scene);
            stage.show();
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
