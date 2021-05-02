package klox.interpreter.clazz

import klox.interpreter.Interpreter
import klox.interpreter.function.LoxCallable
import klox.interpreter.function.LoxFunction


data class LoxClass(
    val name: String,
    val methods: Map<String, LoxFunction>
) : LoxCallable {

    fun findMethod(name: String?): LoxFunction? = methods[name]

    override fun arity(): Int {
        val initializer = findMethod("init")

        return initializer?.arity() ?: 0
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val instance = LoxInstance(this)

        val initializer = findMethod("init")
        initializer?.bind(instance)?.call(interpreter, arguments)

        return instance
    }
}