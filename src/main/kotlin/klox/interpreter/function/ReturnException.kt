package klox.interpreter.function

data class ReturnException(val value: Any?) : RuntimeException(null, null, false, false)