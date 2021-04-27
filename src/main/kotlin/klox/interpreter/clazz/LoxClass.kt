package klox.interpreter.clazz

import klox.interpreter.Interpreter
import klox.interpreter.function.LoxCallable

data class LoxClass(val name: String): LoxCallable {
    override fun arity(): Int = 0

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        return LoxInstance(this)
    }
}