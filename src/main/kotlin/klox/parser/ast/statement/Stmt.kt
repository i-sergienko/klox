package klox.parser.ast.statement

import klox.lexer.Token
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

data class Var(val name: Token, val initializer: Expr?) : Stmt() {
    override fun <R> accept(visitor: StatementVisitor<R>): R = visitor.visitVarStmt(this)
}

data class Block(val statements: List<Stmt>) : Stmt() {
    override fun <R> accept(visitor: StatementVisitor<R>): R = visitor.visitBlockStmt(this)
}