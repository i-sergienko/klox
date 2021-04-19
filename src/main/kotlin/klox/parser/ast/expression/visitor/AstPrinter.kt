package klox.parser.ast.expression.visitor

import klox.parser.ast.expression.*
import klox.parser.ast.expression.Set


class AstPrinter : ExpressionVisitor<String> {

    fun toString(expr: Expr) = expr.accept(this)

    override fun visitAssignExpr(expr: Assign): String = parenthesize("${expr.name} = ", expr.value)

    override fun visitBinaryExpr(expr: Binary): String = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visitCallExpr(expr: Call): String =
        "${expr.callee.accept(this)}.${expr.paren.lexeme}${parenthesize("", *(expr.arguments.toTypedArray()))}"

    override fun visitGetExpr(expr: Get): String = "${expr.`object`.accept(this)}.${expr.name.lexeme}"

    override fun visitGroupingExpr(expr: Grouping): String = parenthesize("group", expr.expression)

    override fun visitLiteralExpr(expr: Literal): String = expr.value?.toString() ?: "nil"

    override fun visitLogicalExpr(expr: Logical): String =
        "(${expr.left.accept(this)} ${expr.operator.lexeme} ${expr.right.accept(this)})"

    override fun visitSetExpr(expr: Set): String =
        parenthesize("${expr.`object`.accept(this)} ${expr.operator.lexeme} ${expr.value.accept(this)}")

    override fun visitSuperExpr(expr: Super): String = parenthesize("${expr.keyword.lexeme}.${expr.method.lexeme}")

    override fun visitThisExpr(expr: This): String = parenthesize(expr.keyword.lexeme)

    override fun visitUnaryExpr(expr: Unary): String = parenthesize(expr.operator.lexeme, expr.right)

    override fun visitVariableExpr(expr: Variable): String = expr.name.lexeme

    private fun parenthesize(name: String, vararg exprs: Expr): String =
        "($name ${exprs.map { e -> e.accept(this) }.joinToString(separator = " ")})"
}