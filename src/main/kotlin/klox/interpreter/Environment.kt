package klox.interpreter

import klox.lexer.Token

class Environment(private val enclosing: Environment? = null) {
    private val values: MutableMap<String, Any?> = HashMap();

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun assign(name: Token, value: Any?) {
        when {
            values.containsKey(name.lexeme) -> values[name.lexeme] = value
            enclosing != null -> enclosing.assign(name, value)
            else -> throw IllegalStateException("Undefined variable '" + name.lexeme + "'.");
        }
    }

    fun get(name: Token): Any? {
        return when {
            values.containsKey(name.lexeme) -> values[name.lexeme]
            enclosing != null -> enclosing.get(name)
            else -> throw IllegalStateException("Undefined variable '" + name.lexeme + "'.");
        }
    }
}