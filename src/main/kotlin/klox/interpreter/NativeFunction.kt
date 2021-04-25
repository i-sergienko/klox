package klox.interpreter

val NATIVE_FUNCTIONS = mapOf<String, LoxCallable>(
    "clock" to object : LoxCallable {
        override fun arity(): Int = 0

        override fun call(interpreter: Interpreter, arguments: List<Any?>): Double =
            System.currentTimeMillis().toDouble() / 1000.0

        override fun toString(): String = "<native fn>"
    }
)