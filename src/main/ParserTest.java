package main;

import java.util.ArrayList;
import java.util.List;

public class ParserTest {

    public static void main(String[] args) {

        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.BEGIN, "SUGOD", null, 1));
        tokens.add(new Token(TokenType.INT, "NUMERO", null, 2));
        tokens.add(new Token(TokenType.IDENTIFIER, "x", null, 2));
        tokens.add(new Token(TokenType.EQUAL, "=", null, 2));
        tokens.add(new Token(TokenType.INT_LITERAL, "10", 10, 2));
        tokens.add(new Token(TokenType.DISPLAY, "IPAKITA", null, 3));
        tokens.add(new Token(TokenType.COLON, ":", null, 3));
        tokens.add(new Token(TokenType.STRING_LITERAL, "\"Hello, World!\"", "Hello, World!", 3));
        tokens.add(new Token(TokenType.END, "KATAPUSAN", null, 4));
        tokens.add(new Token(TokenType.EOF, "", null, 5));


        Parser parser = new Parser(tokens);


        try {
            List<Statements> statements = parser.parse();
            System.out.println("Parsing successful! Parsed statements:");
            for (Statements stmt : statements) {
                System.out.println(stmt.getClass().getSimpleName());
            }
        } catch (Exception e) {
            System.err.println("Parsing failed: " + e.getMessage());
        }
    }
}