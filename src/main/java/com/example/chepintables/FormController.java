package com.example.chepintables;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormController {
public static Stage stage;

public void initialize() {
      disableForm();
}

@FXML
private Button btnStart;
@FXML
private Label lblChosenFile = new Label();
@FXML
private TableColumn<ChepinTableRow, String> clmGroupLeft;

@FXML
private TableColumn<ChepinTableRow, String> clmGroupRight;

@FXML
private TableColumn<ChepinTableRow, Integer> clmGroupSizeLeft;

@FXML
private TableColumn<ChepinTableRow, Integer> clmGroupSizeRight;

@FXML
private TableColumn<SpenTableRow, Integer> clmSpenValues;

@FXML
private TableColumn<SpenTableRow, String> clmSpenVars;

@FXML
private TableColumn<ChepinTableRow, String> clmVarsLeft;
@FXML
private TableColumn<ChepinTableRow, String> clmVarsRight;
@FXML
private TextField editChepinFull;

@FXML
private TextField editChepinIo;

@FXML
private TextField editSumSpen;

@FXML
private TableView<ChepinTableRow> tableChepinLeft;

@FXML
private TableView<ChepinTableRow> tableChepinRight;

@FXML
private TableView<SpenTableRow> tableSpen;


private String collectVarList(Map<String, TableBuilder.TokenGroup> chepinMap, TableBuilder.TokenGroup group){
      StringBuilder strText = new StringBuilder();
      for(var entry : chepinMap.entrySet())
	    if(entry.getValue() == group){
		  strText.append(entry.getKey()).append(",");
	    }
      strText.setLength(strText.length() - 1);
      return strText.toString();
}
private Integer getGroupSize(Map<String, TableBuilder.TokenGroup> chepinMap, TableBuilder.TokenGroup group){
      int bufferSum = 0;
      for(var entry : chepinMap.entrySet()){
	    if(entry.getValue() == group)
		  bufferSum += 1;
      }
      return bufferSum;
}
private List<ChepinTableRow> collectRows(Map<String, TableBuilder.TokenGroup> chepinMap){
      List<ChepinTableRow> rows = new ArrayList<>();
      for (TableBuilder.TokenGroup group : TableBuilder.TokenGroup.values()){
	    int groupSize = getGroupSize(chepinMap, group);
	    if(groupSize != 0){
		  String varList = collectVarList(chepinMap, group);
		  rows.add(new ChepinTableRow(group.toString(), groupSize, varList));
	    }
      }
      return rows;
}
private float calcChepinMetrics(Map<String, TableBuilder.TokenGroup> chepinMap){
      final List<Float> factors = List.of(1.0f, 2.0f, 3.0f, 0.5f);
      int index = 0;
      float chepinValue = 0.0f;
      assert factors.size() == TableBuilder.TokenGroup.values().length;
      for(TableBuilder.TokenGroup group : TableBuilder.TokenGroup.values()){
	    int groupSize = getGroupSize(chepinMap, group);
	    chepinValue += factors.get(index) * groupSize;
	    index += 1;
      }
      return chepinValue;
}
private void setIOChepin(Map<String, TableBuilder.TokenGroup> ioMap) {
      var rows = collectRows(ioMap);
      tableChepinRight.setItems(FXCollections.observableArrayList(rows));
      clmGroupRight.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().group));
      clmGroupSizeRight.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().groupSize));
      clmVarsRight.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().varsList));
      editChepinIo.setText(String.format("%.3f", calcChepinMetrics(ioMap)));
}

private void setFullChepin(Map<String, TableBuilder.TokenGroup> fullMap) {
      var rows = collectRows(fullMap);
      tableChepinLeft.setItems(FXCollections.observableArrayList(rows));
      clmGroupLeft.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().group));
      clmGroupSizeLeft.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().groupSize));
      clmVarsLeft.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().varsList));
      editChepinFull.setText(String.format("%.3f", calcChepinMetrics(fullMap)));
}

private void setSpen(Map<String, Integer> spenMap) {
      Integer buffSum = 0;
      List<SpenTableRow> rows = new ArrayList<>();
      clmSpenValues.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().number));
      clmSpenVars.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().name));
      for(var entry : spenMap.entrySet()){
	    buffSum += entry.getValue();
	    rows.add(new SpenTableRow(entry.getKey(), entry.getValue()));
      }
      tableSpen.setItems(FXCollections.observableArrayList(rows));
      editSumSpen.setText(String.valueOf(buffSum));
}


private TableBuilder builder;

@FXML
void onAnalyzeClicked() {
      if (btnStart.isDisable()) {
	    return;
      }
      builder.start();
      if (builder.isCompleted()) {
	    setSpen(builder.getSpenMap());
	    setFullChepin(builder.getFullChepinMap());
	    setIOChepin(builder.getIOChepinMap());
      } else {
	    String info = String.format("Syntax error after %s statement", builder.getLastStatement());
	    sendMessage("Syntax error", info);
	    disableForm();
      }
}

@FXML
void onProgramExit(ActionEvent event){
      stage.close();
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
      editSumSpen.setText("-");
      editChepinIo.setText("-");
      editChepinFull.setText("-");
      btnStart.setDisable(true);
      lblChosenFile.setText("Файл не выбран");
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

private void sendMessage(String title, String info) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle(title);
      alert.setHeaderText("");
      alert.setContentText(info);
      alert.showAndWait();//.ifPresent(rs -> {});
}

private class SpenTableRow {
      private final String name;
      private final Integer number;
      private SpenTableRow(String name, Integer number) {
	    this.name = name;
	    this.number = number;
      }
}

private class ChepinTableRow {
      private final String group;
      private final Integer groupSize;
      private final String varsList;

      private ChepinTableRow(String group, Integer groupSize, String varsList) {
	    this.group = group;
	    this.groupSize = groupSize;
	    this.varsList = varsList;
      }
}
}
