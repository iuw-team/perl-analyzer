package com.example.hasltedtables;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.example.hasltedtables.Token.Type;

public class Parser {
    private Tokenizer tokenStream;
    private List<Statement> statements;
    private final String source;
    private List<Statement> freeStatements;
    public static final int CircleBrackets = 0;
    public static final int CurlyBrackets = 1;
    public static final int OpenCircle = 0;
    public static final int ClosedCircle = 1;
    public static final int OpenCurly = 2;
    public static final int ClosedCurly = 3;
    public static final int OpenSquare = 4;
    public static final int ClosedSquare = 5;
    public static final int OpenTriangle = 6;
    public static final int CloseTriangle = 7;
    public static final int OpenHash = 8;
    public static final int CloseHash = 9;
    public static final int VarScalar = 0;
    public static final int VarArray = 1;
    public static final int VarHash = 2;
    Parser(String source){
        this.source = source;
    }
    public void parse(){
        tokenStream = new Tokenizer(this.source);
        statements = null;
        build();
    }
    public List<Statement> getStatements(){
        return statements; //changed by Constructor invoking
    }
    private boolean isSeparator(){
        if(!tokenStream.hasNext())
            return false;
        return (tokenStream.nextToken().getType() == Type.Separator);
    }
    private boolean isComa(){
        if(!tokenStream.hasNext())
            return false;
        return (tokenStream.nextToken().getType() == Type.Coma);
    }
    private boolean isLambdaArrow(){
        if(!tokenStream.hasNext())
            return false;
        return (tokenStream.nextToken()).getType() == Type.ArrowLambda;
    }
    private boolean isPointerArrow(){
        if(!tokenStream.hasNext())
            return false;
        return (tokenStream.nextToken()).getType() == Type.ArrowLink;
    }
    private boolean isKeyword(String value){
        if(!tokenStream.hasNext())
            return false;
        Token token = tokenStream.nextToken();
        return (token.getType() == Type.Keyword) && token.getValue().equals(value);
    }
    private boolean isFloatWord(){
        if(!tokenStream.hasNext())
            return false;
        return (tokenStream.nextToken().getType() == Type.FloatWord);
    }
    private boolean isVariable(){
        if(!tokenStream.hasNext())
            return false;
        return tokenStream.nextToken().getType() == Type.Variable;
    }
    private boolean isEqualType(Type type){
        if(!tokenStream.hasNext())
            return false;
        return (tokenStream.nextToken().getType() == type);
    }

    //idempotent calling
    private boolean isBracket(int id){
        if(!tokenStream.hasNext())
            return false;
        tokenStream.freeze();
        Token token = tokenStream.nextToken();
        Token.Type type = token.getType();
        boolean result = false;
        switch (id){
            case OpenCircle -> result = (type == Type.BracketCircle) && (token.getValue().equals("("));
            case ClosedCircle -> result = (token.getType() == Type.BracketCircle) && (token.getValue().equals(")"));
            case OpenCurly -> result = (type == Type.BracketCurly) && (token.getValue().equals("{"));
            case ClosedCurly -> result = (type == Type.BracketCurly) && (token.getValue().equals("}"));
            case OpenSquare -> result = (type == Type.BracketSquare) && (token.getValue().equals("["));
            case ClosedSquare -> result = (type == Type.BracketSquare) && (token.getValue().equals("]"));
            case OpenTriangle -> result = (type == Type.Comparing || type == Type.BracketTriangle) && (token.getValue().equals("<"));
            case CloseTriangle -> result = (type == Type.Comparing || type == Type.BracketTriangle) && (token.getValue().equals(">"));
            case OpenHash ->  result = (type == Type.BracketCurly || type == Type.HashBrackets) && (token.getValue().equals("{"));
            case CloseHash ->  result = (type == Type.BracketCurly || type == Type.HashBrackets) && (token.getValue().equals("}"));
        }
        if(!result)
            tokenStream.release();
        else
            tokenStream.boost();
        return result;
    }
    private boolean isAssignment(){
        tokenStream.freeze();
        boolean result = isAssignable() && tokenStream.hasNext();
        if(result){
            Token token = tokenStream.nextToken();
            result = ((token.getType() == Type.Assignment) || (token.getType() == Type.ComplexAssignment)) &&
                             (isExpression() || isInitialization());
        }
        if(!result)
            tokenStream.release();
        else
            tokenStream.boost();
        return result;
    }

