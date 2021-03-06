package klox.interpreter

import klox.interpreter.clazz.LoxClass
import klox.interpreter.clazz.LoxInstance
import klox.interpreter.function.LoxCallable
import klox.interpreter.function.LoxFunction
import klox.interpreter.function.NATIVE_FUNCTIONS
import klox.interpreter.function.ReturnException
import klox.lexer.Token
import klox.lexer.TokenType
import klox.parser.ast.expression.*
import klox.parser.ast.expression.Set
import klox.parser.ast.statement.*
import klox.parser.ast.statement.Function
import java.util.*


class Interpreter : ExpressionVisitor<Any?>, StatementVisitor<Unit> {
    private val globals: Environment = Environment().apply {
        NATIVE_FUNCTIONS.forEach { (name, value) -> define(name, value) }
    }
    private var environment: Environment = globals
    private val locals: MutableMap<Expr, Int> = HashMap()

    fun interpret(statements: List<Stmt>) {
        try {
            statements.forEach { execute(it) }
        } catch (error: Exception) {
            println(error)
        }
    }

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }

    override fun visitExpressionStmt(stmt: Expression) {
        evaluate(stmt.expression)
    }

    override fun visitFunctionStmt(stmt: Function) {
        val function = LoxFunction(stmt, environment, false)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitReturnStmt(stmt: Return) {
        var value: Any? = null
        if (stmt.value != null) value = evaluate(stmt.value)

        throw ReturnException(value)
    }

    override fun visitIfStmt(stmt: If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
    }

    override fun visitWhileStmt(stmt: While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
    }

    override fun visitPrintStmt(stmt: Print) {
        println(evaluate(stmt.expression))
    }

    override fun visitVarStmt(stmt: Var) {
        environment.define(stmt.name.lexeme, stmt.initializer?.let { evaluate(it) })
    }

    override fun visitBlockStmt(stmt: Block) {
        executeBlock(stmt.statements, Environment(environment));
    }

    override fun visitClassStmt(stmt: Class) {
        val superclass: LoxClass? = stmt.superclass?.let {
            val clazz = evaluate(it)
            if (clazz !is LoxClass) {
                throw IllegalStateException("Superclass must be a class")
            }
            clazz
        }

        environment.define(stmt.name.lexeme, null)

        if (stmt.superclass != null) {
            environment = Environment(environment)
            environment.define("super", superclass);
        }

        val methods: MutableMap<String, LoxFunction> = HashMap()
        for (method in stmt.methods) {
            val function = LoxFunction(method, environment, method.name.lexeme == "init")
            methods[method.name.lexeme] = function
        }

        val klass = LoxClass(stmt.name.lexeme, superclass, methods)

        if (superclass != null) {
            environment = environment.enclosing!!
        }

        environment.assign(stmt.name, klass)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }

    private fun evaluate(expression: Expr) = expression.accept(this)

    private fun stringify(`object`: Any?): String? {
        if (`object` == null) return "nil"
        if (`object` is Double) {
            var text = `object`.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }
        return `object`.toString()
    }

    override fun visitAssignExpr(expr: Assign): Any? {
        val value = evaluate(expr.value)

        val distance = locals[expr]
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }

        return value
    }

    override fun visitBinaryExpr(expr: Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> (left as Double) - (right as Double)
            TokenType.SLASH -> (left as Double) / (right as Double)
            TokenType.STAR -> (left as Double) * (right as Double)
            TokenType.PLUS -> when {
                left is Double && right is Double -> left + right
                left is String && right is String -> left + right
                else -> throw IllegalArgumentException("${expr.operator.type} supports numbers and strings only")
            }
            TokenType.GREATER -> (left as Double) > (right as Double)
            TokenType.GREATER_EQUAL -> (left as Double) >= (right as Double)
            TokenType.LESS -> (left as Double) < (right as Double)
            TokenType.LESS_EQUAL -> (left as Double) <= (right as Double)
            TokenType.EQUAL_EQUAL -> left == right
            TokenType.BANG_EQUAL -> left != right
            else -> null
        }
    }

    override fun visitCallExpr(expr: Call): Any? {
        val callee = evaluate(expr.callee)

        val arguments: MutableList<Any?> = ArrayList()
        for (argument in expr.arguments) {
            arguments.add(evaluate(argument))
        }

        if (callee !is LoxCallable) {
            throw IllegalStateException("Can only call functions and classes.")
        }

        val function: LoxCallable = callee

        if (arguments.size != function.arity()) {
            throw IllegalStateException("Expected ${function.arity()} arguments but got ${arguments.size}.")
        }

        return function.call(this, arguments)
    }

    override fun visitGetExpr(expr: Get): Any? {
        val `object` = evaluate(expr.`object`)
        if (`object` is LoxInstance) {
            return `object`[expr.name]
        }

        throw IllegalStateException("Only instances have properties.")
    }

    override fun visitGroupingExpr(expr: Grouping): Any? = evaluate(expr.expression)

    override fun visitLiteralExpr(expr: Literal): Any? = expr.value

    override fun visitLogicalExpr(expr: Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type === TokenType.OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }

        return evaluate(expr.right)
    }

    override fun visitSetExpr(expr: Set): Any? {
        val `object` =
            evaluate(expr.`object`) as? LoxInstance ?: throw IllegalStateException("Only instances have fields.")

        val value = evaluate(expr.value)
        `object`[expr.name] = value
        return value
    }

    override fun visitSuperExpr(expr: Super): Any? {
        val distance = locals[expr]!!
        val superclass = environment.getAt(distance, "super") as LoxClass

        val `object` = environment.getAt(distance - 1, "this") as LoxInstance

        val method = superclass.findMethod(expr.method.lexeme)
            ?: throw IllegalStateException("Undefined property '" + expr.method.lexeme + "'.")
        return method.bind(`object`)
    }

    override fun visitThisExpr(expr: This): Any? = lookUpVariable(expr.keyword, expr)

    override fun visitUnaryExpr(expr: Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> -(right as Double)
            TokenType.BANG -> !isTruthy(right)
            else -> null
        }
    }

    private fun isTruthy(v: Any?): Boolean =
        when (v) {
            null -> false
            is Boolean -> v
            else -> true
        }

    override fun visitVariableExpr(expr: Variable): Any? = lookUpVariable(expr.name, expr)

    private fun lookUpVariable(name: Token, expr: Expr): Any? {
        val distance = locals[expr]
        return if (distance != null) {
            environment.getAt(distance, name.lexeme)
        } else {
            globals.get(name)
        }
    }

    override fun visitAnonymousFunctionExpr(expr: AnonymousFunction): Any? =
        LoxFunction(Function(expr.paren, expr.params, expr.body), environment, false)
}