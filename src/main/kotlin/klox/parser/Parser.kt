package klox.parser

import klox.parser.ast.statement.Stmt

interface Parser {
    fun parse(): List<Stmt>
}