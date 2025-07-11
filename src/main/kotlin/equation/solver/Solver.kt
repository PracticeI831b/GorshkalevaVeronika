package equation.solver

import kotlin.math.abs
import kotlin.math.sqrt

object NonlinearSolver {
    fun calculateFunction(x: Double, a: Double): Double? {
        if (x + a < 0) return null
        if (x == 0.0) return null

        return try {
            sqrt(x + a) - 1.0 / x
        } catch (e: Exception) {
            null
        }
    }

    fun iterationFormula(x: Double, a: Double): Double? {
        return if (a >= 0) {
            if (x <= 0 || x + a < 0) null
            else 1.0 / sqrt(x + a)
        } else {
            if (x <= 0 || x + a < 0) null
            else 1.0 / (x * x) - a
        }
    }
}