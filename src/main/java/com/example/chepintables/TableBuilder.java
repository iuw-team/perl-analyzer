package com.example.chepintables;

import com.example.parser.Parser;
import com.example.parser.Statement;
import com.example.parser.Token;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.example.parser.Token.*;

public class TableBuilder {
public enum TokenGroup {Param, Modifiable, Control, Temp}

private static class TokenState {
      private final String self;
      private TokenGroup group;
      private boolean isOutput;

      public TokenState(String token, TokenGroup group) {
	    this.self = token;
	    this.group = group;
	    this.isOutput = false; //was set yet
      }

      /**
       * It's impossible to change outputState lower then output
       * Once was be used in Output session, the output state is saved permanently
       */
      public void setOutputState(boolean isOutput) {
	    if (!this.isOutput) //why is not true proceed to set
		  this.isOutput = isOutput;
      }

      public boolean isOutput() {
	    return isOutput;
      }

      public TokenGroup group() {
	    return this.group;
      }

      private void setGroup(TokenGroup group) {
	    this.group = group;
      }

      @Override
      public boolean equals(Object object) {
	    if (object instanceof TokenState) {
		  return ((TokenState) object).self.equals(this.self);
	    }
	    return false;
      }
}

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


private List<String> extractVariables(@NotNull String innerString) {
      Pattern pattern = Pattern.compile("(\\\\)?\\$[a-zA-Z_][a-zA-Z_0-9]*");
      Matcher matcher = pattern.matcher(innerString);
      List<String> tokens = new ArrayList<>();
      while (matcher.find()) {
	    tokens.add(matcher.group());
      }
      return tokens.stream()
		 .filter(t -> !t.contains("\\$"))
		 .toList();
}

private Map<String, Integer> spenMap;
private Map<String, TokenState> stateMap;

private void setSpen(String token) {
      Integer spen = spenMap.getOrDefault(token, 0) + 1;
      spenMap.put(token, spen);
}

private boolean isUpdate(TokenGroup old, TokenGroup upd) {
      if (old == upd)
	    return false;
      return (upd == TokenGroup.Control || (upd == TokenGroup.Modifiable && old != TokenGroup.Control));
}

/**
 * Each token by default has NO_OUTPUT_STATUS<br>
 * This function mark token is OUTPUT
 *
 * @return -> the token status was changed or not
 */
private boolean setIOStatus(String token) {
      TokenState state = stateMap.get(token);
      if (state != null) {
	    state.setOutputState(true);
      }
      return token != null;
}

private void markVariable(String token, TokenGroup group) {
      setSpen(token);
      TokenState state = stateMap.getOrDefault(token, new TokenState(token, group));
      if (isUpdate(state.group(), group)) {
	    state.setGroup(group);
      }
      if (group == TokenGroup.Param && state.group() == TokenGroup.Temp)
	    state.setGroup(TokenGroup.Modifiable);
      stateMap.put(token, state);
}

private void markInnerString(Token innerString, boolean ioStatus) {
      extractVariables(innerString.getValue())
	  .forEach(t -> {
		markVariable(t, TokenGroup.Param);
		if (ioStatus)
		      setIOStatus(t);
	  });
}

private void markTokens(@NotNull Token[] tokens, int offset, int length, TokenGroup group, boolean ioStatus) {
      length = Math.min(tokens.length, offset + length);
      for (int i = offset; i < length; i++) {
	    var token = tokens[i];
	    if (token.getType() == Type.Variable) {
		  markVariable(token.getValue(), group);
		  if (ioStatus)
			setIOStatus(token.getValue());
	    } else if (token.getType() == Type.StringInner) {
		  markInnerString(token, ioStatus); //if variable is used in innerString
		  //that means that this variable is used as output
		  //By other words, to put data to other destination
	    }
      }
}

/**
 * @return first index of token
 * if token is absent ― -1 is return
 */
private int indexOf(Token[] tokens, int offset, Type searchType) {
      int index = 0;
      while (index < tokens.length && tokens[index].getType() != searchType) {
	    index += 1;
      }
      if (index == tokens.length)
	    index = -1;
      return index;
}

//tokens are used in output functions
private boolean isOutputStatement(Token[] tokens, int offset) {
      final List<String> outputList = List.of("print", "read");
      int index = indexOf(tokens, offset, Type.FloatWord);
      boolean result = false;
      if(index != -1){
	    result = outputList.contains(tokens[index].getValue());
      }
      return result;
}

//tokens are used in IO statement that means that tokens are IO
//return Assignable Token
//assumption that it's directly assignment
private boolean isIOAssignment(Token[] tokens, int offset) {
      boolean isFound = false;
      for (int i = offset; i < tokens.length && !isFound; i++) {
	    isFound = tokens[i].getType() == Type.BracketTriangle;
      }
      return isFound;
}

private void markLineStatement(Token[] tokens) {
      boolean isRValue = false;
      int i;
      for (i = 0; i < tokens.length && !isRValue; i++) {
	    if (tokens[i].getType() == Type.Assignment) {
		  TokenGroup assignableGroup = TokenGroup.Temp;
		  boolean ioStatus = false;
		  if (isIOAssignment(tokens, i + 1)) {
			assignableGroup = TokenGroup.Param;
			ioStatus = true;
		  }
		  if (tokens[i - 1].getType() == Type.BracketVarGroup) {//it's var enum
			int j = i - 1;
			while (tokens[j].getType() != Type.BracketVarGroup)
			      j -= 1;
			j += 1; //to params offset
			markTokens(tokens, j, i - j, assignableGroup, ioStatus);
		  } else {
			markVariable(tokens[i - 1].getValue(), assignableGroup); //it's single variable in assignment
			if (ioStatus)
			      setIOStatus(tokens[i - 1].getValue());
		  }
		  isRValue = true;
	    } else if (tokens[i].getType() == Type.ComplexAssignment) {
		  markVariable(tokens[i - 1].getValue(), TokenGroup.Modifiable);
		  isRValue = true;
	    }
      }
      if (!isRValue)
	    i = 0;
      boolean ioStatus = isOutputStatement(tokens, i);
      markTokens(tokens, i, tokens.length - i, TokenGroup.Param, ioStatus);
}

private void markForStatement(Token[] tokens) {
      int stage = 0;
      for (int i = 0; i < tokens.length; i++) {
	    if (tokens[i].getType() == Type.Separator) {
		  stage += 1;
		  continue;
	    }
	    if (tokens[i].getType() == Type.Variable && stage == 1) {
		  int j = i + 1;
		  while (tokens[j].getType() != Type.Separator)
			j += 1;
		  markTokens(tokens, i, j - i, TokenGroup.Param, false);
		  i = j - 1;
		  continue;
	    }
	    if (tokens[i].getType() == Type.Variable && (stage == 0 || stage == 2)) {
		  int j = i + 1;
		  Type tokenType;
		  while ((tokenType = tokens[j].getType()) != Type.Separator && tokenType != Type.Coma && tokenType != Type.BracketCircle) {
			j += 1;
		  }
		  var subTokens = Arrays.copyOfRange(tokens, i, j);
		  markLineStatement(subTokens);
		  i = j - 1;//
	    }
      }
}

private void dispatchStatement(Statement piece) {
      switch (piece.type()) {
	    case For -> markForStatement(piece.getSelf());
	    case While, If, Elsif, Until, Foreach ->
		markTokens(piece.getSelf(), 0, piece.getSelf().length, TokenGroup.Control, false);
	    case Line -> markLineStatement(piece.getSelf());
      }
      for (var child : piece.children())
	    dispatchStatement(child);
}

public Map<String, Integer> getSpenMap() {
      Map<String, Integer> copy = new HashMap<>(spenMap);
      copy.forEach((k, v) -> copy.put(k, v - 1));
      return copy;
}

public Integer getSpenSum() {
      Integer bufferSum = 0;
      for (var entry : getSpenMap().entrySet()) {
	    bufferSum += entry.getValue();
      }
      return bufferSum;
}


public Map<String, TokenGroup> getFullChepinMap() {
      Map<String, TokenGroup> map = new HashMap<>();
      stateMap.forEach((key, value) -> map.put(key, value.group()));
      return map;
}

public Map<String, TokenGroup> getIOChepinMap() {
      Map<String, TokenGroup> map = new HashMap<>();
      stateMap.forEach((key, value) -> {
	    if (value.isOutput)
		  map.put(key, value.group());
      });
      return map;
}

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
      spenMap = new HashMap<>();
      stateMap = new HashMap<>();
      setLastStatement(parser.getLastStatement());
      if (statements == null) {
	    isCompleted = false;
      } else {
	    isCompleted = true;
	    for (Statement piece : statements) {
		  dispatchStatement(piece);
	    }
	    for (Map.Entry<String, TokenState> entry : stateMap.entrySet()) {
		  System.out.format("%s -> %s\n", entry.getKey(), entry.getValue().group.toString());
	    }
      }

}
}
