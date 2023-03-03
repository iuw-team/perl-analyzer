package com.example.hasltedtables;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
    public static String TOKENS_RULES = "Tokens.json";
    private String source;
    private TokenRule lastRule;
    private List<TokenRule> rules;
    private List<Token> tokenStream;
    private Stack<Integer> lastTokens;
    private int nextToken;
    Tokenizer(String source){
        this.source = source;
        rules = new ArrayList<>();
        tokenStream = new ArrayList<>();
        nextToken = 0;
        lastTokens = new Stack<>();
        addRules();
        splitTokens();
        this.source = null;
    }
    public boolean hasNext(){
        return nextToken < tokenStream.size();
    }
    public boolean hasPrev(){return nextToken > 0;}
    public Token nextToken(){
        Token result = tokenStream.get(nextToken);
        nextToken += 1;
        return result;
    }
    public void resetBack(){
        nextToken -= 1;
    }
    public List<Token> getTokenStream(){
        return this.tokenStream;
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
    /**cnt of tokens to skip
     * */
    public Token nextToken(int gapSize){
        for(int i = 0; i < gapSize; i++)
            this.nextToken();
        return this.nextToken();
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
    /**
     *  Save current position of token stream
     * */
    public int freeze(){
        lastTokens.push(nextToken);
        return nextToken;
    }
    public int mark(){
        return (nextToken - 1);
    }
    /** Free last asked token position
     * */
    public void release(){
        nextToken = lastTokens.pop();
    }
    /**
     * make stack empty
     * @return last token in group (after freezing stage)
     * */
    public void boost(){
        lastTokens.pop();
    }
    public void refresh(){
        this.release();
        this.freeze();
    }
    public Token[] getRange(int start, int end){
        Token[] group = new Token[end + 1 - start];
        int index = 0;
        for(int i = start; i <= end; i++){
            group[index] = tokenStream.get(i);
            index += 1;
        }
        return group;
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
//            for(Token token : tokenStream){
//                System.out.printf("<%s>", token.getType());
//            }
    }
}
