package com.example.hasltedtables;

import java.util.List;
public class TableBuilder {
    TableBuilder(){

    }
    private List<Token> operators;
    private List<Token> operands;
    public void start(){
        String source = "while($x < 10){for(my $i = 0; $i < 10; $i++);}";
        Tokenizer tokenizer = new Tokenizer(source);
        while(tokenizer.hasToken()){
            Token token = tokenizer.nextToken();

        }


    }
}
