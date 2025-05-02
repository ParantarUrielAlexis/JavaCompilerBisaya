package main;

import java.util.HashMap;
import java.util.Map;

public class Env {
    final Env enclosing;
    private final Map<String, Var> values = new HashMap<>();

    Env() {
        enclosing = null;
    }

    Env(Env enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value, TokenType type, boolean isImmutable) {
        if (values.containsKey(name)) {
            throw new RuntimeError(new Token(type, name, type, 0),
                    "Variable '" + name + "' NA DEFINE NA ANG SCOPE.");
        }

        Env current = this.enclosing;
        while (current != null) {
            if (current.values.containsKey(name)) {
                throw new RuntimeError(new Token(type, name, type, 0),
                        "Variable '" + name + "' OUTER SCOPE NAAY SHADOW.");
            }
            current = current.enclosing;
        }
        values.put(name, new Var(type, value, isImmutable));
    }

    void define(String name, Object value) {
        values.put(name, new Var(null, value, true));
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme).getValue();
        }

        if (enclosing != null)
            return enclosing.get(name);

        throw new RuntimeError(name, "WLA NA DEFINE PRE '" + name.lexeme + "'.");
    }

    TokenType getType(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme).getType();
        }

        throw new RuntimeError(name, "WLA NA DEFINE PRE '" + name.lexeme + "'.");
    }

    Boolean getMutability(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme).isMutable();
        }

        throw new RuntimeError(name, "WLA NA DEFINE PRE '" + name.lexeme + "'.");
    }

    @SuppressWarnings("incomplete-switch")
    void assign(Token name, Object value) {
        Env env = this;
        while (env != null) {
            if (env.values.containsKey(name.lexeme)) {

                Var existingVar = env.values.get(name.lexeme);

                if (!existingVar.isMutable()) {
                    throw new RuntimeError(name, "DILI MA ASSIGN ANG IMMUTABLE VARIABLE '" + name.lexeme + "'.");
                }

                TokenType expectedType = existingVar.getType();

                boolean typeMatch = false;
                if (expectedType == TokenType.INT && value instanceof Integer) {
                    typeMatch = true;
                } else if (expectedType == TokenType.FLOAT && value instanceof Double) {
                    typeMatch = true;
                } else if (expectedType == TokenType.CHAR && value instanceof Character) {
                    typeMatch = true;
                } else if (expectedType == TokenType.STRING && value instanceof String) {
                    typeMatch = true;
                } else if (expectedType == TokenType.BOOL && value instanceof Boolean) {
                    typeMatch = true;
                }

                if (!typeMatch) {

                    String valueStr = value == null ? "null" : value.toString();
                    if (value instanceof Boolean) valueStr = valueStr.toUpperCase();
                    if (value instanceof String) valueStr = "\"" + valueStr + "\"";
                    if (value instanceof Character) valueStr = "'" + valueStr + "'";

                    throw new RuntimeError(name,
                            "MISMATCH ANG TYPE: DILI MA ASSIGN value " + valueStr +
                                    " (" + (value == null ? "Null" : value.getClass().getSimpleName()) + ")" +
                                    " to variable '" + name.lexeme + "' of type " + expectedType + ".");
                }

                env.values.put(name.lexeme, new Var(expectedType, value, true)); // Keep mutability true
                return;
            }
            env = env.enclosing;
        }

        throw new RuntimeError(name, "WLA NA DEFINE PRE '" + name.lexeme + "' PAG ATTEMPT SA PAG ASSIGN.");
    }
}