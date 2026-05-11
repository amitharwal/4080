//> Resolving and Binding resolver
package craftinginterpreters.java.com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr;
import com.craftinginterpreters.lox.Interpreter;
import com.craftinginterpreters.lox.Lox;
import com.craftinginterpreters.lox.Stmt;
import com.craftinginterpreters.lox.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements com.craftinginterpreters.lox.Expr.Visitor<Void>, com.craftinginterpreters.lox.Stmt.Visitor<Void> {
  private final Interpreter interpreter;
//> scopes-field
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
//< scopes-field
//> function-type-field
  private FunctionType currentFunction = FunctionType.NONE;
//< function-type-field

  Resolver(Interpreter interpreter) {
    this.interpreter = interpreter;
  }
//> function-type
  private enum FunctionType {
    NONE,
/* Resolving and Binding function-type < Classes function-type-method
    FUNCTION
*/
//> Classes function-type-method
    FUNCTION,
//> function-type-initializer
    INITIALIZER,
//< function-type-initializer
    METHOD
//< Classes function-type-method
  }
//< function-type
//> Classes class-type

  private enum ClassType {
    NONE,
/* Classes class-type < Inheritance class-type-subclass
    CLASS
 */
//> Inheritance class-type-subclass
    CLASS,
    SUBCLASS
//< Inheritance class-type-subclass
  }

  private ClassType currentClass = ClassType.NONE;

//< Classes class-type
//> resolve-statements
  void resolve(List<com.craftinginterpreters.lox.Stmt> statements) {
    for (com.craftinginterpreters.lox.Stmt statement : statements) {
      resolve(statement);
    }
  }
//< resolve-statements
//> visit-block-stmt
  @Override
  public Void visitBlockStmt(com.craftinginterpreters.lox.Stmt.Block stmt) {
    beginScope();
    resolve(stmt.statements);
    endScope();
    return null;
  }
//< visit-block-stmt
//> Classes resolver-visit-class
  @Override
  public Void visitClassStmt(com.craftinginterpreters.lox.Stmt.Class stmt) {
//> set-current-class
    ClassType enclosingClass = currentClass;
    currentClass = ClassType.CLASS;

//< set-current-class
    declare(stmt.name);
    define(stmt.name);
//> Inheritance resolve-superclass

//> inherit-self
    if (stmt.superclass != null &&
        stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
      com.craftinginterpreters.lox.Lox.error(stmt.superclass.name,
          "A class can't inherit from itself.");
    }

//< inherit-self
    if (stmt.superclass != null) {
//> set-current-subclass
      currentClass = ClassType.SUBCLASS;
//< set-current-subclass
      resolve(stmt.superclass);
    }
//< Inheritance resolve-superclass
//> Inheritance begin-super-scope

    if (stmt.superclass != null) {
      beginScope();
      scopes.peek().put("super", true);
    }
//< Inheritance begin-super-scope
//> resolve-methods

//> resolver-begin-this-scope
    beginScope();
    scopes.peek().put("this", true);

//< resolver-begin-this-scope
    for (com.craftinginterpreters.lox.Stmt.Function method : stmt.methods) {
      FunctionType declaration = FunctionType.METHOD;
//> resolver-initializer-type
      if (method.name.lexeme.equals("init")) {
        declaration = FunctionType.INITIALIZER;
      }

//< resolver-initializer-type
      resolveFunction(method, declaration); // [local]
    }

//> resolver-end-this-scope
    endScope();

//< resolver-end-this-scope
//< resolve-methods
//> Inheritance end-super-scope
    if (stmt.superclass != null) endScope();

//< Inheritance end-super-scope
//> restore-current-class
    currentClass = enclosingClass;
//< restore-current-class
    return null;
  }
//< Classes resolver-visit-class
//> visit-expression-stmt
  @Override
  public Void visitExpressionStmt(com.craftinginterpreters.lox.Stmt.Expression stmt) {
    resolve(stmt.expression);
    return null;
  }
//< visit-expression-stmt
//> visit-function-stmt
  @Override
  public Void visitFunctionStmt(com.craftinginterpreters.lox.Stmt.Function stmt) {
    declare(stmt.name);
    define(stmt.name);

/* Resolving and Binding visit-function-stmt < Resolving and Binding pass-function-type
    resolveFunction(stmt);
*/
//> pass-function-type
    resolveFunction(stmt, FunctionType.FUNCTION);
//< pass-function-type
    return null;
  }
//< visit-function-stmt
//> visit-if-stmt
  @Override
  public Void visitIfStmt(com.craftinginterpreters.lox.Stmt.If stmt) {
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null) resolve(stmt.elseBranch);
    return null;
  }
