//> Parsing Expressions parser
package craftinginterpreters.java.com.craftinginterpreters.lox;

//> Statements and State parser-imports
import com.craftinginterpreters.lox.Expr;
import com.craftinginterpreters.lox.Lox;
import com.craftinginterpreters.lox.Stmt;
import com.craftinginterpreters.lox.Token;
import com.craftinginterpreters.lox.TokenType;

import java.util.ArrayList;
//< Statements and State parser-imports
//> Control Flow import-arrays
import java.util.Arrays;
//< Control Flow import-arrays
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

class Parser {
//> parse-error
  private static class ParseError extends RuntimeException {}

//< parse-error
  private final List<com.craftinginterpreters.lox.Token> tokens;
  private int current = 0;

  Parser(List<com.craftinginterpreters.lox.Token> tokens) {
    this.tokens = tokens;
  }
/* Parsing Expressions parse < Statements and State parse
  Expr parse() {
    try {
      return expression();
    } catch (ParseError error) {
      return null;
    }
  }
*/
//> Statements and State parse
  List<com.craftinginterpreters.lox.Stmt> parse() {
    List<com.craftinginterpreters.lox.Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
/* Statements and State parse < Statements and State parse-declaration
      statements.add(statement());
*/
//> parse-declaration
      statements.add(declaration());
//< parse-declaration
    }

    return statements; // [parse-error-handling]
  }
//< Statements and State parse
//> expression
  private com.craftinginterpreters.lox.Expr expression() {
/* Parsing Expressions expression < Statements and State expression
    return equality();
*/
//> Statements and State expression
    return assignment();
//< Statements and State expression
  }
//< expression
//> Statements and State declaration
  private com.craftinginterpreters.lox.Stmt declaration() {
    try {
//> Classes match-class
      if (match(CLASS)) return classDeclaration();
//< Classes match-class
//> Functions match-fun
      if (match(FUN)) return function("function");
//< Functions match-fun
      if (match(VAR)) return varDeclaration();

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }
//< Statements and State declaration
//> Classes parse-class-declaration
  private com.craftinginterpreters.lox.Stmt classDeclaration() {
    com.craftinginterpreters.lox.Token name = consume(IDENTIFIER, "Expect class name.");
//> Inheritance parse-superclass

    com.craftinginterpreters.lox.Expr.Variable superclass = null;
    if (match(LESS)) {
      consume(IDENTIFIER, "Expect superclass name.");
      superclass = new com.craftinginterpreters.lox.Expr.Variable(previous());
    }

//< Inheritance parse-superclass
    consume(LEFT_BRACE, "Expect '{' before class body.");

    List<com.craftinginterpreters.lox.Stmt.Function> methods = new ArrayList<>();
    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      methods.add(function("method"));
    }

    consume(RIGHT_BRACE, "Expect '}' after class body.");

/* Classes parse-class-declaration < Inheritance construct-class-ast
    return new Stmt.Class(name, methods);
*/
//> Inheritance construct-class-ast
    return new com.craftinginterpreters.lox.Stmt.Class(name, superclass, methods);
//< Inheritance construct-class-ast
  }
//< Classes parse-class-declaration
//> Statements and State parse-statement
  private com.craftinginterpreters.lox.Stmt statement() {
//> Control Flow match-for
    if (match(FOR)) return forStatement();
//< Control Flow match-for
//> Control Flow match-if
    if (match(IF)) return ifStatement();
//< Control Flow match-if
    if (match(PRINT)) return printStatement();
//> Functions match-return
    if (match(RETURN)) return returnStatement();
//< Functions match-return
//> Control Flow match-while
    if (match(WHILE)) return whileStatement();
//< Control Flow match-while
//> parse-block
    if (match(LEFT_BRACE)) return new com.craftinginterpreters.lox.Stmt.Block(block());
//< parse-block

    return expressionStatement();
  }
//< Statements and State parse-statement
//> Control Flow for-statement
  private com.craftinginterpreters.lox.Stmt forStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'for'.");

