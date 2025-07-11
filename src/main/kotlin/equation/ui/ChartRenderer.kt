package equation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import equation.solver.NonlinearSolver
import org.jetbrains.letsPlot.Figure
import org.jetbrains.letsPlot.geom.*
import org.jetbrains.letsPlot.ggplot
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.scale.scaleColorManual
import org.jetbrains.letsPlot.scale.scaleXContinuous
import org.jetbrains.letsPlot.scale.scaleYContinuous
import org.jetbrains.letsPlot.skia.compose.PlotPanel
import kotlin.math.abs
import kotlin.math.max

object ChartRenderer {
    @Composable
    fun PlotView(chart: Figure) {
        PlotPanel(
            figure = chart,
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
        ) { messages -> messages.forEach { println(it) } }
    }

    fun createChart(
        xStart: Double,
        xEnd: Double,
        a: Double,
        title: String,
        intervals: List<Pair<Double, Double>>?,
        initialPoints: List<Double>?,
        roots: List<Double>,
        zoomed: Boolean
    ): Figure {
        val pointsCount = 800
        val xValues = mutableListOf<Double>()
        val yValues = mutableListOf<Double>()

        for (i in 0..pointsCount) {
            val x = xStart + i * (xEnd - xStart) / pointsCount
            NonlinearSolver.calculateFunction(x, a)?.let {
                xValues.add(x)
                yValues.add(it)
            }
        }

        val (yMin, yMax) = calculateYRange(yValues, roots, initialPoints, a, zoomed)

        var chart = ggplot(mapOf("x" to xValues, "y" to yValues)) +
                geomLine(color = "#1E88E5", size = 1.2) { x = "x"; y = "y" } +
                geomHLine(yintercept = 0.0, color = "#78909C", linetype = "dashed") +
                geomVLine(xintercept = 0.0, color = "#78909C", linetype = "dashed") +
                ggtitle(title) +
                scaleXContinuous("x", limits = xStart to xEnd)

        if (zoomed) {
            chart = chart + scaleYContinuous("f(x)", limits = yMin to yMax)
        } else {
            val range = maxOf(abs(yMin), abs(yMax), 0.1)
            chart = chart + scaleYContinuous("f(x)", limits = -range to range)
        }

        intervals?.forEach { (start, end) ->
            chart = chart +
                    geomVLine(xintercept = start, color = "#FF9800", alpha = 0.7) +
                    geomVLine(xintercept = end, color = "#FF9800", alpha = 0.7)
        }

        val pointsX = mutableListOf<Double>()
        val pointsY = mutableListOf<Double>()
        val pointTypes = mutableListOf<String>()

        initialPoints?.forEach { x0 ->
            NonlinearSolver.calculateFunction(x0, a)?.let { y0 ->
                pointsX.add(x0)
                pointsY.add(y0)
                pointTypes.add("Начальное значение")
            }
        }

        roots.forEach { root ->
            NonlinearSolver.calculateFunction(root, a)?.let { yRoot ->
                pointsX.add(root)
                pointsY.add(yRoot)
                pointTypes.add("Решение")
            }
        }

        if (pointsX.isNotEmpty()) {
            val pointsData = mapOf(
                "x" to pointsX,
                "y" to pointsY,
                "type" to pointTypes
            )

            chart = chart + geomPoint(
                data = pointsData,
                size = 5.0,
                alpha = 0.9
            ) { x = "x"; y = "y"; color = "type" } +
                    scaleColorManual(
                        values = listOf("#7CB342", "#D81B60"),
                        name = "Точки"
                    )
        }

        return chart
    }

    private fun calculateYRange(
        yValues: List<Double>,
        roots: List<Double>,
        initials: List<Double>?,
        a: Double,
        zoomed: Boolean
    ): Pair<Double, Double> {
        if (yValues.isEmpty()) return -1.0 to 1.0

        var yMin = yValues.minOrNull() ?: -1.0
        var yMax = yValues.maxOrNull() ?: 1.0

        if (zoomed) {
            val filtered = yValues.filter { abs(it) < 1000 }
            if (filtered.isNotEmpty()) {
                yMin = filtered.minOrNull()!!
                yMax = filtered.maxOrNull()!!
            }

            val specialValues = mutableListOf<Double>()
            roots.forEach { root ->
                NonlinearSolver.calculateFunction(root, a)?.let { specialValues.add(it) }
            }
            initials?.forEach { init ->
                NonlinearSolver.calculateFunction(init, a)?.let { specialValues.add(it) }
            }

            if (specialValues.isNotEmpty()) {
                yMin = minOf(yMin, specialValues.minOrNull()!!)
                yMax = maxOf(yMax, specialValues.maxOrNull()!!)
            }

            val padding = max(0.1, (yMax - yMin) * 0.15)
            yMin -= padding
            yMax += padding
        }

        if (abs(yMax - yMin) < 0.1) {
            val center = (yMin + yMax) / 2
            yMin = center - 0.05
            yMax = center + 0.05
        }

        return yMin to yMax
    }
}