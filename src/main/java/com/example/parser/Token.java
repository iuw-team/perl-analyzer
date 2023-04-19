package com.example.parser;

public class Token {
    public enum Type {StringInner, StringPlain, BracketCircle, BracketCurly, BracketSquare, BracketTriangle, Keyword, Coma, Separator, Number,
        Assignment, ComplexAssignment, ArrowLambda, ArrowLink, Arithmetic, Logical, Comparing, StringCmp, StringRep, StringCat, ArrayRange,
        Variable, FloatWord, HashKey, FlowKeyword, HashBrackets, BracketVarGroup,
        Unknown
    };
    Token(String value, Type type){
        this.type = type;
        this.value = value;
    }
    public Type getType(){
        return type;
    }

    public String getValue(){
        return value;
    }
    public void setType(Type type){
        this.type = type;
    }

    private String value;
    private Type type;
}
