package klox.parser.ast.expression.visitor

import klox.lexer.TokenType
import klox.parser.ast.expression.*
import klox.parser.ast.expression.Set


class Interpreter : ExpressionVisitor<Any?> {

    fun interpret(expression: Expr) {
        try {
            val value = evaluate(expression)
            println(stringify(value))
        } catch (error: Exception) {
            println(error)
        }
    }

    fun evaluate(expression: Expr) = expression.accept(this)

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
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun visitGetExpr(expr: Get): Any? {
        TODO("Not yet implemented")
    }

    override fun visitGroupingExpr(expr: Grouping): Any? = evaluate(expr.expression)

    override fun visitLiteralExpr(expr: Literal): Any? = expr.value

    override fun visitLogicalExpr(expr: Logical): Any? {
        TODO("Not yet implemented")
    }

    override fun visitSetExpr(expr: Set): Any? {
        TODO("Not yet implemented")
    }

    override fun visitSuperExpr(expr: Super): Any? {
        TODO("Not yet implemented")
    }

    override fun visitThisExpr(expr: This): Any? {
        TODO("Not yet implemented")
    }

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

    override fun visitVariableExpr(expr: Variable): Any? {
        TODO("Not yet implemented")
    }
}