package com.example.hasltedtables;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
    public static String TOKENS_RULES = "Tokens.json";
    String source;
    TokenRule lastRule;
    List<TokenRule> rules;
    List<Token> tokenStream;
    Tokenizer(String source){
        this.source = source;
        rules = new ArrayList<>();
        tokenStream = new ArrayList<>();
        addRules();
        splitTokens();

    }
    public boolean hasToken(){
        return false;
    }
    public Token nextToken(){
        return null;
    }
    private void addRules(){
        try(BufferedReader reader = new BufferedReader(new FileReader(TOKENS_RULES))){
            StringBuilder fileContent = new StringBuilder();
            Gson gson = new Gson();
            String line;
            while((line = reader.readLine()) != null)
                fileContent.append(line);
            List<JsonRule> jsonList = List.of(gson.fromJson(fileContent.toString(), JsonRule[].class));
            for(JsonRule json : jsonList ){
                rules.add(new TokenRule(json));
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Impossible to find tokens' rule file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * @return index in given token where next step should be started
     * */
    private int checkRules(String token, int offset){
        int index = 0;
        lastRule = null;
        for(TokenRule rule : rules){
            Pattern pattern = rule.pattern();
            Matcher matcher = pattern.matcher(token);
            matcher.region(offset, token.length());
            if(matcher.lookingAt()){
                index = matcher.end();
                lastRule = rule;
                break;
            }
        }
        return index;
    }
    private void sendToken(String token){
        if(lastRule.type() != Token.Type.Unknown)
            tokenStream.add(new Token(token, lastRule.type()));
    }
    private void splitTokens(){
            int lastIndex = 0;
            int nextIndex;
            while(lastIndex < source.length()){
                nextIndex = checkRules(source, lastIndex);
                if(nextIndex == 0){
                    System.out.format("%s",source.charAt(lastIndex));
                    lastIndex += 1;
                }
                else {
                    String value = source.substring(lastIndex, nextIndex);
                    sendToken(value);
                    lastIndex = nextIndex;
                }
            }
            for(Token token : tokenStream){
                System.out.format("<%s>", token.getType());
            }
    }
}
