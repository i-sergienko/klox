package klox.resolver

import klox.Klox
import klox.interpreter.Interpreter
import klox.lexer.Token
import klox.parser.ast.expression.*
import klox.parser.ast.expression.Set
import klox.parser.ast.statement.*
import klox.parser.ast.statement.Function
import java.util.*

class Resolver(private val interpreter: Interpreter) : ExpressionVisitor<Unit>, StatementVisitor<Unit> {
    private val scopes: Deque<MutableMap<String, Boolean>> = ArrayDeque<MutableMap<String, Boolean>>()
    private var currentFunction: FunctionType = FunctionType.NONE
    private var currentClass: ClassType = ClassType.NONE

    override fun visitAssignExpr(expr: Assign) {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
    }

    override fun visitBinaryExpr(expr: Binary) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitCallExpr(expr: Call) {
        resolve(expr.callee)

        expr.arguments.forEach { argument -> resolve(argument) }
    }

    override fun visitGetExpr(expr: Get) {
        resolve(expr.`object`)
    }

    override fun visitGroupingExpr(expr: Grouping) {
        resolve(expr.expression)
    }

    override fun visitLiteralExpr(expr: Literal) = Unit

    override fun visitLogicalExpr(expr: Logical) {
        resolve(expr.left)
        resolve(expr.right)
    }

    override fun visitSetExpr(expr: Set) {
        resolve(expr.value)
        resolve(expr.`object`)
    }

    override fun visitSuperExpr(expr: Super) {
        if (currentClass == ClassType.NONE) {
            Klox.error(expr.keyword, "Can't use 'super' outside of a class.");
        } else if (currentClass != ClassType.SUBCLASS) {
            Klox.error(expr.keyword, "Can't use 'super' in a class with no superclass.");
        }

        resolveLocal(expr, expr.keyword)
    }

    override fun visitThisExpr(expr: This) {
        if (currentClass == ClassType.NONE) {
            Klox.error(expr.keyword, "Can't use 'this' outside of a class.")
        }

        resolveLocal(expr, expr.keyword)
    }

    override fun visitUnaryExpr(expr: Unary) {
        resolve(expr.right);
    }

    override fun visitVariableExpr(expr: Variable) {
        if (
            !scopes.isEmpty() &&
            scopes.peek()[expr.name.lexeme]?.let { !it } == true
        ) {
            Klox.error(
                expr.name,
                "Can't read local variable in its own initializer."
            );
        }

        resolveLocal(expr, expr.name);
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        scopes.withIndex().forEach { (i, scope) ->
            if (scope.containsKey(name.lexeme)) {
                interpreter.resolve(expr, i)
                return
            }
        }
    }

    override fun visitAnonymousFunctionExpr(expr: AnonymousFunction) {
        resolveFunction(expr.params, expr.body, FunctionType.FUNCTION)
    }

    override fun visitExpressionStmt(stmt: Expression) {
        resolve(stmt.expression)
    }

    override fun visitPrintStmt(stmt: Print) {
        resolve(stmt.expression);
    }

    override fun visitVarStmt(stmt: Var) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
    }

    private fun declare(name: Token) {
        if (scopes.isNotEmpty()) {
            val scope: MutableMap<String, Boolean> = scopes.peek() as MutableMap<String, Boolean>
            if (scope.containsKey(name.lexeme)) {
                Klox.error(name, "Already variable with this name in this scope.");
            }
            scope[name.lexeme] = false
        }
    }

    private fun define(name: Token) {
        if (scopes.isNotEmpty()) {
            val scope: MutableMap<String, Boolean> = scopes.peek() as MutableMap<String, Boolean>
            scope[name.lexeme] = true
        }
    }

    override fun visitFunctionStmt(stmt: Function) {
        declare(stmt.name)
        define(stmt.name)

        resolveFunction(stmt.params, stmt.body, FunctionType.FUNCTION)
    }

    private fun resolveFunction(params: List<Token>, body: List<Stmt>, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type

        beginScope()
        for (param in params) {
            declare(param)
            define(param)
        }
        resolve(body)
        endScope()

        currentFunction = enclosingFunction
    }

    override fun visitReturnStmt(stmt: Return) {
        if (currentFunction == FunctionType.NONE) {
            Klox.error(stmt.keyword, "Can't return from top-level code.");
        }

        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                Klox.error(stmt.keyword, "Can't return a value from an initializer.")
            }

            resolve(stmt.value)
        }
    }

    override fun visitBlockStmt(stmt: Block) {
        beginScope()
        resolve(stmt.statements)
        endScope()
    }

    private fun beginScope() {
        scopes.push(mutableMapOf())
    }

    fun resolve(statements: List<Stmt>) {
        for (statement in statements) {
            resolve(statement)
        }
    }

    private fun resolve(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun resolve(expr: Expr) {
        expr.accept(this)
    }

    private fun endScope() {
        scopes.pop()
    }

    override fun visitIfStmt(stmt: If) {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        if (stmt.elseBranch != null) resolve(stmt.elseBranch)
    }

    override fun visitWhileStmt(stmt: While) {
        resolve(stmt.condition)
        resolve(stmt.body)
    }

    override fun visitClassStmt(stmt: Class) {
        val enclosingClass = currentClass
        currentClass = ClassType.CLASS

        declare(stmt.name)
        define(stmt.name)

        val subclassName = stmt.name.lexeme
        stmt.superclass?.apply {
            if (name.lexeme == subclassName) {
                Klox.error(name, "A class can't inherit from itself.")
            }
            currentClass = ClassType.SUBCLASS
            resolve(this)

            beginScope() // Begin a new scope to define 'super'
            scopes.peek()["super"] = true
        }

        beginScope() // Begin a new scope to define 'this'
        scopes.peek()["this"] = true

        for (method in stmt.methods) {
            val declaration: FunctionType = when {
                method.name.lexeme == "init" -> FunctionType.INITIALIZER
                else -> FunctionType.METHOD
            }
            resolveFunction(method.params, method.body, declaration)
        }
        endScope()  // Get out of 'this' scope

        stmt.superclass?.apply { endScope() } // Get out of the class scope

        currentClass = enclosingClass
    }
}