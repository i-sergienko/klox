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

    fun getAt(distance: Int, name: String): Any? {
        return ancestor(distance).values[name]
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    private fun ancestor(distance: Int): Environment {
        var environment: Environment = this
        for (i in 0 until distance) {
            environment = environment.enclosing!!
        }
        return environment
    }
}