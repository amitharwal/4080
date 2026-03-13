package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(1);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Logical  : Expr left, Token operator, Expr right",
                "Unary    : Token operator, Expr right",
                "Variable : Token name",
                "Class      : Token name, List<Stmt.Function> methods, List<Stmt.Function> classMethods")
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block      : List<Stmt> statements",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> parameters, List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "Return     : Token keyword, Expr value",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt body"
        ));
    }
    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");

        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt.Function> classMethods = new ArrayList<>();
        consume(LEFT_BRACE, "Expect '{' before class body.");

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            boolean isClassMethod = match(CLASS);
            (isClassMethod ? classMethods : methods).add(function("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");

        return new Stmt.Class(name, methods, classMethods);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");

        List<Token> parameters = null;

        // Allow omitting the parameter list entirely in method getters.
        if (!kind.equals("method") || check(LEFT_PAREN)) {
            consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
            parameters = new ArrayList<>();
            if (!check(RIGHT_PAREN)) {
                do {
                    if (parameters.size() >= 255) {
                        error(peek(), "Can't have more than 255 parameters.");
                    }

                    parameters.add(consume(IDENTIFIER, "Expect parameter name."));
                } while (match(COMMA));
            }
            consume(RIGHT_PAREN, "Expect ')' after parameters.");
        }

        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private static void defineAst(
            String outputDir, String baseName, List<String> types)
            throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.craftinginterpreters.lox;");
        writer.println("");
        writer.println("import java.util.List;");
        writer.println("");
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        // The AST classes.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim(); // [robust]
            defineType(writer, baseName, className, fields);
        }

        // The base accept() method.
        writer.println("");
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }
    private static void defineVisitor(
            PrintWriter writer, String baseName, List<String> types) {
        writer.println("  interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }
    private static void defineType(
            PrintWriter writer, String baseName,
            String className, String fieldList) {
        writer.println("");
        writer.println("  static class " + className + " extends " +
                baseName + " {");

        // Constructor.
        writer.println("    " + className + "(" + fieldList + ") {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

        // Visitor pattern.
        writer.println();
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" +
                className + baseName + "(this);");
        writer.println("    }");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
    }
    interface PastryVisitor {
        void visitBeignet(Beignet beignet); // [overload]
        void visitCruller(Cruller cruller);
    }
    abstract class Pastry {
        abstract void accept(PastryVisitor visitor);
    }

    class Beignet extends Pastry {
        @Override
        void accept(PastryVisitor visitor) {
            visitor.visitBeignet(this);
        }
    }

    class Cruller extends Pastry {
        @Override
        void accept(PastryVisitor visitor) {
            visitor.visitCruller(this);
        }
    }
}
