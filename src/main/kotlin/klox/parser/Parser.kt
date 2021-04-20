package klox.parser

import klox.parser.ast.expression.Expr

interface Parser {
    fun parse(): Expr?
}