//< visit-if-stmt
//> visit-print-stmt
  @Override
  public Void visitPrintStmt(com.craftinginterpreters.lox.Stmt.Print stmt) {
    resolve(stmt.expression);
    return null;
  }
//< visit-print-stmt
//> visit-return-stmt
  @Override
  public Void visitReturnStmt(com.craftinginterpreters.lox.Stmt.Return stmt) {
//> return-from-top
    if (currentFunction == FunctionType.NONE) {
      com.craftinginterpreters.lox.Lox.error(stmt.keyword, "Can't return from top-level code.");
    }

//< return-from-top
    if (stmt.value != null) {
//> Classes return-in-initializer
      if (currentFunction == FunctionType.INITIALIZER) {
        com.craftinginterpreters.lox.Lox.error(stmt.keyword,
            "Can't return a value from an initializer.");
      }

//< Classes return-in-initializer
      resolve(stmt.value);
    }

    return null;
  }
//< visit-return-stmt
//> visit-var-stmt
  @Override
  public Void visitVarStmt(com.craftinginterpreters.lox.Stmt.Var stmt) {
    declare(stmt.name);
    if (stmt.initializer != null) {
      resolve(stmt.initializer);
    }
    define(stmt.name);
    return null;
  }
//< visit-var-stmt
//> visit-while-stmt
  @Override
  public Void visitWhileStmt(com.craftinginterpreters.lox.Stmt.While stmt) {
    resolve(stmt.condition);
    resolve(stmt.body);
    return null;
  }
//< visit-while-stmt
//> visit-assign-expr
  @Override
  public Void visitAssignExpr(com.craftinginterpreters.lox.Expr.Assign expr) {
    resolve(expr.value);
    resolveLocal(expr, expr.name);
    return null;
  }
//< visit-assign-expr
//> visit-binary-expr
  @Override
  public Void visitBinaryExpr(com.craftinginterpreters.lox.Expr.Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }
//< visit-binary-expr
//> visit-call-expr
  @Override
  public Void visitCallExpr(com.craftinginterpreters.lox.Expr.Call expr) {
    resolve(expr.callee);

    for (com.craftinginterpreters.lox.Expr argument : expr.arguments) {
      resolve(argument);
    }

    return null;
  }
//< visit-call-expr
//> Classes resolver-visit-get
  @Override
  public Void visitGetExpr(com.craftinginterpreters.lox.Expr.Get expr) {
    resolve(expr.object);
    return null;
  }
//< Classes resolver-visit-get
//> visit-grouping-expr
  @Override
  public Void visitGroupingExpr(com.craftinginterpreters.lox.Expr.Grouping expr) {
    resolve(expr.expression);
    return null;
  }
//< visit-grouping-expr
//> visit-literal-expr
  @Override
  public Void visitLiteralExpr(com.craftinginterpreters.lox.Expr.Literal expr) {
    return null;
  }
//< visit-literal-expr
//> visit-logical-expr
  @Override
  public Void visitLogicalExpr(com.craftinginterpreters.lox.Expr.Logical expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }
//< visit-logical-expr
//> Classes resolver-visit-set
  @Override
  public Void visitSetExpr(com.craftinginterpreters.lox.Expr.Set expr) {
    resolve(expr.value);
    resolve(expr.object);
    return null;
  }