    private boolean isAssignable(){
        return isExpression();
    }
    private boolean isArrayInitialization(int bracketType){
        tokenStream.freeze();
        boolean result = false;
        switch(bracketType){
            case CircleBrackets -> result = isBracket(OpenCircle) && isFuncParams() && isBracket(ClosedCircle);
            case CurlyBrackets -> result = isBracket(OpenCurly) && isFuncParams() && isBracket(ClosedCurly);
        }
        if(result)
            tokenStream.boost();
        else
            tokenStream.release();
        return result;
    }
    private boolean isTableEntries(){
        Token hashKey;
        if(!tokenStream.hasNext())
            return true;

        tokenStream.freeze();
        hashKey = tokenStream.nextToken();
        if(hashKey.getType() !=  Type.HashKey && hashKey.getType() != Type.FloatWord){
            tokenStream.release();
            return true;
        }
        boolean isEntries = isLambdaArrow() && (isInitialization() || isExpression()) && isComa() && isTableEntries();
        if(!isEntries){
            tokenStream.refresh();
            isFloatWord();//isFloatWord is dummy invoke to skip token
            isEntries = isLambdaArrow() && (isInitialization() || isExpression());
        }
        if(isEntries){
            hashKey.setType(Type.HashKey);
            tokenStream.boost();
        }
        else
            tokenStream.release();
        return true;
    }
    private boolean isTableInitialization(){
        tokenStream.freeze();
        boolean isHeader = isBracket(OpenCurly) && isTableEntries() && isBracket(ClosedCurly);
        if(!isHeader){
            tokenStream.refresh();
            isHeader = isBracket(OpenCircle) && isTableEntries() && isBracket(OpenCircle);
        }
        if(isHeader)
            tokenStream.boost();
        else
            tokenStream.release();
        return isHeader;
    }
    private boolean isInitialization(){
        return isTableInitialization() || isArrayInitialization(CircleBrackets) || isArrayInitialization(CurlyBrackets);
    }
    private boolean isStorageClass(){
        tokenStream.freeze();
        boolean isCorrect = isKeyword("my") ||
                            refreshStream() && isKeyword("our") ||
                            refreshStream() && isKeyword("state");
        if(isCorrect)
            tokenStream.boost();
        else
            tokenStream.release();
        return isCorrect;
    }
    private boolean isDeclaration(){
        tokenStream.freeze();
        boolean result = isStorageClass() && isVariable() && tokenStream.hasNext();
        if(result){
            tokenStream.freeze();
            Token token = tokenStream.nextToken();
            result = ((token.getType() == Type.Assignment) && (isExpression() || isInitialization()));
            if(!result){
                tokenStream.release();
                result = true;
            }
            else {
                tokenStream.boost();
            }
        }
        if(!result)
            tokenStream.release();
        else
            tokenStream.boost();
        return result;
    }

