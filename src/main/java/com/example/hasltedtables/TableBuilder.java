package com.example.hasltedtables;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
public class TableBuilder {
    TableBuilder(){

    }
    private List<Token> operators;
    private List<Token> operands;
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
            if(statements == null)
                System.out.println("Syntax error");
            else {
                for(Statement piece : statements){
                    System.out.println(piece.type());
                    Token[] tokens = piece.getSelf();
                    if(tokens == null)
                        continue;
                    for(Token token : tokens)
                        System.out.println(token.getValue());
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
