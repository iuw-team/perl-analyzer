package com.example.flowtables;

import com.example.parser.Parser;
import com.example.parser.Statement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TableBuilder {
private String source;
private String lastStatement;
private boolean isCompleted;

public TableBuilder() {
      isCompleted = false;
}
private @NotNull List<String> catchAll(String regExp, String source) {
      List<String> result = new ArrayList<>();
      Pattern pattern = Pattern.compile(regExp);
      Matcher matcher = pattern.matcher(source);
      while (matcher.find())
	    result.add(matcher.group());
      return result;
}

private boolean isSystemKeyword(String value) {
      final List<String> list = List.of("STDIN", "STDERR", "STDOUT");
      boolean result = false;
      for (String keyword : list) {
	    if (keyword.equals(value)) {
		  result = true;
		  break;
	    }
      }
      return result;
}
private int regStatCnt;
private int condStatCnt;

private int maxNestLevel;
private void updateStat(int dtReg, int dtCond){
      regStatCnt += dtReg;
      condStatCnt += dtCond;
}
private void defaultCallable(Statement parent, Integer nestLevel){
      for(Statement child : parent.children()){
	    dispatchStatement(child, nestLevel);
      }
}
private void ascendingCallable(Statement parent, Integer nestLevel){
      for(Statement child : parent.children()){
	    dispatchStatement(child, nestLevel);
	    nestLevel += 1;
      }
}
private void dispatchStatement(Statement piece, int nestLevel) {
      BiConsumer<Statement, Integer> callable = this::defaultCallable;
      switch (piece.type()) {
	    case For, Foreach -> {
		  updateStat(3, 1);
		  nestLevel += 1;
	    }
	    case While, Until, If, Elsif -> {
		  updateStat(1, 1);
		  nestLevel += 1;
	    }
	    case CommonIf -> {
		  callable = this::ascendingCallable;
	    }
	    case Else -> nestLevel -= 1;
	    case Line -> updateStat(1, 0);
	    case Import, Function, Body-> {
		  //function declaration is commonly skipped
	    }
      }
      maxNestLevel = Math.max(maxNestLevel, nestLevel);
      callable.accept(piece, nestLevel);
}
public int getNestLevel(){return maxNestLevel;}
public int getBranchCnt(){
      return condStatCnt;
}
public int getCommonCnt(){return regStatCnt;}
public void setSource(String source) {
      this.source = source;
}

public boolean isCompleted() {
      return isCompleted;
}

private void setLastStatement(Statement last) {
      if (last == null) {
	    lastStatement = "";
      } else {
	    StringBuilder builder = new StringBuilder();
	    if (last.getSelf() != null) {
		  Stream.of(last.getSelf())
		      .forEach(token -> builder.append(token.getValue()));
	    }
	    lastStatement = builder.toString();
      }
}

public String getLastStatement() {
      return lastStatement;
}

public void start() {
      Parser parser = new Parser(source);
      parser.parse();
      List<Statement> statements = parser.getStatements();
      condStatCnt = 0;
      regStatCnt = 0;
      maxNestLevel = 0;
      setLastStatement(parser.getLastStatement());
      if (statements == null) {
	    isCompleted = false;
      }
      else {
	    isCompleted = true;
	    for (Statement piece : statements) {
		  dispatchStatement(piece, 0);
	    }
      }

}
}
