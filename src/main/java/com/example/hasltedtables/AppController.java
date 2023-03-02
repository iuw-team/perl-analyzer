package com.example.hasltedtables;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AppController {
public static Stage stage;
private TableBuilder tableBuilder;
@FXML
private Button btnInitAnalyze;


@FXML
private TableColumn<TableRow, String> clmOperators;
@FXML
private TableColumn<TableRow, Integer> clmOperatorsCnt;
@FXML
private TableColumn<TableRow, String> clmOperands;
@FXML
private TableColumn<TableRow, Integer> clmOperandsCnt;

@FXML
private TextField editAppDict;

@FXML
private TextField editAppLength;

@FXML
private TextField editAppVolume;

@FXML
private TextField editCommonOperands;

@FXML
private TextField editCommonOperators;

@FXML
private TextField editOperandDict;

@FXML
private TextField editOperatorDict;
@FXML
private TableView<TableRow> tableRight;
@FXML
private TableView<TableRow> tableLeft;
@FXML
void onBtnStartClicked(MouseEvent event) {
    tableBuilder.start();
    if(tableBuilder.isCompleted()){
        showMessage("Completed", "The source code was analyzed");
        updateFormContent();
    }
    else {
        String lastStatement = tableBuilder.getLastStatement();
        if(lastStatement.equals(""))
            showMessage("File error", "No source code was found");
        else{
            showMessage("Syntax error", "The syntax error after `" + lastStatement + "`");
        }
    }
}
private class TableRow {
    final private String string;
    final private Integer number;
    TableRow(String string, Integer number){
        this.string = string;
        this.number = number;
    }

}
private void updateFormContent(){
    var operators = tableBuilder.getOperators();
    var operands = tableBuilder.getOperands();
    List<TableRow> listRight = new ArrayList<>();
    List<TableRow> listLeft = new ArrayList<>();
    operands.forEach((key, value) -> listRight.add(new TableRow(key, value)));
    operators.forEach((key, value) -> listLeft.add(new TableRow(key, value)));
    tableLeft.setItems(FXCollections.observableArrayList(listLeft));
    tableRight.setItems(FXCollections.observableArrayList(listRight));
    clmOperators.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().string));
    clmOperands.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().string));
    clmOperatorsCnt.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().number));
    clmOperandsCnt.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().number));
    updateParams(operators, operands);
}
private void updateParams(Map<String, Integer> operators, Map<String, Integer> operands){
    AtomicInteger operandCnt = new AtomicInteger(0);
    AtomicInteger operatorCnt = new AtomicInteger(0);
    int dictSize = operators.size() + operands.size();
    int length;
    editOperandDict.setText(String.valueOf(operands.size()));
    editOperatorDict.setText(String.valueOf(operators.size()));
    editAppDict.setText(String.valueOf(dictSize));
    operands.forEach((key, value) -> operandCnt.set(operandCnt.get() + value));
    editCommonOperands.setText(String.valueOf(operandCnt.get()));
    operators.forEach((key,value)-> operatorCnt.set(operatorCnt.get() + value));
    length = operandCnt.get() + operatorCnt.get();
    editCommonOperators.setText(String.valueOf(operatorCnt.get()));
    editAppLength.setText(String.valueOf(length));
    editAppVolume.setText(String.format("%.3f", Math.log(dictSize)/Math.log(2)*length));
}
@FXML
void onOpenFileChanged(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(stage);
    if(file != null && file.canRead()){
        btnInitAnalyze.setDisable(false);
        String content = getFileContent(file);
        tableBuilder = new TableBuilder();
        tableBuilder.setSource(content);
    }
}
private String getFileContent(File file) {
    String result = "";
    try(BufferedReader reader = new BufferedReader(new FileReader(file.getAbsoluteFile()))){
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }
        result = builder.toString();
    } catch (IOException e) {
        showMessage("Ошибка доступа файла", "Невозможно анализировать файл");
    }
    return result;
}

private void showMessage(String title, String info){
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText("");
    alert.setContentText(info);
    alert.showAndWait();//.ifPresent(rs -> {});
}
@FXML
void onProgramExit(ActionEvent event) {

}

}