package klox.interpreter

import klox.lexer.Token

class Environment {
    private val values: MutableMap<String, Any?> = HashMap();

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
        } else {
            throw IllegalStateException("Undefined variable '" + name.lexeme + "'.");
        }
    }

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        } else {
            throw IllegalStateException("Undefined variable '" + name.lexeme + "'.");
        }
    }
}