//< Classes resolver-visit-set
//> Inheritance resolve-super-expr
  @Override
  public Void visitSuperExpr(com.craftinginterpreters.lox.Expr.Super expr) {
//> invalid-super
    if (currentClass == ClassType.NONE) {
      com.craftinginterpreters.lox.Lox.error(expr.keyword,
          "Can't use 'super' outside of a class.");
    } else if (currentClass != ClassType.SUBCLASS) {
      com.craftinginterpreters.lox.Lox.error(expr.keyword,
          "Can't use 'super' in a class with no superclass.");
    }

//< invalid-super
    resolveLocal(expr, expr.keyword);
    return null;
  }
//< Inheritance resolve-super-expr
//> Classes resolver-visit-this
  @Override
  public Void visitThisExpr(com.craftinginterpreters.lox.Expr.This expr) {
//> this-outside-of-class
    if (currentClass == ClassType.NONE) {
      com.craftinginterpreters.lox.Lox.error(expr.keyword,
          "Can't use 'this' outside of a class.");
      return null;
    }

//< this-outside-of-class
    resolveLocal(expr, expr.keyword);
    return null;
  }

//< Classes resolver-visit-this
//> visit-unary-expr
  @Override
  public Void visitUnaryExpr(com.craftinginterpreters.lox.Expr.Unary expr) {
    resolve(expr.right);
    return null;
  }
//< visit-unary-expr
//> visit-variable-expr
  @Override
  public Void visitVariableExpr(com.craftinginterpreters.lox.Expr.Variable expr) {
    if (!scopes.isEmpty() &&
        scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
      com.craftinginterpreters.lox.Lox.error(expr.name,
          "Can't read local variable in its own initializer.");
    }

    resolveLocal(expr, expr.name);
    return null;
  }
//< visit-variable-expr
//> resolve-stmt
  private void resolve(com.craftinginterpreters.lox.Stmt stmt) {
    stmt.accept(this);
  }
//< resolve-stmt
//> resolve-expr
  private void resolve(com.craftinginterpreters.lox.Expr expr) {
    expr.accept(this);
  }
//< resolve-expr
//> resolve-function
/* Resolving and Binding resolve-function < Resolving and Binding set-current-function
  private void resolveFunction(Stmt.Function function) {
*/
//> set-current-function
  private void resolveFunction(
          com.craftinginterpreters.lox.Stmt.Function function, FunctionType type) {
    FunctionType enclosingFunction = currentFunction;
    currentFunction = type;

//< set-current-function
    beginScope();
    for (com.craftinginterpreters.lox.Token param : function.params) {
      declare(param);
      define(param);
    }
    resolve(function.body);
    endScope();
//> restore-current-function
    currentFunction = enclosingFunction;
//< restore-current-function
  }
//< resolve-function
//> begin-scope
  private void beginScope() {
    scopes.push(new HashMap<String, Boolean>());
  }
//< begin-scope
//> end-scope
  private void endScope() {
    scopes.pop();
  }
//< end-scope
//> declare
  private void declare(com.craftinginterpreters.lox.Token name) {
    if (scopes.isEmpty()) return;

    Map<String, Boolean> scope = scopes.peek();
//> duplicate-variable
    if (scope.containsKey(name.lexeme)) {
      Lox.error(name,
          "Already a variable with this name in this scope.");
    }

//< duplicate-variable
    scope.put(name.lexeme, false);
  }
//< declare
//> define
  private void define(com.craftinginterpreters.lox.Token name) {
    if (scopes.isEmpty()) return;
    scopes.peek().put(name.lexeme, true);
  }
//< define
//> resolve-local
  private void resolveLocal(com.craftinginterpreters.lox.Expr expr, com.craftinginterpreters.lox.Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(expr, scopes.size() - 1 - i);
        return;
      }
    }
  }
//< resolve-local
}
