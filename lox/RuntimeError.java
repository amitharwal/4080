//> Evaluating Expressions runtime-error-class
package craftinginterpreters.java.com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Token;

class RuntimeError extends RuntimeException {
  final com.craftinginterpreters.lox.Token token;

  RuntimeError(com.craftinginterpreters.lox.Token token, String message) {
    super(message);
    this.token = token;
  }
}
