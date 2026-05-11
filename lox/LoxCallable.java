//> Functions callable
package craftinginterpreters.java.com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Interpreter;

import java.util.List;

interface LoxCallable {
//> callable-arity
  int arity();
//< callable-arity
  Object call(com.craftinginterpreters.lox.Interpreter interpreter, List<Object> arguments);
}
