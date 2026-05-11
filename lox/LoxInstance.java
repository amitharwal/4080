//> Classes lox-instance
package craftinginterpreters.java.com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.LoxClass;
import com.craftinginterpreters.lox.LoxFunction;
import com.craftinginterpreters.lox.RuntimeError;
import com.craftinginterpreters.lox.Token;

import java.util.HashMap;
import java.util.Map;

class LoxInstance {
  private com.craftinginterpreters.lox.LoxClass klass;
//> lox-instance-fields
  private final Map<String, Object> fields = new HashMap<>();
//< lox-instance-fields

  LoxInstance(com.craftinginterpreters.lox.LoxClass klass) {
    this.klass = klass;
  }

//> lox-instance-get-property
  Object get(com.craftinginterpreters.lox.Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

//> lox-instance-get-method
    com.craftinginterpreters.lox.LoxFunction method = klass.findMethod(name.lexeme);
/* Classes lox-instance-get-method < Classes lox-instance-bind-method
    if (method != null) return method;
*/
//> lox-instance-bind-method
    if (method != null) return method.bind(this);
//< lox-instance-bind-method

//< lox-instance-get-method
    throw new RuntimeError(name, // [hidden]
        "Undefined property '" + name.lexeme + "'.");
  }
//< lox-instance-get-property
//> lox-instance-set-property
  void set(com.craftinginterpreters.lox.Token name, Object value) {
    fields.put(name.lexeme, value);
  }
//< lox-instance-set-property
  @Override
  public String toString() {
    return klass.name + " instance";
  }
}
