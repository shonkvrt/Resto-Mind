module com.example.restomind {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.restomind to javafx.fxml;
    exports com.example.restomind;
}