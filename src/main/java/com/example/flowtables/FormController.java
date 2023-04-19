package com.example.flowtables;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class FormController {
public static Stage stage;

public void initialize(){
      disableForm();
}

@FXML
private TextField editAbsoluteCnt;

@FXML
private TextField editDensity;

@FXML
private TextField editNestLevel;
@FXML
private Button btnStart;
@FXML
private Label lblChosenFile;
@FXML
private Label lblCommonCnt;
private TableBuilder builder;

@FXML
void onAnalyzeClicked() {
      if (btnStart.isDisable()) {
	    return;
      }
      builder.start();
      if(!builder.isCompleted()){
	    String info = String.format("Syntax error after %s statement", builder.getLastStatement());
	    sendMessage("Syntax error", info);
	    disableForm();
	    return;
      }
      editAbsoluteCnt.setText(String.valueOf(builder.getBranchCnt()));
//      editNestLevel.setText(String.valueOf(builder.getNestLevel()));
      lblCommonCnt.setText(String.valueOf(builder.getCommonCnt()));
      float density = (float) builder.getBranchCnt() / builder.getCommonCnt();
      editDensity.setText(String.format("%.3f", density));

}

@FXML
void onFileClose(ActionEvent event) {
      builder = null;
      disableForm();
}

@FXML
void onFileOpen(ActionEvent event) {
      FileChooser chooser = new FileChooser();
      File file = chooser.showOpenDialog(FormController.stage);
      String content;
      if (file != null && file.canRead() && (content = getFileContent(file)) != null) {
	    updateBuilder(content);
	    activeForm(file.getAbsolutePath());
      }
}

private void updateBuilder(String content) {
      builder = new TableBuilder();
      builder.setSource(content);
}

private void activeForm(String fileName) {
      lblChosenFile.setText(fileName);
      btnStart.setDisable(false);
}

private void disableForm() {
      editDensity.setText("-");
      editAbsoluteCnt.setText("-");
      editNestLevel.setText("-");
      btnStart.setDisable(true);
      lblChosenFile.setText("Файл не выбран");
      lblCommonCnt.setText("?");
}

private @Nullable String getFileContent(File file) {
      String result = null;
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	    StringBuilder strText = new StringBuilder();
	    String line;
	    while ((line = reader.readLine()) != null) {
		  strText.append(line).append("\n");
	    }
	    result = strText.toString();
      } catch (FileNotFoundException e) {
	    sendMessage("File error", "File not found");
      } catch (IOException e) {
	    sendMessage("File error", "IO error");
      }
      return result;
}

private void sendMessage(String title, String info){
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle(title);
      alert.setHeaderText("");
      alert.setContentText(info);
      alert.showAndWait();//.ifPresent(rs -> {});
}

}