/* Control Flow for-statement < Control Flow for-initializer
    // More here...
*/
//> for-initializer
    com.craftinginterpreters.lox.Stmt initializer;
    if (match(SEMICOLON)) {
      initializer = null;
    } else if (match(VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }
//< for-initializer
//> for-condition

    com.craftinginterpreters.lox.Expr condition = null;
    if (!check(SEMICOLON)) {
      condition = expression();
    }
    consume(SEMICOLON, "Expect ';' after loop condition.");
//< for-condition
//> for-increment

    com.craftinginterpreters.lox.Expr increment = null;
    if (!check(RIGHT_PAREN)) {
      increment = expression();
    }
    consume(RIGHT_PAREN, "Expect ')' after for clauses.");
//< for-increment
//> for-body
    com.craftinginterpreters.lox.Stmt body = statement();

//> for-desugar-increment
    if (increment != null) {
      body = new com.craftinginterpreters.lox.Stmt.Block(
          Arrays.asList(
              body,
              new com.craftinginterpreters.lox.Stmt.Expression(increment)));
    }

//< for-desugar-increment
//> for-desugar-condition
    if (condition == null) condition = new com.craftinginterpreters.lox.Expr.Literal(true);
    body = new com.craftinginterpreters.lox.Stmt.While(condition, body);

//< for-desugar-condition
//> for-desugar-initializer
    if (initializer != null) {
      body = new com.craftinginterpreters.lox.Stmt.Block(Arrays.asList(initializer, body));
    }

//< for-desugar-initializer
    return body;
//< for-body
  }
//< Control Flow for-statement
//> Control Flow if-statement
  private com.craftinginterpreters.lox.Stmt ifStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'if'.");
    com.craftinginterpreters.lox.Expr condition = expression();
    consume(RIGHT_PAREN, "Expect ')' after if condition."); // [parens]

    com.craftinginterpreters.lox.Stmt thenBranch = statement();
    com.craftinginterpreters.lox.Stmt elseBranch = null;
    if (match(ELSE)) {
      elseBranch = statement();
    }

    return new com.craftinginterpreters.lox.Stmt.If(condition, thenBranch, elseBranch);
  }
//< Control Flow if-statement
//> Statements and State parse-print-statement
  private com.craftinginterpreters.lox.Stmt printStatement() {
    com.craftinginterpreters.lox.Expr value = expression();
    consume(SEMICOLON, "Expect ';' after value.");
    return new com.craftinginterpreters.lox.Stmt.Print(value);
  }
//< Statements and State parse-print-statement
//> Functions parse-return-statement
  private com.craftinginterpreters.lox.Stmt returnStatement() {
    com.craftinginterpreters.lox.Token keyword = previous();
    com.craftinginterpreters.lox.Expr value = null;
    if (!check(SEMICOLON)) {
      value = expression();
    }

    consume(SEMICOLON, "Expect ';' after return value.");
    return new com.craftinginterpreters.lox.Stmt.Return(keyword, value);
  }
//< Functions parse-return-statement
//> Statements and State parse-var-declaration
  private com.craftinginterpreters.lox.Stmt varDeclaration() {
    com.craftinginterpreters.lox.Token name = consume(IDENTIFIER, "Expect variable name.");

    com.craftinginterpreters.lox.Expr initializer = null;
    if (match(EQUAL)) {
      initializer = expression();
    }

    consume(SEMICOLON, "Expect ';' after variable declaration.");
    return new com.craftinginterpreters.lox.Stmt.Var(name, initializer);
  }
//< Statements and State parse-var-declaration
//> Control Flow while-statement
  private com.craftinginterpreters.lox.Stmt whileStatement() {
    consume(LEFT_PAREN, "Expect '(' after 'while'.");
    com.craftinginterpreters.lox.Expr condition = expression();
    consume(RIGHT_PAREN, "Expect ')' after condition.");
    com.craftinginterpreters.lox.Stmt body = statement();

    return new com.craftinginterpreters.lox.Stmt.While(condition, body);
  }
//< Control Flow while-statement
//> Statements and State parse-expression-statement
  private com.craftinginterpreters.lox.Stmt expressionStatement() {
    com.craftinginterpreters.lox.Expr expr = expression();
    consume(SEMICOLON, "Expect ';' after expression.");
    return new com.craftinginterpreters.lox.Stmt.Expression(expr);
  }
