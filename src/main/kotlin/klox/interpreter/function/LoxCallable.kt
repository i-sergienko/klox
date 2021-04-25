package klox.interpreter.function

import klox.interpreter.Interpreter

interface LoxCallable {
    fun arity(): Int

    fun call(interpreter: Interpreter, arguments: List<Any?>): Any?
}