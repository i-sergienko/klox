package klox.parser.ast.statement

interface StatementVisitor<R> {

    fun visitExpressionStmt(stmt: Expression): R

    fun visitPrintStmt(stmt: Print): R

    fun visitVarStmt(stmt: Var): R

    fun visitFunctionStmt(stmt: Function): R

    fun visitBlockStmt(stmt: Block): R

    fun visitIfStmt(stmt: If): R

    fun visitWhileStmt(stmt: While): R

}