//< Statements and State parse-expression-statement
//> Functions parse-function
  private com.craftinginterpreters.lox.Stmt.Function function(String kind) {
    com.craftinginterpreters.lox.Token name = consume(IDENTIFIER, "Expect " + kind + " name.");
//> parse-parameters
    consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
    List<com.craftinginterpreters.lox.Token> parameters = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Can't have more than 255 parameters.");
        }

        parameters.add(
            consume(IDENTIFIER, "Expect parameter name."));
      } while (match(COMMA));
    }
    consume(RIGHT_PAREN, "Expect ')' after parameters.");
//< parse-parameters
//> parse-body

    consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
    List<com.craftinginterpreters.lox.Stmt> body = block();
    return new com.craftinginterpreters.lox.Stmt.Function(name, parameters, body);
//< parse-body
  }
//< Functions parse-function
//> Statements and State block
  private List<com.craftinginterpreters.lox.Stmt> block() {
    List<com.craftinginterpreters.lox.Stmt> statements = new ArrayList<>();

    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(RIGHT_BRACE, "Expect '}' after block.");
    return statements;
  }
//< Statements and State block
//> Statements and State parse-assignment
  private com.craftinginterpreters.lox.Expr assignment() {
/* Statements and State parse-assignment < Control Flow or-in-assignment
    Expr expr = equality();
*/
//> Control Flow or-in-assignment
    com.craftinginterpreters.lox.Expr expr = or();
//< Control Flow or-in-assignment

    if (match(EQUAL)) {
      com.craftinginterpreters.lox.Token equals = previous();
      com.craftinginterpreters.lox.Expr value = assignment();

      if (expr instanceof com.craftinginterpreters.lox.Expr.Variable) {
        com.craftinginterpreters.lox.Token name = ((com.craftinginterpreters.lox.Expr.Variable)expr).name;
        return new com.craftinginterpreters.lox.Expr.Assign(name, value);
//> Classes assign-set
      } else if (expr instanceof com.craftinginterpreters.lox.Expr.Get) {
        com.craftinginterpreters.lox.Expr.Get get = (com.craftinginterpreters.lox.Expr.Get)expr;
        return new com.craftinginterpreters.lox.Expr.Set(get.object, get.name, value);
//< Classes assign-set
      }

      error(equals, "Invalid assignment target."); // [no-throw]
    }

    return expr;
  }
//< Statements and State parse-assignment
//> Control Flow or
  private com.craftinginterpreters.lox.Expr or() {
    com.craftinginterpreters.lox.Expr expr = and();

    while (match(OR)) {
      com.craftinginterpreters.lox.Token operator = previous();
      com.craftinginterpreters.lox.Expr right = and();
      expr = new com.craftinginterpreters.lox.Expr.Logical(expr, operator, right);
    }

    return expr;
  }
//< Control Flow or
//> Control Flow and
  private com.craftinginterpreters.lox.Expr and() {
    com.craftinginterpreters.lox.Expr expr = equality();

    while (match(AND)) {
      com.craftinginterpreters.lox.Token operator = previous();
      com.craftinginterpreters.lox.Expr right = equality();
      expr = new com.craftinginterpreters.lox.Expr.Logical(expr, operator, right);
    }

    return expr;
  }
//< Control Flow and
//> equality
  private com.craftinginterpreters.lox.Expr equality() {
    com.craftinginterpreters.lox.Expr expr = comparison();

    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      com.craftinginterpreters.lox.Token operator = previous();
      com.craftinginterpreters.lox.Expr right = comparison();
      expr = new com.craftinginterpreters.lox.Expr.Binary(expr, operator, right);
    }

    return expr;
  }
//< equality
//> comparison
  private com.craftinginterpreters.lox.Expr comparison() {
    com.craftinginterpreters.lox.Expr expr = term();

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      com.craftinginterpreters.lox.Token operator = previous();
      com.craftinginterpreters.lox.Expr right = term();
      expr = new com.craftinginterpreters.lox.Expr.Binary(expr, operator, right);
    }

    return expr;
  }
//< comparison
//> term
  private com.craftinginterpreters.lox.Expr term() {
    com.craftinginterpreters.lox.Expr expr = factor();

    while (match(MINUS, PLUS)) {
      com.craftinginterpreters.lox.Token operator = previous();
      com.craftinginterpreters.lox.Expr right = factor();
      expr = new com.craftinginterpreters.lox.Expr.Binary(expr, operator, right);
    }

    return expr;
  }
