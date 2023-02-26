package com.example.hasltedtables;

import java.util.ArrayList;
import java.util.List;

public class TokenGroups {
    private List<Token[]> tokenGroups;
    TokenGroups(){
        tokenGroups = new ArrayList<>();
    }
    public void add(Token[] tokens){
        tokenGroups.add(tokens);
    }
    public List<Token[]> get(){
        return tokenGroups;
    }
}
