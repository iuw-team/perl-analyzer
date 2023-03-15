package com.example.flowtables;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AppForm extends Application {
@Override
public void start(Stage stage) throws IOException {
      FXMLLoader fxmlLoader = new FXMLLoader(AppForm.class.getResource("flow-tables.fxml"));
      Scene scene = new Scene(fxmlLoader.load(), 600, 400);
      TableBuilder builder = new TableBuilder();
      try(BufferedReader reader = new BufferedReader(new FileReader("source.pl"))){
	    String line;
	    StringBuilder buffer = new StringBuilder();
	    while((line = reader.readLine()) != null){
		  buffer.append(line).append("\n");
	    }
	    builder.setSource(buffer.toString());
      }
      FormController.stage = stage;
      builder.start();
      stage.setTitle("FlowTables 3100");
      stage.setScene(scene);
      stage.show();
}

public static void main(String[] args) {
      launch();
}
}