    private boolean isPointerAccess(){
        tokenStream.freeze();
        boolean isPointer = isVariable() && isPointerArrow() && isExpression(); //todo: replace with correct expression
//        if(isPointer)
//            tokenStream.boost();
//        else
            tokenStream.release();
        return false;
    }
    private boolean isDataFlowExpr(){
        tokenStream.freeze();
        boolean isFile = isBracket(OpenTriangle) && isVariable() && isBracket(CloseTriangle);
        if(!isFile)
            isFile = refreshStream() && isBracket(OpenTriangle) && isFloatWord() && isBracket(CloseTriangle);
        if(isFile){
            tokenStream.refresh();
            tokenStream.nextToken().setType(Type.BracketTriangle);
            tokenStream.nextToken();
            tokenStream.nextToken().setType(Type.BracketTriangle);
            tokenStream.boost();
        }
        else
            tokenStream.release();
        return isFile;
    }
    private boolean isHashAccess(){
        tokenStream.freeze();
        boolean result = isVariable() && isBracket(OpenHash) && isExpression() && isBracket(CloseHash);
        if(!result)
            tokenStream.release();
        else{
            tokenStream.refresh();
            tokenStream.nextToken(1).setType(Type.HashBrackets);
            isExpression();
            tokenStream.nextToken().setType(Type.HashBrackets);
            tokenStream.boost();
        }
        return result;
    }
    private boolean isArrayAccess(){
        tokenStream.freeze();
        boolean result =  isVariable() && isBracket(OpenSquare) && isExpression() && isBracket(ClosedSquare);
        if(!result)
            tokenStream.release();
        else
            tokenStream.boost();
        return result;
    }
    private boolean isExpOperator(){
        if(!tokenStream.hasNext())
            return false;
        tokenStream.freeze();
        boolean result = false;
        var types = List.of(Type.StringCmp,
                    Type.Arithmetic,
                    Type.Logical,
                    Type.StringCat,
                    Type.StringRep,
                    Type.Comparing,
                    Type.ArrayRange);
        for(Token.Type type : types){
            tokenStream.refresh();
            result = isEqualType(type);
            if(result)
                break;
        }
        if(!result)
            tokenStream.release();
        else
            tokenStream.boost();
        return result;
    }
    private boolean isExpValue(){
        if(!tokenStream.hasNext())
            return false;
        tokenStream.freeze();
        boolean result = isFuncCall() || isHashAccess() || isArrayAccess() || isPointerAccess() || isDataFlowExpr() || isVariable(); //last is not idempotante
        if(!result){
            var types = List.of(Type.Digits, Type.StringInner, Type.StringPlain);
            for(Type type : types){
                tokenStream.refresh();
                result = isEqualType(type);
                if(result)
                    break;
            }
        }
        if (!result) {
            tokenStream.release();
        } else {
            tokenStream.boost();
        }
        return result;
    }

