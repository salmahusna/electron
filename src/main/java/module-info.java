module com.example.electron {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;


    opens com.example.electron to javafx.fxml;
    exports com.example.electron;
}