package ec.edu.uisek.calculator.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class CalculatorState(
    val display: String = "0"
)

sealed class CalculatorEvent {
    data class Number(val number: String) : CalculatorEvent()
    data class Operator(val operator: String) : CalculatorEvent()
    object Clear : CalculatorEvent()
    object AllClear : CalculatorEvent()
    object Calculate : CalculatorEvent()
    object Decimal : CalculatorEvent()
}

class CalculatorViewModel : ViewModel() {
    // --- Estado Interno del ViewModel (la lógica) ---
    private var number1: String = ""
    private var number2: String = ""
    private var operator: String? = null

    // --- Estado que observa la UI ---
    var state by mutableStateOf(CalculatorState())
        private set

    // El "router" de eventos, no cambia.
    fun onEvent(event: CalculatorEvent) {
        when (event) {
            is CalculatorEvent.Number -> enterNumber(event.number)
            is CalculatorEvent.Operator -> enterOperator(event.operator)
            is CalculatorEvent.Decimal -> enterDecimal()
            is CalculatorEvent.AllClear -> clearAll()
            is CalculatorEvent.Clear -> clearLast() // Lo mejoramos para que sea más inteligente
            is CalculatorEvent.Calculate -> performCalculation()
        }
    }

    private fun enterNumber(number: String) {
        if (operator == null) { // Estamos introduciendo el primer número
            number1 += number
            state = state.copy(display = number1)
        } else { // Estamos introduciendo el segundo número
            number2 += number
            state = state.copy(display = number2)
        }
    }

    // Función mejorada que maneja la entrada de un operador
    private fun enterOperator(op: String) {
        // Si ambos números (number1 y number2) ya tienen valores,
        // se realiza primero la operación pendiente antes de establecer un nuevo operador.
        if (number1.isNotBlank() && number2.isNotBlank()) {
            performCalculation() // Llama a la función que realiza el cálculo
        }

        // Si ya hay un primer número ingresado (number1 no está vacío),
        // se guarda el operador actual que el usuario ingresó.
        if (number1.isNotBlank()) {
            operator = op // Asigna el operador actual (por ejemplo '+', '−', '×', '÷')
        }
    }

    private fun enterDecimal() {
        val currentNumber = if (operator == null) number1 else number2
        if (!currentNumber.contains(".")) {
            if (operator == null) {
                number1 += "."
                state = state.copy(display = number1)
            } else {
                number2 += "."
                state = state.copy(display = number2)
            }
        }
    }

    private fun performCalculation() {
        val num1 = number1.toDoubleOrNull()
        val num2 = number2.toDoubleOrNull()

        if (num1 != null && num2 != null && operator != null) {
            val result = when (operator) {
                "+" -> num1 + num2
                "−" -> num1 - num2
                "×" -> num1 * num2
                "÷" -> if (num2 != 0.0) num1 / num2 else Double.NaN // Manejar división por cero
                else -> 0.0
            }

            // Preparamos para la siguiente operación
            clearAll()
            // Mostramos el resultado y lo guardamos como el primer número de la siguiente posible operación
            val resultString = if (result.isNaN()) "Error" else result.toString().removeSuffix(".0")
            number1 = if (result.isNaN()) "" else resultString
            state = state.copy(display = resultString)
        }
    }

    private fun clearLast() {
        // Borra el último dígito del número que se está escribiendo
        if (operator == null) {
            if (number1.isNotBlank()) {
                number1 = number1.dropLast(1)
                state = state.copy(display = if (number1.isBlank()) "0" else number1)
            }
        } else {
            if (number2.isNotBlank()) {
                number2 = number2.dropLast(1)
                state = state.copy(display = if (number2.isBlank()) "0" else number2)
            } else {
                // Si no hay segundo número, borramos el operador
                operator = null
                state = state.copy(display = number1)
            }
        }
    }

    private fun clearAll() {
        number1 = ""
        number2 = ""
        operator = null
        state = state.copy(display = "0")
    }
}
