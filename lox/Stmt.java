//> Appendix II stmt
package craftinginterpreters.java.com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr;
import com.craftinginterpreters.lox.Token;

import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt);
    R visitClassStmt(Class stmt);
    R visitExpressionStmt(Expression stmt);
    R visitFunctionStmt(Function stmt);
    R visitIfStmt(If stmt);
    R visitPrintStmt(Print stmt);
    R visitReturnStmt(Return stmt);
    R visitVarStmt(Var stmt);
    R visitWhileStmt(While stmt);
  }

  // Nested Stmt classes here...
//> stmt-block
  static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
  }
//< stmt-block
//> stmt-class
  static class Class extends Stmt {
    Class(Token name,
          com.craftinginterpreters.lox.Expr.Variable superclass,
          List<Function> methods) {
      this.name = name;
      this.superclass = superclass;
      this.methods = methods;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }

    final Token name;
    final com.craftinginterpreters.lox.Expr.Variable superclass;
    final List<Function> methods;
  }
//< stmt-class
//> stmt-expression
  static class Expression extends Stmt {
    Expression(com.craftinginterpreters.lox.Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    final com.craftinginterpreters.lox.Expr expression;
  }
//< stmt-expression
//> stmt-function
  static class Function extends Stmt {
    Function(Token name, List<Token> params, List<Stmt> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    final Token name;
    final List<Token> params;
    final List<Stmt> body;
  }
//< stmt-function
//> stmt-if
  static class If extends Stmt {
    If(com.craftinginterpreters.lox.Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }

    final com.craftinginterpreters.lox.Expr condition;
    final Stmt thenBranch;
    final Stmt elseBranch;
  }
//< stmt-if
//> stmt-print
  static class Print extends Stmt {
    Print(com.craftinginterpreters.lox.Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    final com.craftinginterpreters.lox.Expr expression;
  }
//< stmt-print
//> stmt-return
  static class Return extends Stmt {
    Return(Token keyword, com.craftinginterpreters.lox.Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }

    final Token keyword;
    final com.craftinginterpreters.lox.Expr value;
  }
//< stmt-return
//> stmt-var
  static class Var extends Stmt {
    Var(Token name, com.craftinginterpreters.lox.Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    final Token name;
    final com.craftinginterpreters.lox.Expr initializer;
  }
//< stmt-var
//> stmt-while
  static class While extends Stmt {
    While(com.craftinginterpreters.lox.Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }

    final com.craftinginterpreters.lox.Expr condition;
    final Stmt body;
  }
//< stmt-while

  abstract <R> R accept(Visitor<R> visitor);
}
//< Appendix II stmt
