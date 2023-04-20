package com.example.chepintables;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AppForm extends Application {
@Override
public void start(Stage stage) throws IOException {
      FXMLLoader fxmlLoader = new FXMLLoader(AppForm.class.getResource("chepin-tables.fxml"));
      Scene scene = new Scene(fxmlLoader.load(), 600, 400);
      FormController.stage = stage;
      stage.setTitle("ChepinTables 3200");
      stage.setScene(scene);
      stage.show();
}

public static void main(String[] args) {
      launch();
}
}