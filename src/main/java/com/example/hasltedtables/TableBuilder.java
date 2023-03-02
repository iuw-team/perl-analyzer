package com.example.hasltedtables;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TableBuilder {

    TableBuilder(){
        operands = new HashMap<>();
        operators = new HashMap<>();
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
                    if(value.equals("("))
                        saveOperator("(@@value)");
                }
                case BracketCurly -> {
                    if(value.equals("{"))
                        saveOperator("{@@index}");
                }
                case BracketSquare -> {
                    if(value.equals("["))
                        saveOperator("[@@index]");
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
                    if(isSystemKeyword(value))
                        saveOperator(value);
                    else
                        saveOperand(value + "()");
                }
        }
    }
    private void dispatchStatement(Statement piece){
        String operator = null;
        Token[] self = piece.getSelf();
            switch(piece.type()){
                case For -> {
                    operator = "for(;;)";
                    for(int i = 2; i < self.length - 1; i++)
                        if(!self[i].getValue().equals(";"))
                            dispatchToken(self[i]);
                }
                case While -> {
                    operator = "while()";
                    for(int i = 2; i < self.length - 1;i++)
                        dispatchToken(self[i]);
                }
                case Until -> {
                    operator = "until()";
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
                    for (Token token : self)
                        dispatchToken(token);
                }
                case Function -> {
                    operator = self[1].getValue();//the name of function self; no token to dispatch
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
    public void start(){
        String source;
        try(BufferedReader reader = new BufferedReader(new FileReader("source.pl"))){
            StringBuilder strText = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
                strText.append(line).append("\n");
            source = strText.toString();
            Parser parser = new Parser(source);
            parser.parse();
            List<Statement> statements = parser.getStatements();
            if(statements == null){
                System.out.println("Syntax error");

            }
            else {

                for(Statement piece : statements){
                    dispatchStatement(piece);
                }
                System.out.println("Operators");
                for(var entry : operators.entrySet()){
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
                System.out.println("Operands");
                for(var entry : operands.entrySet()){
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
