package com.example.restomind;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.control.*;

import java.util.HashMap;
import java.util.Optional;

public class WaiterController {
    // vertical
    @FXML
    private VBox waitingScreen;
    // horizontal
    @FXML
    private HBox mainWorkScreen;
    @FXML private VBox menu;
    @FXML private VBox dropZone;
    @FXML private VBox orderDishesList;
    @FXML private VBox preOrderActions;
    @FXML private HBox readyOrdersArea;
    private DayManager dayManager;
    private Order currentOrder;

    @FXML
    public void initialize() {
        this.dayManager = HelloApplication.getDayManager();
        this.currentOrder = new Order();

        Timeline checkService = new Timeline();

        // every second check if service started for infinite if it does go to main screen
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), e -> {

            if (dayManager.isServiceStarted()) {

                checkService.stop();
                waitingScreen.setVisible(false); // hide waiting screen
                mainWorkScreen.setVisible(true); // show main screen
                loadMenu();
                readyOrdersRefresh();


            }
        });

        checkService.getKeyFrames().add(keyFrame);
        checkService.setCycleCount(Timeline.INDEFINITE);
        checkService.play();

        setupDropZone();
    }

    // load the menu to the screen
    private void loadMenu() {
        menu.getChildren().clear();
        preOrderActions.getChildren().clear();

        for (Dish dish : dayManager.getInventory().getMenuDishes()) {
            VBox card = new VBox(5);
            card.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 10; -fx-border-color: #bdc3c7;");

            Label nameLabel = new Label(dish.getName());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000; -fx-font-size: 14px;");

            Label priceLabel = new Label("price: " + dish.getPrice());
            priceLabel.setStyle("-fx-text-fill: #000000; -fx-font-size: 13px;");

            loadPreOrderActions(dish);
            // add the name and the price to the card
            card.getChildren().addAll(nameLabel, priceLabel);

            // add card drag
            card.setOnDragDetected(event -> {
                // takes the info from the dragged card (dish name)
                javafx.scene.input.Dragboard db = card.startDragAndDrop(javafx.scene.input.TransferMode.COPY);
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();

                // gives a name for the whole card to identify the dish
                content.putString(dish.getName());
                // connect the copy drag card with is information
                db.setContent(content);

                // stop the event from keep going to other layers
                event.consume();
            });

            // add card to the menu
            menu.getChildren().add(card);
        }
    }

    // actions to do before order (general actions)
    private void loadPreOrderActions(Dish dish) {
        String action = dayManager.getSuggestedAction(dish);

        // checks if action is discount and if it does then show it as general action
        if (action.startsWith("Discount") || action.equals("Chef Recommendation")) {
            Label actionLabel = new Label(action + ": " + dish.getName());
            actionLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold;");
            preOrderActions.getChildren().add(actionLabel);
        }

    }

    // gets the dragged card and handel it
    @FXML
    public void setupDropZone() {
        // card drag is over the drop zone
        dropZone.setOnDragOver(event -> {
            // the place that we took the card is not from drag zone and have dish name
            if (event.getGestureSource() != dropZone && event.getDragboard().hasString()) {
                // shows that we can release the card to the drop zone
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            event.consume();
        });

        // while dropping the card
        dropZone.setOnDragDropped(event -> {
            // gets the copy card
            javafx.scene.input.Dragboard db = event.getDragboard();
            boolean success = false;

            // check if it has the dish name
            if (db.hasString()) {
                String dishName = db.getString();
                Dish dish = dayManager.getInventory().getDishByName(dishName);

                // checks if there is still dish left
                int stock = dayManager.getPreparedStock(dish);

                if (stock <= 0) {
                    Dish anotherDish = findBestAnotherDish(dish);
                    showAnotherBestDish(dish, anotherDish);
                    event.setDropCompleted(false);
                    event.consume();
                    return;
                }

                int potentialHardLoad = currentOrder.getHardDishesCount();
                if(dish.isHardToMake()){
                    potentialHardLoad++;
                }

                if(dayManager.checkKitchenLoad(3,potentialHardLoad)){
                    // needs to press ok for continue
                    if (!showLoadWarningAndConfirm(dishName)) {
                        event.setDropCompleted(false);
                        event.consume();
                        return;
                    }
                }

                currentOrder.addItem(dish);

                VBox orderCard = createOrderCard(dishName);
                orderDishesList.getChildren().add(orderCard);
                event.setDropCompleted(true);

            }
            event.consume();
        });
    }

    private void showAnotherBestDish(Dish dishOver, Dish bestDish) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Dish is over");
        alert.setHeaderText(dishOver.getName() + " over");
        if (bestDish != null) {
            alert.setContentText("advice to customer: " + bestDish.getName());
        } else {
            alert.setContentText("no available dishes");
        }
        alert.showAndWait();
    }


    private boolean showLoadWarningAndConfirm(String dishName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("load in kitchen ");
        alert.setHeaderText("kitchen at his peak load");
        alert.setContentText(dishName + "will be delayed \n");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // find the best dish when one of the dish is over
    private Dish findBestAnotherDish(Dish outOfStockDish) {
        Dish bestAnotherDish = null;
        int maxStock = -1;

        for (Dish dish : dayManager.getInventory().getMenuDishes()) {

            if (!dish.getName().equals(outOfStockDish.getName())) {
                int currentStock = dayManager.getPreparedStock(dish);
                if (currentStock > 0) {

                    if (currentStock > maxStock) {
                        maxStock = currentStock;
                        bestAnotherDish = dish;
                    }
                }
            }


        }

        return bestAnotherDish;
    }

    // create the order card in the drop zone
    private VBox createOrderCard(String dishName) {
        Dish dish = dayManager.getInventory().getDishByName(dishName);
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-border-color: #3498db; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");

        Label name = new Label(dishName);
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: #000000; -fx-font-size: 14px;");

        HBox amountControl = new HBox(10);
        amountControl.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button minusBtn = new Button("-");
        Label amountLabel = new Label("1");
        amountLabel.setStyle("-fx-font-size: 14px; -fx-padding: 0 5 0 5; -fx-text-fill: #000000; -fx-font-weight: bold;");
        Button plusBtn = new Button("+");
        
        minusBtn.setOnAction(e -> {
            int currentAmount = Integer.parseInt(amountLabel.getText());
            if (currentAmount > 1) {
                currentOrder.removeItem(dish);
                amountLabel.setText(String.valueOf(currentAmount - 1));
            }
        });

        plusBtn.setOnAction(e -> {
            int potentialHardLoad = currentOrder.getHardDishesCount();
            if(dish.isHardToMake()){
                potentialHardLoad++;
            }

            if (dayManager.checkKitchenLoad(3, potentialHardLoad)) {
                if (!showLoadWarningAndConfirm(dishName)) return;
            }

            currentOrder.addItem(dish);
            int currentAmount = Integer.parseInt(amountLabel.getText());
            amountLabel.setText(String.valueOf(currentAmount + 1));
        });

        // a blank space between the delete and the change amount
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // change the size of the space


        Button deleteBtn = new Button("X");
        deleteBtn.setOnAction(e -> {
            currentOrder.removeAllOfDish(dish);
            orderDishesList.getChildren().remove(card);
        });

        amountControl.getChildren().addAll(minusBtn, amountLabel, plusBtn, spacer, deleteBtn);

        card.getChildren().addAll(name, amountControl);
        
        return card;
    }
    
    // done with the order and send it to the chefs
    @FXML
    private void onSendOrderClick() {

        if (currentOrder.getOrder().isEmpty()) {
            System.out.println("cant order empty order");
            return;
        }

        boolean success = dayManager.processOrder(currentOrder);

        if (success) {
            orderDishesList.getChildren().clear();
            currentOrder = new Order();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error: not enough prepared dishes");
            alert.showAndWait();
        }
    }

    // refreshes for ready orders
    private void readyOrdersRefresh() {
        Timeline refreshLoop = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            updateReadyOrders();
        }));
        refreshLoop.setCycleCount(Timeline.INDEFINITE);
        refreshLoop.play();
    }

    private void updateReadyOrders() {
        readyOrdersArea.getChildren().clear();

        for (Order order : dayManager.getReadyOrders()) {
            HBox card = new HBox(10);
            card.setStyle("-fx-background-color: #2ecc71; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5;");

            Label info = new Label("# order " + order.getOrderId() + "is ready");
            info.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            Button deliveredBtn = new Button("delivered");
            deliveredBtn.setOnAction(e -> {
                dayManager.deliverOrder(order.getOrderId());
                updateReadyOrders();
            });

            card.getChildren().addAll(info, deliveredBtn);
            readyOrdersArea.getChildren().add(card);
        }
    }
}
