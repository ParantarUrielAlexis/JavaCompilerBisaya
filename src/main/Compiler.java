package main;

import java.util.List;
import java.util.ArrayList;
import java.lang.String;

import main.Expression.Assign;
import main.Expression.Binary;
import main.Expression.Call;
import main.Expression.Grouping;
import main.Expression.Literal;
import main.Expression.Logical;
import main.Expression.Unary;
import main.Expression.Variable;
import main.Statements.Block;
import main.Statements.Bool;
import main.Statements.Char;
import main.Statements.Float;
import main.Statements.If;
import main.Statements.Int;
import main.Statements.Print;
import main.Statements.Scan;
import main.Statements.For;
import java.util.Scanner;
import java.util.stream.Collectors;

// import main.Statements.Statements;
// public class Statements {
//     interface Visitor<R> {
//         R visitBlockStmt(Block stmt);
//         R visitExpressionStmt(Expression stmt);
//         R visitIfStmt(If stmt);
//         R visitPrintStmt(Print stmt);
//         R visitScanStmt(Scan stmt);
//         R visitForStmt(For stmt);
//         R visitIntStmt(Int stmt);
//         R visitFloatStmt(Float stmt);
//         R visitCharStmt(Char stmt);
//         R visitBoolStmt(Bool stmt);
//     }

public class Compiler implements Expression.Visitor<Object>, Statements.Visitor<Object> {

    final Env globals = new Env();
    private Env env = globals;
    private boolean hasDisplay = false;

    public void interpret(List<Statements> statements) {
        try {
            for (Statements statement : statements) {
                execute(statement);
            }
            if (!hasDisplay) {
                System.out.println("No Error.");
            }
        } catch (RuntimeError error) {
            System.err.println("RUNTIME ERROR " + error.getMessage());
            Main.runtimeError(error); // Optional: if you still want Main to handle something special
        } catch (Exception e) {
            System.err.println("Error found in the code. Unexpected Error occurred.");
            e.printStackTrace();
        }
    }

    // public void setEnv(Env env) {
    //     this.env = env;
    // }


    @Override
    public Object expressBinary(Binary expr) {
//        if (expr.operator.type == TokenType.EQUAL_EQUAL) {
//            if (expr.left instanceof Variable) {
//                Variable left = (Variable) expr.left;
//                if (left.name.lexeme.equals(expr.right.toString())) {
//                    return true;
//                }
//            }
//        }
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
//            case AND:
//                return isTruthy(left) && isTruthy(right);
            case GREATER_THAN:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left > (int) right;
                } else {
                    return (double) left > (double) right;
                }
            case GREATER_THAN_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left >= (int) right;
                } else {
                    return (double) left >= (double) right;
                }
