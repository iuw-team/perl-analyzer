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
private boolean isActive = false;
private TableBuilder tableBuilder;
@FXML
private Button btnInitAnalyze;


@FXML
private TableColumn<TableRow, String> clmOperators;
@FXML
private TableColumn<TableRow, String> clmOperatorsCnt;
@FXML
private TableColumn<TableRow, String> clmOperands;
@FXML
private TableColumn<TableRow, String> clmOperandsCnt;

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
private TableView<TableRow> tableGlobal;
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
    private String operator;
    private Integer operatorCnt;
    private String operand;
    private Integer operandCnt;
    TableRow(String operand, Integer operandCnt){
        this.operand = operand;
        this.operandCnt = operandCnt;
    }
    public void add(String operator, Integer operatorCnt){
        this.operator = operator;
        this.operatorCnt = operatorCnt;
    }
    public TableRow update(String operator, Integer operatorCnt){
        this.operator = operator;
        this.operatorCnt = operatorCnt;
        return this;
    }
}
private void updateFormContent(){
    var operators = tableBuilder.getOperators();
    var operands = tableBuilder.getOperands();
    List<TableRow> list = new ArrayList<TableRow>();
    operands.forEach((key, value) -> list.add(new TableRow(key, value)));
    AtomicInteger index = new AtomicInteger(0);
    operators.forEach((key, value) -> {
        if(index.get() > list.size())
            list.add(new TableRow("", 0).update(key, value));
        else
            list.get(index.get()).add(key, value);
        index.set(index.get() + 1);
    });
    tableGlobal.setItems(FXCollections.observableArrayList(list));
    clmOperators.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().operator));
    clmOperatorsCnt.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(String.valueOf(entity.getValue().operatorCnt)));
    clmOperands.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(entity.getValue().operand));
    clmOperandsCnt.setCellValueFactory(entity -> new ReadOnlyObjectWrapper<>(String.valueOf(entity.getValue().operandCnt)));

}
private void fillOperators(Map<String, Integer> operators){

}
@FXML
void onOpenFileChanged(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(stage);
    if(file != null && file.canRead()){
        isActive = true;
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