package main;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
//         }
        //
        //
        // throw new RuntimeError(name, "WLA NA DEFINE PRE '" + name.lexeme + "'.");
        // }
    // }
    // }

    // static class Var {
    //     private final TokenType type;
    //     private final Object value;

abstract class Expression {
    interface Visitor<R> {

        R expressBinary(Binary expr);
        R expressAssignment(Assign expr);
        // R expressBlock(Block expr);
        R expressLiteral(Literal expr);

        // change to Logical
        R expressLogic(Logical expr);
        R expressVariable(Variable expr);

        R expressCall(Call expr);
        R expressUnary(Unary expr);



        R expressGrouping(Grouping expr);

        // R expressScan(Scan expr);
    }

//    // static class Block extends Expression {
//        Block(List<Stmt> statements) {
//            this.statements = statements;
//        }
//
//        @Override
//        <R> R accept(Visitor<R> visitor) {
//            return visitor.expressBlock(this);
//        }
//
//        final List<Stmt> statements;
//    }


    static class Logical extends Expression {
        Logical(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.expressLogic(this);
        }

        final Expression left;
        final Token operator;
        final Expression right;
    }

    static class Binary extends Expression {
        Binary(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.expressBinary(this);
        }

        final Expression left;
        final Token operator;
        final Expression right;
    }

    static class Assign extends Expression {
        Assign(Token name, Expression value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.expressAssignment(this);
        }

        final Token name;
        final Expression value;
    }

    static class Call extends Expression {
        Call(Expression callee, Token paren, List<Expression> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.expressCall(this);
        }

        final Expression callee;
        final Token paren;
        final List<Expression> arguments;
    }

    static class Grouping extends Expression {
        Grouping(Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.expressGrouping(this);
        }

        final Expression expression;
    }

    // static class Scan extends Expression {
    //     Scan(List<Token> identifiers) {
    //         this.identifiers = identifiers;
    //     }
    //     @Override
    //     <R> R accept(Visitor<R> visitor) {
    //         return visitor.expressScan(this);
    //     }
    //     final List<Token> identifiers;
    // }


    static class Literal extends Expression {
        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.expressLiteral(this);
        }

        final Object value;
    }



    static class Unary extends Expression {
        Unary(Token operator, Expression right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.expressUnary(this);
        }

        final Token operator;
        final Expression right;
    }

    static class Variable extends Expression {
        Variable(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.expressVariable(this);
        }

        final Token name;
    }

    abstract <R> R accept(Visitor<R> visitor);
}
