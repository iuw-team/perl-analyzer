package com.example.hasltedtables;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TableBuilder {
    private String source;
    private String lastStatement;
    private boolean isCompleted;
    TableBuilder(){
        operands = new HashMap<>();
        operators = new HashMap<>();
        isCompleted = false;
    }
    private Map<String, Integer> operators;
    private Map<String, Integer> operands;
    private void saveOperator(String value){
        Integer cnt = operators.getOrDefault(value, 0) + 1;
        if(cnt == 1)
            operators.put(value, cnt);
        else
            operators.replace(value, cnt);
    }
    private void saveOperand(String value){
        Integer cnt = operands.getOrDefault(value, 0) + 1;
        if(cnt == 1)
            operands.put(value, cnt);
        else
            operands.replace(value, cnt);
    }
    private @NotNull List<String> catchAll(String regExp, String source){
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(source);
        while(matcher.find())
            result.add(matcher.group());
        return result;
    }
    private boolean isSystemKeyword(String value){
        final List<String> list = List.of("STDIN", "STDERR", "STDOUT");
        boolean result = false;
        for(String keyword : list){
            if(keyword.equals(value)){
                result = true;
                break;
            }
        }
        return result;
    }
    private boolean isFunctionCall = false;
    private void dispatchToken(Token token){
        if(token == null)
            return;
        String value = token.getValue();
        switch(token.getType()){
                case StringInner -> {
                    List<String> variables = catchAll("(\\\\)?\\$[a-zA-Z_][a-zA-Z_0-9]*", value);
                    variables = variables.stream()
                                        .filter(name->!name.contains("\\$"))
                                        .toList();
                    variables.forEach(this::saveOperand);
                    saveOperand(value);
                }
                case BracketCircle -> {
                    if(value.equals("(") && !isFunctionCall)
                        saveOperator("( )");
                }
                case BracketCurly -> {
                    if(value.equals("{"))
                        saveOperator("{ }");
                }
                case BracketSquare -> {
                    if(value.equals("["))
                        saveOperator("[ ]");
                }
                case Keyword -> {
                         Stream.of("last", "redo", "next", "return")
                         .filter(keyword -> keyword.equals(value))
                         .findAny()
                         .ifPresent(this::saveOperator);
                }
                case  Coma, Separator, Assignment, ComplexAssignment,
                             ArrowLambda, ArrowLink, Arithmetic, Logical,
                             Comparing, StringCmp, StringRep, StringCat,
                             ArrayRange -> {saveOperator(value);}
                case Digits, Variable, StringPlain -> {saveOperand(value);}
                case FloatWord -> {
                    if(isSystemKeyword(value)){
                        saveOperand(value);
                    }
                    else {
                        saveOperator(value);
                        isFunctionCall = true;
                    }
                }
        }
        if(token.getType() != Token.Type.FloatWord)
            isFunctionCall = false;
    }
    private void dispatchStatement(Statement piece){
        String operator = null;
        Token[] self = piece.getSelf();
            switch(piece.type()){
                case For -> {
                    operator = "for";
                    for(int i = 2; i < self.length - 1; i++)
                        if(!self[i].getValue().equals(";"))
                            dispatchToken(self[i]);
                }
                case While -> {
                    operator = "while";
                    for(int i = 2; i < self.length - 1;i++)
                        dispatchToken(self[i]);
                }
                case Until -> {
                    operator = "until";
                    for(int i = 2; i < self.length - 1; i++)
                        dispatchToken(self[i]);
                }
                case CommonIf -> {
                    operator = "if-elsif-else";
                }
                case If, Elsif -> {
                    for(int i = 2; i < self.length - 1; i++)
                        dispatchToken(self[i]);
                }
                case Else -> {}
                case Import -> {}
                case Line -> {
                    for(Token token : self){
                        dispatchToken(token);
                    }
                }
                case Function -> {
                    operator = self[1].getValue();//the name of function self; no token to dispatch
                    saveOperand(operator);
                }
                case Body -> {
                    if(self[0].getType() == Token.Type.Separator)
                        dispatchToken(self[0]);
                    else
                        operator = "{...}";
                }
            }
        if(operator != null)
            saveOperator(operator);
        for(Statement child : piece.children())
            dispatchStatement(child);
    }
    public void setSource(String source){
        this.source = source;
    }
    public Map<String, Integer> getOperators(){
        return this.operators;
    }
    public Map<String, Integer> getOperands(){
        return this.operands;
    }
    public boolean isCompleted(){
        return isCompleted;
    }
    private void setLastStatement(Statement last){
        if(last == null){
            lastStatement = "";
        }
        else {
            StringBuilder builder = new StringBuilder();
            if(last.getSelf() != null){
                Stream.of(last.getSelf())
                        .forEach(token -> builder.append(token.getValue()));
            }
            lastStatement = builder.toString();
        }
    }
    public String getLastStatement(){
        return lastStatement;
    }
    public void start(){
        Parser parser = new Parser(source);
        parser.parse();
        List<Statement> statements = parser.getStatements();
        if(statements == null){
            isCompleted = false;
            setLastStatement(parser.getLastStatement());
        }
        else {
            isCompleted = true;
            setLastStatement(parser.getLastStatement());
            for(Statement piece : statements){
                dispatchStatement(piece);
            }
        }

    }
}
