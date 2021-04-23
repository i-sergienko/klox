package klox.parser.ast.statement

interface StatementVisitor<R> {

    fun visitExpressionStmt(stmt: Expression): R

    fun visitPrintStmt(stmt: Print): R

    fun visitVarStmt(stmt: Var): R

}