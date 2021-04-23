package klox.parser.ast.expression

import klox.lexer.Token

sealed class Expr {
    abstract fun <R> accept(visitor: ExpressionVisitor<R>): R
}

data class Assign(
    val name: Token,
    val value: Expr
) : Expr() {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitAssignExpr(this)
}

data class Binary(
    val left: Expr,
    val operator: Token,
    val right: Expr
) : Expr() {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitBinaryExpr(this)
}

data class Call(
    val callee: Expr,
    val paren: Token,
    val arguments: List<Expr>
) : Expr() {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitCallExpr(this)
}

data class Get(
    val `object`: Expr,
    val name: Token
) : Expr() {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitGetExpr(this)
}

data class Grouping(val expression: Expr) : Expr() {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitGroupingExpr(this)
}

data class Literal(val value: Any?) : Expr() {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitLiteralExpr(this)
}

data class Logical(
    val left: Expr,
    val operator: Token,
    val right: Expr
) : Expr() {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitLogicalExpr(this)
}

data class Set(
    val `object`: Expr,
    val operator: Token,
    val value: Expr
) : Expr() {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitSetExpr(this)
}

data class Super(val keyword: Token, val method: Token) : Expr() {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitSuperExpr(this)
}

data class This(val keyword: Token) : Expr() {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitThisExpr(this)
}

data class Unary(val operator: Token, val right: Expr) : Expr() {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitUnaryExpr(this)
}

data class Variable(val name: Token) : Expr() {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitVariableExpr(this)
}