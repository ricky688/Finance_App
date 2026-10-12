package com.example.vibefinance.util

import java.math.BigDecimal
import java.math.RoundingMode

/** Decimal addition/subtraction, independent of entry currency and persisted amount signs. */
object KeypadCalculatorEngine {
    data class State(val expression: String = "0", val evaluated: Boolean = false)
    private val complete = Regex("-?\\d+(?:\\.\\d{0,2})?(?:[+-]\\d+(?:\\.\\d{0,2})?)*")
    private val operand = Regex("[+-]?\\d+(?:\\.\\d{0,2})?")
    fun result(expression: String): BigDecimal? = if (!complete.matches(expression)) null else
        runCatching { operand.findAll(expression).fold(BigDecimal.ZERO) { sum, it -> sum + it.value.toBigDecimal() } }.getOrNull()
    fun preview(expression: String): BigDecimal? = result(expression.trimEnd('+', '-'))
    fun press(state: State, key: String): State {
        val text = state.expression
        return when (key) {
            "clear" -> State()
            "back" -> State(text.dropLast(1).takeIf { it.isNotEmpty() && it != "-" } ?: "0")
            "=" -> result(text)?.let { State(it.setScale(2, RoundingMode.HALF_UP).toPlainString(), true) } ?: state
            "+", "-" -> if (text == "0" && !state.evaluated) state else State(text.trimEnd('+', '-') + key)
            "." -> {
                val base = if (state.evaluated) "0" else text
                val last = base.substringAfterLast('+').substringAfterLast('-')
                if ('.' in last) state else State(base + if (last.isEmpty()) "0." else ".")
            }
            else -> {
                if (key.length != 1 || !key[0].isDigit()) return state
                val base = if (state.evaluated || text == "0" || text == "0.00") "" else text
                val last = base.substringAfterLast('+').substringAfterLast('-')
                if ('.' in last && last.substringAfter('.').length >= 2) state
                else State(if (last == "0") base.dropLast(1) + key else base + key)
            }
        }
    }
}
