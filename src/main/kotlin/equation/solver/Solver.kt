package equation.solver


import kotlin.math.sqrt

// Математический решатель уравнения
object NonlinearSolver {
    // Вычисление значения функции f(x) = √(x+a) - 1/x
    fun calculateFunction(x: Double, a: Double): Double? {
        if (x + a < 0) return null  // Проверка области определения
        if (x == 0.0) return null   // Защита от деления на ноль

        return try {
            sqrt(x + a) - 1.0 / x
        } catch (e: Exception) {
            null
        }
    }

    // Итерационная формула для метода итераций
    fun iterationFormula(x: Double, a: Double): Double? {
        return if (a >= 0) {
            // Для положительных a
            if (x <= 0 || x + a < 0) null
            else 1.0 / sqrt(x + a)
        } else {
            // Для отрицательных a
            if (x <= 0 || x + a < 0) null
            else 1.0 / (x * x) - a
        }
    }
}