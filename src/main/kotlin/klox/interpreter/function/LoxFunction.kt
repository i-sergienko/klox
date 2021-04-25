package klox.interpreter.function

import klox.interpreter.Environment
import klox.interpreter.Interpreter
import klox.parser.ast.statement.Function

class LoxFunction(
    private val declaration: Function,
    private val globals: Environment
) : LoxCallable {
    override fun arity(): Int = declaration.params.size

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(globals)

        arguments.forEachIndexed { i, argument -> environment.define(declaration.params[i].lexeme, argument) }

        interpreter.executeBlock(declaration.body, environment)
        return null
    }

    override fun toString(): String = "<fn ${declaration.name.lexeme}>"
}