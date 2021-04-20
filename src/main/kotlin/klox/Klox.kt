package klox

import klox.lexer.Lexer
import klox.lexer.LexerImpl
import klox.lexer.Token
import klox.lexer.TokenType
import klox.parser.Parser
import klox.parser.ParserImpl
import klox.parser.ast.expression.Expr
import klox.parser.ast.expression.visitor.AstPrinter
import klox.parser.ast.expression.visitor.Interpreter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    when {
        args.size > 1 -> {
            println("Usage: klox [script]")
            exitProcess(64)
        }
        args.size == 1 -> Klox.runFile(args[0])
        else -> Klox.runPrompt()
    }
}

object Klox {
    var hadError = false

    val interpreter = Interpreter()

    fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))
        // Indicate an error in the exit code.
        if (hadError) exitProcess(65)
    }

    fun runPrompt() {
        val input: Reader = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)
        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            hadError = false
        }
    }

    fun run(source: String) {
        val lexer: Lexer = LexerImpl(source)
        val tokens = lexer.scanTokens()

        val parser: Parser = ParserImpl(tokens)
        val expression: Expr? = parser.parse()

        // Stop if there was a syntax error.
        if (hadError) return

        expression?.apply { interpreter.interpret(this) }
        expression?.apply { println(AstPrinter().toString(this)) }
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun report(
        line: Int, where: String,
        message: String
    ) {
        System.err.println(
            "[line $line] Error$where: $message"
        )
        hadError = true
    }

    fun error(token: Token, message: String) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message)
        } else {
            report(token.line, " at '" + token.lexeme.toString() + "'", message)
        }
    }
}