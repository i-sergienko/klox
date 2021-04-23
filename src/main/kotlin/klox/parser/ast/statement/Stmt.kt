package klox.parser.ast.statement

import klox.parser.ast.expression.Expr

sealed class Stmt {
    abstract fun <R> accept(visitor: StatementVisitor<R>): R
}

data class Expression(val expression: Expr) : Stmt() {
    override fun <R> accept(visitor: StatementVisitor<R>): R = visitor.visitExpressionStmt(this)
}

data class Print(val expression: Expr) : Stmt() {
    override fun <R> accept(visitor: StatementVisitor<R>): R = visitor.visitPrintStmt(this)
}