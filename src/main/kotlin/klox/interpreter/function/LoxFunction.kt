package klox.interpreter.function

import klox.interpreter.Environment
import klox.interpreter.Interpreter
import klox.interpreter.clazz.LoxInstance
import klox.parser.ast.statement.Function


class LoxFunction(
    private val declaration: Function,
    private val closure: Environment
) : LoxCallable {
    override fun arity(): Int = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(closure)

        arguments.forEachIndexed { i, argument -> environment.define(declaration.params[i].lexeme, argument) }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (e: ReturnException) {
            return e.value
        }
        return null
    }

    fun bind(instance: LoxInstance): LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment)
    }

    override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}