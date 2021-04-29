package klox.interpreter.clazz

import klox.interpreter.function.LoxFunction
import klox.lexer.Token


data class LoxInstance(private val klass: LoxClass) {
    private val fields: MutableMap<String, Any?> = mutableMapOf()

    operator fun set(name: Token, value: Any?) {
        fields[name.lexeme] = value
    }

    operator fun get(name: Token): Any? {
        if (fields.containsKey(name.lexeme)) {
            return fields[name.lexeme]
        }

        val method: LoxFunction? = klass.findMethod(name.lexeme)
        if (method != null) {
            return method
        }

        throw IllegalStateException("Undefined property '" + name.lexeme + "'.")
    }
}