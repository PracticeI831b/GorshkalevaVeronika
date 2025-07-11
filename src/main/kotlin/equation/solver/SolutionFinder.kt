package equation.solver

import equation.ui.ChartRenderer
import org.jetbrains.letsPlot.Figure
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign

data class Solution(
    val rootValue: Double,
    val steps: Int,
    val initialApproximation: Double,
    val functionValue: Double,
    val mainChart: Figure,
    val detailedChart: Figure
)

private const val PRECISION = 0.001
private const val MAX_STEPS = 5000

fun findSolution(
    parameter: String,
    onResult: (Solution?, String) -> Unit
) {
    try {
        val a = parameter.replace(',', '.').toDouble()
        val lowerBound = max(0.0, -a) + 1e-5

        val (interval, initial) = findSolutionInterval(lowerBound, a)
            ?: return onResult(null, "Решение не найдено в интервале [${"%.2f".format(lowerBound)}, 100]")

        val (root, iterations) = solveByIterations(initial, a)
            ?: return onResult(null, "Не удалось найти решение для начального значения ${"%.4f".format(initial)}")

        val fValue = NonlinearSolver.calculateFunction(root, a) ?: 0.0

        val chartRange = max(max(abs(root) * 1.5, abs(initial) * 1.5), 10.0)
        val mainChart = ChartRenderer.createChart(
            -chartRange,
            chartRange,
            a,
            "√(x+$a) = 1/x",
            intervals = listOf(interval),
            initialPoints = listOf(initial),
            roots = listOf(root),
            zoomed = false
        )
        val detailedChart = createDetailedChart(root, a, lowerBound, interval, initial, root)

        onResult(Solution(root, iterations, initial, fValue, mainChart, detailedChart), "")
    } catch (e: Exception) {
        onResult(null, "Ошибка: ${e.message}")
    }
}

private fun findSolutionInterval(low: Double, a: Double): Pair<Pair<Double, Double>, Double>? {
    var x1 = if (a < 0) abs(a) + 1e-5 else low
    var x2 = x1 + 1.0
    var f1 = NonlinearSolver.calculateFunction(x1, a)
    var f2 = NonlinearSolver.calculateFunction(x2, a)

    while (f1 != null && f2 != null && f1.sign == f2.sign && x2 < 100.0) {
        x1 = x2
        x2 += 1.0
        f1 = f2
        f2 = NonlinearSolver.calculateFunction(x2, a)
    }

    if (f1 == null || f2 == null || f1.sign == f2.sign) return null

    val startPoint = (x1 + x2) / 2
    return (x1 to x2) to startPoint
}

private fun solveByIterations(x0: Double, a: Double): Pair<Double, Int>? {
    var current = x0
    var count = 0

    while (count < MAX_STEPS) {
        val next = NonlinearSolver.iterationFormula(current, a) ?: return null
        if (abs(next - current) < PRECISION) return next to count

        current = next
        count++
    }
    return null
}

private fun createDetailedChart(
    root: Double,
    a: Double,
    lowBound: Double,
    interval: Pair<Double, Double>,
    initial: Double,
    iterRoot: Double
): Figure {
    val margin = 0.5
    val viewStart = max(lowBound, root - margin)
    val viewEnd = root + margin

    return ChartRenderer.createChart(
        viewStart, viewEnd, a,
        "√(x+$a) = 1/x (детальный вид)",
        listOf(interval),
        listOf(initial),
        listOf(iterRoot),
        zoomed = true
    )
}