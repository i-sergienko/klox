package klox.lexer

interface Lexer {
    fun scanTokens(): List<Token>
}