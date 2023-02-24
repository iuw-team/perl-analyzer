module com.example.hasltedtables {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires com.google.gson;

    opens com.example.hasltedtables to javafx.fxml;
    exports com.example.hasltedtables;
}