//            case LESS_THAN_EQUAL:
//                checkNumberOperands(expr.operator, left, right);
//                if (left instanceof Integer && right instanceof Integer) {
//                    return (int) left <= (int) right;
//                } else {
//                    return (double) left <= (double) right;
//                }
            case LESS_THAN:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left < (int) right;
                } else {
                    return (double) left < (double) right;
                }
            case LESS_THAN_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left <= (int) right;
                } else {
                    return (double) left <= (double) right;
                }
            case MINUS:
                checkNumberOperands(expr.operator, left, right);

                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left - (int) right;
                } else {
                    return (double) left - (double) right;
                }
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    if ((int) right == 0) {
                        throw new RuntimeError(expr.operator, "DILI MA divide zero.");
                    } else {
                        return (int) left / (int) right;
                    }
                } else {
                    if ((double) right == 0) {
                        throw new RuntimeError(expr.operator, "DILI MA divide zero.");
                    } else {
                        return (double) left / (double) right;
                    }
                }
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left * (int) right;
                } else {
                    return (double) left * (double) right;
                }
            case PLUS:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left + (int) right;
                } else if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                break;
            case AMPERSAND:
                String leftValue = "";
                String rightValue = "";

                if (left != null) {
                    leftValue = stringify(left);
                }

                if (right != null) {
                    rightValue = stringify(right);
                }

                return leftValue + rightValue;

            case MODULO:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer) {
                    return (int) left % (int) right;
                } else {
                    return (double) left % (double) right;
                }
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case NOT_EQUAL:
                return !isEqual(left, right);
            default:
                break;
        }

        return null;
    }


    // public void setEnv(Env env) {
    //     this.env = env;
    // }


    @Override
    public Object expressCall(Call expr) {
        throw new RuntimeError(expr.paren, "WALA NAHIBAWAN na command."); // E009
    }


    @Override
    public Object expressGrouping(Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object expressLiteral(Literal expr) {
        return expr.value;
    }

    @Override
    public Object expressUnary(Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case NOT:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                if (right instanceof Integer) {
                    return -(int) right;
                } else {
                    return -(double) right;
                }
            case PLUS:
                checkNumberOperand(expr.operator, right);
                if (right instanceof Integer) {
                    return +(int) right;
                } else {
                    return +(double) right;
                }
            default:
                break;
        }

        return null;
    }

    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    private void execute(Statements statements) {
        if (statements instanceof Print) {
            hasDisplay = true;
        }

        statements.accept(this);
    }

    void executeBlock(List<Statements> statements, Env env) {
        Env previous = this.env;
        try {
            this.env = env;
            for (Statements statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            System.err.println("[ERROR SA RUNTIME] " + error.getMessage());

        } finally {
            this.env = previous;
        }
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double || operand instanceof Integer)
            return;
        throw new RuntimeError(operator, "DDAPAT ANG OPERAND KAY NUMERO OR TIPIK.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if ((left instanceof Integer && right instanceof Integer)
                || (left instanceof Double && right instanceof Double))
            return;
        throw new RuntimeError(operator, "DAPAT ANG OPERAND KAY NUMERO OR TIPIK.");
    }

    @Override
    public Void visitExpressionStmt(Statements.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    private String stringify(Object value) {
        if (value == null) return "null";

        if (value instanceof Boolean) {
            return (Boolean) value ? "OO" : "DILI";
        }

        return value.toString();
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.print(stringify(value));
        return null;
    }

    @Override
    public Object visitIntStmt(Int stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof Integer)) {
                Object v = value;
                if (value instanceof Boolean) {
                    v = value.toString().toUpperCase();
                }
                throw new RuntimeError(stmt.name, "ang balyu '" + v + "' dili Integer.");
            }
        }
        env.define(stmt.name.lexeme, value, TokenType.INT, stmt.mutable);
        return null;
    }

    @Override
    public Object visitFloatStmt(Float stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof Double)) {
                Object v = value;
                if (value instanceof Boolean) {
                    v = value.toString().toUpperCase();
                }
                throw new RuntimeError(stmt.name, "ang balyu '" + v + "' dili Float.");
            }
        }
        env.define(stmt.name.lexeme, value, TokenType.FLOAT, stmt.mutable);
        return null;
    }

    @Override
    public Object visitCharStmt(Char stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof Character)) {
                Object v = value;
                if (value instanceof Boolean) {
                    v = value.toString().toUpperCase();
                }
                throw new RuntimeError(stmt.name, "ang balyu '" + v + "' is not of type Character.");
            }
        }
        env.define(stmt.name.lexeme, value, TokenType.CHAR, stmt.mutable);
        return null;
    }

    @Override
    public Object visitBoolStmt(Bool stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            if (!(value instanceof Boolean)) {
                throw new RuntimeError(stmt.name, "ANG BALYU '" + value + "' DILI Boolean.");
            }
        }
        env.define(stmt.name.lexeme, value, TokenType.BOOL, stmt.mutable);
        return null;
    }

    @Override
    public Object expressVariable(Variable expr) {
        return env.get(expr.name);
    }

    @Override
    public Object expressAssignment(Assign expr) {
        Object value = evaluate(expr.value);
        env.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Env(env));

        return null;
    }

    @Override
    public Object visitIfStmt(If stmt) {
        Object condition = evaluate(stmt.condition);

        if (!(condition instanceof Boolean)) {
            throw new RuntimeError(null, "TARUNG NA KONDISYON must be a Boolean.");
        }

        if (isTruthy(condition)) {
            executeBlock(stmt.thenBranch, new Env(env));
        } else {
            boolean executedElseIf = false;
            for (int i = 0; i < stmt.elseIfConditions.size(); i++) {
                Object elseIfCondition = evaluate(stmt.elseIfConditions.get(i));
                if (!(elseIfCondition instanceof Boolean)) {
                    throw new RuntimeError(null, "TARUNG NA KONDISYON must be a Boolean.");
                }
                if (isTruthy(elseIfCondition)) {
                    executeBlock(stmt.elseIfBranches.get(i), new Env(env));
                    executedElseIf = true;
                    break;
                }
            }

            if (!executedElseIf && stmt.elseBranch != null) {
                executeBlock(stmt.elseBranch, new Env(env));
            }
        }
        return null;
    }

    @Override
    public Object expressLogic(Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitForStmt(For stmt) {
        if (stmt.initializer != null) {
            execute(stmt.initializer);
        }

        while (true) {
            Object condition = evaluate(stmt.condition);

            if (!(condition instanceof Boolean)) {
                throw new RuntimeError(null, "TARUNG NA KONDISYON must be a Boolean."); // E006
            }

            if (!isTruthy(condition)) break;

            executeBlock(stmt.body, new Env(env));

            if (stmt.increment != null) {
                evaluate(stmt.increment);
            }
        }

        return null;
    }


    @Override
    public Object visitScanStmt(Scan stmt) {
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();

        List<String> input = List.of(line.split(",")).stream()
                .map(String::trim)
                .collect(Collectors.toList());

        List<Object> parsedInput = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            try {
                int intVal = Integer.parseInt(input.get(i));
                parsedInput.add(intVal);
                continue;
            } catch (NumberFormatException e) {
            }

            try {
                double floatVal = Double.parseDouble(input.get(i));
                parsedInput.add(floatVal);
            } catch (NumberFormatException e) {
                if (input.get(i).length() == 1) {
                    char newChar = input.get(i).charAt(0);
                    parsedInput.add(newChar);

                } else {
                    if (input.get(i).equals("OO")) {
                        parsedInput.add(true);
                    } else if (input.get(i).equals("DILI")) {
                        parsedInput.add(false);
                    } else {
                        parsedInput.add(input.get(i));
                    }
                }
            }
        }

        if (input.size() > stmt.identifiers.size()) {
            String m = "value";
            if (stmt.identifiers.size() > 1) {
                m = "values";
            }
            throw new RuntimeError(stmt.identifiers.get(0), "NANGITA " + stmt.identifiers.size()
                    + " " + m + ". Received more than " + stmt.identifiers.size() + " " + m + ".");
        }

        int current = 0;
        while (current < stmt.identifiers.size()) {
            Object value = parsedInput.get(current);
            env.assign(stmt.identifiers.get(current), value);
            current++;
        }
        return null;

    }

}