//< term
//> factor
  private com.craftinginterpreters.lox.Expr factor() {
    com.craftinginterpreters.lox.Expr expr = unary();

    while (match(SLASH, STAR)) {
      com.craftinginterpreters.lox.Token operator = previous();
      com.craftinginterpreters.lox.Expr right = unary();
      expr = new com.craftinginterpreters.lox.Expr.Binary(expr, operator, right);
    }

    return expr;
  }
//< factor
//> unary
  private com.craftinginterpreters.lox.Expr unary() {
    if (match(BANG, MINUS)) {
      com.craftinginterpreters.lox.Token operator = previous();
      com.craftinginterpreters.lox.Expr right = unary();
      return new com.craftinginterpreters.lox.Expr.Unary(operator, right);
    }

/* Parsing Expressions unary < Functions unary-call
    return primary();
*/
//> Functions unary-call
    return call();
//< Functions unary-call
  }
//< unary
//> Functions finish-call
  private com.craftinginterpreters.lox.Expr finishCall(com.craftinginterpreters.lox.Expr callee) {
    List<com.craftinginterpreters.lox.Expr> arguments = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
//> check-max-arity
        if (arguments.size() >= 255) {
          error(peek(), "Can't have more than 255 arguments.");
        }
//< check-max-arity
        arguments.add(expression());
      } while (match(COMMA));
    }

    com.craftinginterpreters.lox.Token paren = consume(RIGHT_PAREN,
                          "Expect ')' after arguments.");

    return new com.craftinginterpreters.lox.Expr.Call(callee, paren, arguments);
  }
//< Functions finish-call
//> Functions call
  private com.craftinginterpreters.lox.Expr call() {
    com.craftinginterpreters.lox.Expr expr = primary();

    while (true) { // [while-true]
      if (match(LEFT_PAREN)) {
        expr = finishCall(expr);
//> Classes parse-property
      } else if (match(DOT)) {
        com.craftinginterpreters.lox.Token name = consume(IDENTIFIER,
            "Expect property name after '.'.");
        expr = new com.craftinginterpreters.lox.Expr.Get(expr, name);
//< Classes parse-property
      } else {
        break;
      }
    }

    return expr;
  }
//< Functions call
//> primary
  private com.craftinginterpreters.lox.Expr primary() {
    if (match(FALSE)) return new com.craftinginterpreters.lox.Expr.Literal(false);
    if (match(TRUE)) return new com.craftinginterpreters.lox.Expr.Literal(true);
    if (match(NIL)) return new com.craftinginterpreters.lox.Expr.Literal(null);

    if (match(NUMBER, STRING)) {
      return new com.craftinginterpreters.lox.Expr.Literal(previous().literal);
    }
//> Inheritance parse-super

    if (match(SUPER)) {
      com.craftinginterpreters.lox.Token keyword = previous();
      consume(DOT, "Expect '.' after 'super'.");
      com.craftinginterpreters.lox.Token method = consume(IDENTIFIER,
          "Expect superclass method name.");
      return new com.craftinginterpreters.lox.Expr.Super(keyword, method);
    }
//< Inheritance parse-super
//> Classes parse-this

    if (match(THIS)) return new com.craftinginterpreters.lox.Expr.This(previous());
//< Classes parse-this
//> Statements and State parse-identifier

    if (match(IDENTIFIER)) {
      return new com.craftinginterpreters.lox.Expr.Variable(previous());
    }
//< Statements and State parse-identifier

    if (match(LEFT_PAREN)) {
      com.craftinginterpreters.lox.Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new com.craftinginterpreters.lox.Expr.Grouping(expr);
    }
//> primary-error

    throw error(peek(), "Expect expression.");
//< primary-error
  }
//< primary
//> match
  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }
//< match
//> consume
  private com.craftinginterpreters.lox.Token consume(TokenType type, String message) {
    if (check(type)) return advance();

    throw error(peek(), message);
  }
//< consume
//> check
  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().type == type;
  }
//< check
//> advance
  private com.craftinginterpreters.lox.Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }
//< advance
//> utils
  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private com.craftinginterpreters.lox.Token peek() {
    return tokens.get(current);
  }

  private com.craftinginterpreters.lox.Token previous() {
    return tokens.get(current - 1);
  }
//< utils
//> error
  private ParseError error(com.craftinginterpreters.lox.Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }
//< error
//> synchronize
  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }
//< synchronize
}
