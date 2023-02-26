package com.example.hasltedtables;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.example.hasltedtables.Token.Type;

public class Parser {
    private Tokenizer tokenStream;
    private List<Statement> statements;
    private String source;
    private List<Statement> freeStatements;
    public static final int OpenCircle = 0;
    public static final int ClosedCircle = 1;
    public static final int OpenCurly = 2;
    public static final int ClosedCurly = 3;
    public static final int OpenSquare = 4;
    public static final int ClosedSquare = 5;
    public static final int VarScalar = 0;
    public static final int VarArray = 1;
    public static final int VarHash = 2;
    Parser(String source){
        this.source = source;
    }
    public void parse(){
        tokenStream = new Tokenizer(this.source);
        statements = new ArrayList<>();
        build();
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
        boolean result = false;
        switch (id){
            case OpenCircle ->{
                result = (token.getType() == Type.BracketCircle) && (token.getValue().equals("("));
            }
            case ClosedCircle -> {
                result = (token.getType() == Type.BracketCircle) && (token.getValue().equals(")"));
            }
            case OpenCurly -> {
                result = (token.getType() == Type.BracketCurly) && (token.getValue().equals("{"));
            }
            case ClosedCurly -> {
                result = (token.getType() == Type.BracketCurly) && (token.getValue().equals("}"));
            }
            case OpenSquare ->
                    result = (token.getType() == Type.BracketSquare) && (token.getValue().equals("["));
            case ClosedSquare ->
                    result = (token.getType() == Type.BracketSquare) && (token.getValue().equals("]"));
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
                     isExpression();
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
    private boolean isInitialization(){
        return false;
    }
    private boolean isDeclaration(){
        tokenStream.freeze();
        boolean result = isKeyword("my") && isVariable() && tokenStream.hasNext();
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
        return false;
    }
    private boolean isHashAccess(){
        tokenStream.freeze();
        boolean result =  isVariable() && isBracket(OpenCurly) && isExpression() && isBracket(ClosedCurly);
        if(!result)
            tokenStream.release();
        else
            tokenStream.boost();
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
                    Type.Comparing);
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
        boolean result = isFuncCall() || isHashAccess() || isArrayAccess() || isPointerAccess() || isVariable(); //last is not idempotante
        if(!result){
            var types = List.of(Type.Digits, Type.StringInner, Type.StringPlain);
            for(Type type : types){
                tokenStream.refresh();
                result = isEqualType(type);
                if(result)
                    break;
            }
        }
        if(!result)
            tokenStream.release();
        else
            tokenStream.boost();
        return result;
    }

    private boolean refreshStream(){
        tokenStream.refresh();
        return true;
    }
    private boolean isExpression(){
        tokenStream.freeze();
        boolean result = isExpValue() && isExpOperator() && isExpression() ||
                refreshStream() && isBracket(OpenCircle) && isExpression() && isBracket(ClosedCircle) && isExpOperator() && isExpression() ||
                refreshStream() && isBracket(OpenCircle) && isExpression() && isBracket(ClosedCircle) ||
                refreshStream() && isExpValue();
        if(!result)
            tokenStream.release();
        else
            tokenStream.boost();
        return result;
    }
    private boolean isFuncParams(){
        tokenStream.freeze();
        boolean result = (isExpression() || isInitialization()) && isComa() && isFuncParams();
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
//        int start = tokenStream.freeze();
//        int end;
//        boolean isForHeader =
//        if(!isForHeader){
//            tokenStream.release();
//            freeStatements = null;
//            return false;
//        }
//        end = tokenStream.mark();
//        isForHeader = isBody();
//        if(!isForHeader){
//            tokenStream.release();
//            //freeStatements is null
//        }
//        else {
//            Statement self = new Statement(Statement.Type.For, tokenStream.getRange(start, end));
//            updateStatements(self);
//            tokenStream.boost();
//        }
//        return isForHeader;
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
            return  (isAssignment() || isDeclaration() || isFuncCall() ||
                    isExpression()) &&  isSeparator();
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
        boolean hasStatement = (isIfStatement() || isUntilStatement()
                || isWhileStatement() || isForStatement() ||
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
    private void build(){
        resetStatements();
        isStatements();
        if(tokenStream.hasNext())
            System.out.println("Syntax error");
//        while(tokenStream.hasNext()){
//            isStatements();
//            if(tokenStream.hasNext())
//                tokenStream.nextToken();
//        }
    }
}
