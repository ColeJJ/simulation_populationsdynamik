module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    opens org.example to javafx.fxml;
    exports org.example;
}
