module com.iuwteam.flowtables {
      requires javafx.controls;
      requires javafx.fxml;

      requires org.controlsfx.controls;
      requires net.synedra.validatorfx;
      requires com.google.gson;
      requires annotations;
      opens com.example.chepintables to javafx.fxml;
      exports com.example.chepintables;
      exports  com.example.parser;
}