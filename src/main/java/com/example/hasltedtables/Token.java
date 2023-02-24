package com.example.hasltedtables;

public class Token {
    public enum Type {StringInner, StringPlain, BracketCircle, BracketCurly, BracketSquare, Keyword, Coma, Separator, Digits,
        Assignment, ComplexAssignment, ArrowLambda, ArrowLink, Arithmetic, Logical, Comparing, StringCmp, StringRep, StringCat, ArrayRange,
        Variable, FloatWord,
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

    private String value;
    private Token.Type type;
}
