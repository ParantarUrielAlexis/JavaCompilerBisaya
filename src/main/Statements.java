package main;

import java.util.List;

abstract class Statements {
    interface Visitor<R> {
        R visitBlockStmt(Block stmt);

        R visitExpressionStmt(Expression stmt);

        R visitIfStmt(If stmt);

        R visitPrintStmt(Print stmt);

        R visitScanStmt(Scan stmt);

        R visitForStmt(For stmt);

        R visitIntStmt(Int stmt);

        R visitFloatStmt(Float stmt);

        R visitCharStmt(Char stmt);

        R visitBoolStmt(Bool stmt);
    }

    static class Block extends Statements {
        Block(List<Statements> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }

        final List<Statements> statements;
    }

    static class Expression extends Statements {
        Expression(main.Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        final main.Expression expression;
    }

    static class If extends Statements {
        If(main.Expression condition, List<Statements> thenBranch, List<main.Expression> elseIfConditions, List<List<Statements>> elseIfBranches,
           List<Statements> elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseIfConditions = elseIfConditions;
            this.elseIfBranches = elseIfBranches;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }

        final main.Expression condition;
        final List<Statements> thenBranch;
        final List<main.Expression> elseIfConditions;
        final List<List<Statements>> elseIfBranches;
        final List<Statements> elseBranch;
    }

    static class Print extends Statements {
        Print(main.Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }

        final main.Expression expression;
    }


    static class Scan extends Statements {
        Scan(List<Token> identifiers) {
            this.identifiers = identifiers;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitScanStmt(this);
        }

        final List<Token> identifiers;
    }

    static class For extends Statements {
        For(Statements initializer, main.Expression condition, main.Expression increment, List<Statements> body) {
            this.initializer = initializer;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }

        final Statements initializer;
        final main.Expression condition;
        final main.Expression increment;
        final List<Statements> body;
    }


    static class Int extends Statements {
        Int(Token name, main.Expression initializer, boolean mutable) {
            this.name = name;
            this.initializer = initializer;
            this.mutable = mutable;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIntStmt(this);
        }

        final Token name;
        final main.Expression initializer;
        final boolean mutable;
    }

    static class Float extends Statements {
        Float(Token name, main.Expression initializer, boolean mutable) {
            this.name = name;
            this.initializer = initializer;
            this.mutable = mutable;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFloatStmt(this);
        }

        final Token name;
        final main.Expression initializer;
        final boolean mutable;
    }

    static class Char extends Statements {
        Char(Token name, main.Expression initializer, boolean mutable) {
            this.name = name;
            this.initializer = initializer;
            this.mutable = mutable;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCharStmt(this);
        }

        final Token name;
        final main.Expression initializer;
        final boolean mutable;
    }

    static class Bool extends Statements {
        Bool(Token name, main.Expression initializer, boolean mutable) {
            this.name = name;
            this.initializer = initializer;
            this.mutable = mutable;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBoolStmt(this);
        }

        final Token name;
        final main.Expression initializer;
        final boolean mutable;
    }

    abstract <R> R accept(Visitor<R> visitor);
}