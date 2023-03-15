package com.example.parser;

import java.util.regex.Pattern;

public class TokenRule extends JsonRule{

    TokenRule(JsonRule rule){
        this.rule = rule.rule;
        this.type = rule.type;
    }
    private Pattern pattern;
    public Pattern pattern(){
        if(pattern == null){
            pattern = Pattern.compile(rule);
        }
        return pattern;
    }
}