    private boolean refreshStream(){
        tokenStream.refresh();
        return true;
    }
    private boolean isExpression(){
        tokenStream.freeze();
        boolean result =
                isBracket(OpenCircle) && isExpression() && isBracket(ClosedCircle) && isExpOperator() && isExpression() ||
                refreshStream() && isBracket(OpenCircle) && isExpression() && isBracket(ClosedCircle) ||
                refreshStream() && isExpValue() && isExpOperator() && isExpression() ||
                refreshStream() && isExpValue();
        if(!result)
            tokenStream.release();
        else
            tokenStream.boost();
        return result;
    }
    private boolean isFuncParams(){
        tokenStream.freeze();
        boolean result = (isExpression() || isInitialization() || isDeclaration()) && isComa() && isFuncParams();
        if(!result){
            tokenStream.refresh();
            result = (isExpression() || isInitialization());
        }
        if(!result)
            tokenStream.release();
        else
            tokenStream.boost();
        return true;
    }
    private boolean isFuncCall(){
        tokenStream.freeze();
        boolean result = (isFloatWord() && isBracket(OpenCircle) && isFuncParams() && isBracket(ClosedCircle));
        if(!result){
            tokenStream.refresh();
            result = isFloatWord() && isFuncParams();
        }
        if(!result)
            tokenStream.release();
        else {
            tokenStream.boost();
        }
        return result;
    }
    /**
     * keyword that has impact to other construction in language
     * eg. next, redo, last
     * */
    private boolean isFlowKeyword(){
        Token flowKeyword;
        String keyValue;
        tokenStream.freeze();
        if(!tokenStream.hasNext() || (flowKeyword = tokenStream.nextToken()).getType() != Type.FlowKeyword){
            tokenStream.release();
            return false;
        }
        keyValue = flowKeyword.getValue();
        boolean isFlow = keyValue.equals("last") ||
                         keyValue.equals("next") ||
                         keyValue.equals("redo") ||
                         keyValue.equals("return") && isExpression();
                                 //todo: replace isExpression to FuncParams
                                 //todo: change FuncParams to check brackets
        if(isFlow)
            tokenStream.boost();
        else
            tokenStream.release();
        return isFlow;
    }
    private boolean isUntilStatement(){
        return isCondBodyStatement(Statement.Type.Until, "until");
    }
    private boolean isWhileStatement(){
        return isCondBodyStatement(Statement.Type.While, "while");
    }
    /**if freeStatements are not null add to `statement`'s children all freeStatements
     * new freeStatements will be holds only this statement
     * */
    private void updateStatements(Statement statement){
        List<Statement> list = new ArrayList<>(1);
        if(freeStatements != null) {
            statement.addAll(freeStatements);
        }
        list.add(statement);
        freeStatements = list;
    }
    private void resetStatements(){
        freeStatements = null;
    }
    private boolean isForStatement(){
        return isVCondBodyStatement(Statement.Type.For, o-> {
            return (isKeyword("for") && isBracket(OpenCircle) &&
                    (isDeclaration() || isAssignment()) && isSeparator() &&
                    isExpression() && isSeparator() && isAssignment() && isBracket(ClosedCircle));
        });
    }
    private boolean isFuncDeclaration(){
        return isVCondBodyStatement(Statement.Type.Function, o -> isKeyword("sub") && isFloatWord());
    }
    private boolean isLineStatement(){
        return isSingleStatement(Statement.Type.Line, o->{
            if (!isAssignment() && !isDeclaration() && !isExpression() && !isFuncCall()) {
                isFlowKeyword();
            }
            return isSeparator();
        });
    }
    private boolean isForeachStatement(){
        return isVCondBodyStatement(Statement.Type.Foreach, o->{
            boolean isHeader = isKeyword("foreach");
            if(isHeader){
                tokenStream.freeze();
                isHeader = (isStorageClass() && isVariable()) ||
                           (refreshStream() && isVariable());
                if(isHeader)
                    tokenStream.boost();
                else
                    tokenStream.release();
                isHeader = true;
            }
            isHeader = isHeader && isBracket(OpenCircle) && (isFuncParams() || isExpression()) && isBracket(ClosedCircle);
            return isHeader;
        });
    }
    /**
     * @param cond (Object is always null)
     */
    private boolean isVCondBodyStatement(Statement.Type type, Function<Object, Boolean> cond){
        int start = tokenStream.freeze();
        int end;
        boolean isValid = cond.apply(null);
        Statement self;
        if(!isValid){
            tokenStream.release();
            freeStatements = null;
            return false;
        }
        end = tokenStream.mark();
        isValid = isBody();
        if(!isValid){
            tokenStream.release();
            //freeStatements are null yet
        }
        else {
            self = new Statement(type, tokenStream.getRange(start, end));
            updateStatements(self);
            tokenStream.boost();
        }
        return isValid;
    }
    private boolean isCondBodyStatement(Statement.Type type, String keyword){
        int start = tokenStream.freeze();
        int end;
        boolean isStatement = isKeyword(keyword) && isBracket(OpenCircle) &&
                              isExpression() && isBracket(ClosedCircle);
        if(!isStatement){
            tokenStream.release();
            freeStatements = null; //freeStatements should be always null after unlucky statement construction
            return false;
        }
        end = tokenStream.mark();
        isStatement = isBody();
        if(!isStatement){
            tokenStream.release();
            //after isBody freeStatements is null
        }
        else {
            Statement self = new Statement(type, tokenStream.getRange(start, end));
            updateStatements(self);
            tokenStream.boost();
        }
        return isStatement;
    }
    private boolean isSingleStatement(Statement.Type type, Function<Object, Boolean> cond){
        int start = tokenStream.freeze();
        int end;
        boolean isValid = cond.apply(null);
        resetStatements();
        if(!isValid){
            tokenStream.release();
        }
        else {
            end = tokenStream.mark();
            var self = new Statement(type, tokenStream.getRange(start, end));
            updateStatements(self);
            tokenStream.boost();
        }
        return isValid;
    }
    private boolean isIfHeader(){
        return isCondBodyStatement(Statement.Type.If, "if");
    }
    private boolean isElifPart(){
        boolean isElsif = isCondBodyStatement(Statement.Type.Elsif, "elsif");
        if(isElsif){
            List<Statement> saved = freeStatements;
            isElifPart();
            if(freeStatements != null)
                saved.addAll(freeStatements);
            freeStatements = saved;
        }
        return true;
    }
    private boolean isElsePart(){
        int start = tokenStream.freeze();
        boolean isElse = isKeyword("else");
        if(!isElse){
            tokenStream.release();
            freeStatements = null;
            return true;// always return true
        }
        int end = tokenStream.mark();
        isElse = isBody();
        if (!isElse) {
            tokenStream.release();
        } else {
            Statement self = new Statement(Statement.Type.Else, tokenStream.getRange(start, end));
            updateStatements(self);
            tokenStream.boost();
        }
        return true;
    }
    private boolean isIfStatement(){
        Statement self;
        boolean isCommonIf = isIfHeader();
        if (isCommonIf) {
            self = new Statement(Statement.Type.CommonIf, null);
            self.add(freeStatements.get(0));
            if(isElifPart() && (freeStatements != null))
                self.addAll(freeStatements);
            if(isElsePart() && (freeStatements != null))
                self.addAll(freeStatements);
            resetStatements();
            updateStatements(self);
        }
        return isCommonIf;
    }
    private boolean isImport(){
        return isSingleStatement(Statement.Type.Import, o->{
            return isKeyword("use") && isFloatWord() && isSeparator();
        });
    }
    private boolean isStatements(){
        boolean hasStatement = (isIfStatement() || isUntilStatement() ||
                isWhileStatement() || isForStatement() || isForeachStatement() ||
                isLineStatement() || isFuncDeclaration() || isImport());
        if(hasStatement){
            List<Statement> group = new ArrayList<>(freeStatements);
            isStatements();
            if(freeStatements != null)
                group.addAll(freeStatements);
            freeStatements = group;
        }
        return true;
    }
    /**
     * can change tokenStream
     * */
    private boolean isBody(){
        int start = tokenStream.freeze();
        int end;
        Statement self;
        boolean hasBody;

        resetStatements();
        hasBody = isBracket(OpenCurly) && isStatements();
        if(!hasBody){
            tokenStream.refresh();
            hasBody = isSeparator();
            freeStatements = null;
            if(!hasBody) {
                tokenStream.release();
            }
            else {
                int index = tokenStream.mark();
                self = new Statement(Statement.Type.Body, tokenStream.getRange(index, index));
                updateStatements(self);
                tokenStream.boost();
            }
            return hasBody;
        }
        hasBody = isBracket(ClosedCurly);
        end = tokenStream.mark();
        if(!hasBody){
            freeStatements = null;
            tokenStream.release();
        }
        else {
            var tokens = List.of(tokenStream.getRange(start, start)[0],
                                 tokenStream.getRange(end, end)[0]);
            self = new Statement(Statement.Type.Body, tokens.toArray(new Token[0]));
            updateStatements(self);
            tokenStream.boost();
        }
        return hasBody;
    }
public @Nullable Statement getLastStatement(){
       if(statements == null) {
           return freeStatements != null ? freeStatements.get(freeStatements.size() - 1) : null;
       } else
           return statements.get(statements.size() - 1);
}
    private void build(){
//        while(tokenStream.hasNext()){
            resetStatements();
            isStatements();
            if(!tokenStream.hasNext())
                statements = freeStatements;

//            if(tokenStream.hasNext())
//                System.out.println("Syntax error!");
//            else
    }
}
