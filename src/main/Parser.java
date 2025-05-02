package main;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;
    private boolean afterVarDeclaration = false;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Statements> parse() {
        List<Statements> statements = new ArrayList<>();

        consume(TokenType.BEGIN, "SUGOD kuwang.");

        while (match(TokenType.STRING, TokenType.CHAR, TokenType.INT, TokenType.FLOAT, TokenType.BOOL,
                TokenType.DECLARATION)) {
            statements.addAll(varDeclaration());
        }

        while (!isAtEnd() && !check(TokenType.END)) {
            if (check(TokenType.STRING) || check(TokenType.CHAR) || check(TokenType.INT) || check(TokenType.FLOAT)
                    || check(TokenType.BOOL) || check(TokenType.DECLARATION)) {
                afterVarDeclaration = true;
            }
            statements.add(statement());
        }

        consume(TokenType.END, "Walay KATAPUSAN.");

        if (peek().type != TokenType.EOF) {
            if (peek().type == TokenType.BEGIN) {
                throw error(peek(),
                        "sugod wala ug KATAPUSAN blocks");
            } else {
                throw error(peek(),
                        "SUGOD and KATAPUSAN wa nakita.");
            }
        }

        return statements;
    }

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        Expression expression = or();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if (expression instanceof Expression.Variable) {
                Token name = ((Expression.Variable) expression).name;
                return new Expression.Assign(name, value);
            }

            throw error(equals, "Cannot assign to " + expression.getClass().getSimpleName() + ".");
        }

        return expression;
    }

    private Expression or() {
        Expression expression = and();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expression right = and();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression and() {
        Expression expression = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expression right = equality();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    private Statements statement() {
        if (match(TokenType.DECLARATION)) {
            return varDeclaration().get(0);
        }
        if (match(TokenType.DISPLAY)) {
            consume(TokenType.COLON, "WALAY ':' IPAKITA");
            return displayStatement();
        }

        if (match(TokenType.SCAN)) {
            consume(TokenType.COLON, "WALAY':' DAWAT");
            return scanStatement();
        }

        if (match(TokenType.IF)) {
            return ifStatement();
        }

        if (match(TokenType.FOR)) {
            consume(TokenType.THE, "NANGITA 'SA' PAGHUMAN'ALANG'.");
            return forStatement();
        }

        return expressionStatement();
    }

    private Statements displayStatement() {
        Expression value = or();
        return new Statements.Print(value);
    }

    private Statements scanStatement() {
        List<Token> identifiers = new ArrayList<>();

        do {
            identifiers.add(consume(TokenType.IDENTIFIER, "NANGITA identifier PAGHUMAN'dawat'."));
        } while (match(TokenType.COMMA));

        return new Statements.Scan(identifiers);
    }

    private Statements ifStatement() {
        //IF (WALAY KUNG)
        consume(TokenType.LEFT_PARENTHESIS, "NANGITA '(' PAGHUMANWALAY KUNG.");
        Expression condition = expression(); // Parse the main condition.
        consume(TokenType.RIGHT_PARENTHESIS, "NANGITA ')' PAGHUMANWALAY KUNG condition.");

        consume(TokenType.BLOCK, "NANGITA SA PUNDOK MIGO PAGHUMANWALAY KUNG condition.");
        consume(TokenType.LEFT_BRACE, "NANGITA '{' PAGHUMANSA PUNDOK MIGO.");

        List<Statements> thenBranch = block();

        // ELSE IF (DILI SAKTO)
        List<Expression> elseIfConditions = new ArrayList<>();
        List<List<Statements>> elseIfBranches = new ArrayList<>();

        while (check(TokenType.IF) && checkNext(TokenType.NOT)) {
            advance();
            advance();

            consume(TokenType.LEFT_PARENTHESIS, "NANGITA '(' dili kung wala");
            Expression elseIfCondition = expression(); // Parse the else if condition.
            elseIfConditions.add(elseIfCondition);
            consume(TokenType.RIGHT_PARENTHESIS, "NANGITA ')' PAGHUMANDILI SAKTO condition.");

            consume(TokenType.BLOCK, "NANGITA SA PUNDOK MIGO PAGHUMANDILI SAKTO condition.");
            consume(TokenType.LEFT_BRACE, "NANGITA '{' PAGHUMANSA PUNDOK MIGO.");

            List<Statements> elseIfBranch = block();
            elseIfBranches.add(elseIfBranch);
        }

        // ELSE (WALAY KUNG WALA)
        List<Statements> elseBranch = null;

        if (check(TokenType.IF) && checkNext(TokenType.ELSE)) {
            advance();
            advance();

            consume(TokenType.BLOCK, "NANGITA SA PUNDOK MIGO PAGHUMANWALAY KUNG WALA.");
            consume(TokenType.LEFT_BRACE, "NANGITA '{' PAGHUMANSA PUNDOK MIGO.");

            elseBranch = block();
        }

        return new Statements.If(condition, thenBranch, elseIfConditions, elseIfBranches, elseBranch);
    }

    private Statements forStatement() {
        consume(TokenType.LEFT_PARENTHESIS, "NANGITA '(' PAGHUMANALANG SA.");

        Statements initializer;
        if (match(TokenType.DECLARATION)) {
            initializer = varDeclaration().get(0);
        } else if (match(TokenType.IDENTIFIER)) {
            Token name = previous();
            consume(TokenType.EQUAL, "NANGITA '=' in initializer.");
            Expression value = expression();
            initializer = new Statements.Expression(new Expression.Assign(name, value));
        } else {
            throw error(peek(), "DILI SAKTO initializer in ALANG SA.");
        }

        consume(TokenType.COMMA, "NANGITA ',' PAGHUMANinitializer.");

        Expression condition = expression();
        consume(TokenType.COMMA, "NANGITA ',' PAGHUMANcondition.");

        Expression increment;
        if (match(TokenType.IDENTIFIER)) {
            Token name = previous();
            if (match(TokenType.PLUS_PLUS)) {
                increment = new Expression.Assign(
                        name,
                        new Expression.Binary(
                                new Expression.Variable(name),
                                new Token(TokenType.PLUS, "+", null, name.line),
                                new Expression.Literal(1)
                        )
                );
            } else if (match(TokenType.MINUS_MINUS)) {
                increment = new Expression.Assign(
                        name,
                        new Expression.Binary(
                                new Expression.Variable(name),
                                new Token(TokenType.MINUS, "-", null, name.line),
                                new Expression.Literal(1)
                        )
                );
            } else {
                throw error(peek(), "NANGITA variable in increment.");
            }
        } else {
            throw error(peek(), "NANGITA variable name in increment.");
        }

        consume(TokenType.RIGHT_PARENTHESIS, "NANGITA ')' PAGHUMANALANG SA clauses.");

        consume(TokenType.BLOCK, "NANGITA SA PUNDOK MIGO PAGHUMANALANG SA.");
        consume(TokenType.LEFT_BRACE, "NANGITA '{' PAGHUMANSA PUNDOK MIGO.");

        List<Statements> body = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            body.add(statement());
        }
        consume(TokenType.RIGHT_BRACE, "NANGITA '}' PAGHUMANSA PUNDOK MIGO block.");

        return new Statements.For(initializer, condition, increment, body);
    }

    private List<Statements> block() {
        List<Statements> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(statement());
        }
        consume(TokenType.RIGHT_BRACE, "NANGITA '}' PAGHUMANblock.");
        return statements;
    }

    private void debugPrintTokens(List<Token> tokens) {
        System.out.println("=== DEBUG: TOKENS ===");
        for (Token token : tokens) {
            System.out.println(token.type + " -> " + token.lexeme);
        }
        System.out.println("=====================");
    }

    private List<Statements> varDeclaration() {
        Token declaration = previous();
        Token token = previous();
        boolean mutable = true;
        List<Token> names = new ArrayList<>();
        List<Expression> initializers = new ArrayList<>();

        if (declaration.type == TokenType.DECLARATION) { // Check for "MUGNA"
            token = consume(peek().type, "NANGITA a variable type PAGHUMANMUGNA (DECLARATION).");
        } else {
           throw error(declaration, "NANGITA keyword MUGNA before variable declaration.");
        }

        do {
            Token name = consume(TokenType.IDENTIFIER, "NANGITA proper variable declaration.");
            names.add(name);
            Expression initializer = null;

            if (match(TokenType.EQUAL)) {
                initializer = expression();
            }

            initializers.add(initializer);
        } while (match(TokenType.COMMA));

        List<Statements> statements = new ArrayList<>();

        switch (token.type) {
            case CHAR:
                for (int i = 0; i < names.size(); i++) {
                    statements.add(new Statements.Char(names.get(i), initializers.get(i), mutable));
                }
                break;
            case INT:
                for (int i = 0; i < names.size(); i++) {
                    statements.add(new Statements.Int(names.get(i), initializers.get(i), mutable));
                }
                break;
            case FLOAT:
                for (int i = 0; i < names.size(); i++) {
                    statements.add(new Statements.Float(names.get(i), initializers.get(i), mutable));
                }
                break;
            case BOOL:
                for (int i = 0; i < names.size(); i++) {
                    statements.add(new Statements.Bool(names.get(i), initializers.get(i), mutable));
                }
                break;
            default:
                throw error(declaration, "WALAY SUPORTA variable type.");
        }

        return statements;
    }

    private Statements expressionStatement() {
        Expression expression = expression();
        return new Statements.Expression(expression);
    }

    private Expression equality() {
        Expression expression = comparison();
        while (match(TokenType.NOT_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expression = new Expression.Binary(expression, operator, right);
        }
        return expression;
    }

    private Expression comparison() {
        Expression expression = term();
        while (match(TokenType.GREATER_THAN, TokenType.GREATER_THAN_EQUAL, TokenType.LESS_THAN,
                TokenType.LESS_THAN_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression term() {
        Expression expression = factor();

        while (match(TokenType.MINUS, TokenType.PLUS, TokenType.AMPERSAND)) {
            Token operator = previous();
            Expression right = factor();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression factor() {
        Expression expression = unary();
        while (match(TokenType.SLASH, TokenType.STAR, TokenType.MODULO)) {
            Token operator = previous();
            Expression right = unary();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression unary() {
        if (match(TokenType.NOT, TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Expression.Unary(operator, right);
        }

        return call();
    }

    private Expression call() {
        Expression expression = primary();

        if (match(TokenType.LEFT_PARENTHESIS)) {
            expression = finishCall(expression);
        }

        return expression;
    }

    private Expression finishCall(Expression callee) {
        List<Expression> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PARENTHESIS)) {
            do {
                if (arguments.size() >= 255) {
                    throw error(peek(), "MORE THAN 255 arguments, way klaro");
                }
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        Token rightParen = consume(TokenType.RIGHT_PARENTHESIS, "NANGITA a parenthesis PAGHUMANa function call.");
        return new Expression.Call(callee, rightParen, arguments);
    }

    private Expression primary() {
        if (match(TokenType.TRUE_LITERAL))
            return new Expression.Literal(true);
        if (match(TokenType.FALSE_LITERAL))
            return new Expression.Literal(false);
        if (match(TokenType.NULL))
            return new Expression.Literal(null);
        if (match(TokenType.STRING_LITERAL, TokenType.CHAR_LITERAL,
                TokenType.INT_LITERAL, TokenType.FLOAT_LITERAL, TokenType.DOLLAR_SIGN))
            return new Expression.Literal(previous().literal);
        if (match(TokenType.LEFT_PARENTHESIS)) {
            Expression expression = expression();
            consume(TokenType.RIGHT_PARENTHESIS, "NANGITA ')' PAGHUMANexpression");
            return new Expression.Grouping(expression);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Expression.Variable(previous());
        }

        String message = "NANGITA expression.";
        throw error(peek(), message);
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Main.error(token, message);
        return new ParseError();
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekNext() {
        return tokens.get(current + 1);
    }

    private Token advance() {
        if (!isAtEnd())
            current++;

        return previous();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).type == type;
    }
}
