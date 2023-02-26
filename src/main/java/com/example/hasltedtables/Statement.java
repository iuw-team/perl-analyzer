package com.example.hasltedtables;

import java.util.ArrayList;
import java.util.List;

//comments is skipped
public class Statement {
    public enum Type {For, While, Until, CommonIf, If, Elsif, Else, Import, Line, Function, Body};
    private Type type;
    private Token[] self;
    private List<Statement> children;
    Statement(Type type, Token[] tokens){
        this.type = type;
        this.self = tokens;
        this.children = new ArrayList<>();
    }
    public void add(Statement child){
        children.add(child);
    }
    public void addAll(List<Statement> children){
        for(var child : children)
            add(child);
    }
    public Type type(){return type;}
    public List<Statement> children(){
        return this.children;
    }
}
