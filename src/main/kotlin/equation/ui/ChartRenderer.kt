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

// Отрисовщик графиков
object ChartRenderer {
    // Компонент для отображения графика
    @Composable
    fun PlotView(chart: Figure) {
        PlotPanel(
            figure = chart,
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
        ) { messages -> messages.forEach { println(it) } }
    }

    // Создание графика функции
    fun createChart(
        xStart: Double,         // Начало интервала по X
        xEnd: Double,           // Конец интервала по X
        a: Double,              // Параметр уравнения
        title: String,          // Заголовок графика
        intervals: List<Pair<Double, Double>>?, // Интервалы поиска
        initialPoints: List<Double>?, // Начальные точки
        roots: List<Double>,    // Найденные корни
        zoomed: Boolean         // Флаг детализации
    ): Figure {
        // Генерация точек для построения графика
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

        // Расчет границ по Y
        val (yMin, yMax) = calculateYRange(yValues, roots, initialPoints, a, zoomed)

        // Построение основного графика
        var chart = ggplot(mapOf("x" to xValues, "y" to yValues)) +
                geomLine(color = "#1E88E5", size = 1.2) { x = "x"; y = "y" } + // Линия функции
                geomHLine(yintercept = 0.0, color = "#78909C", linetype = "dashed") + // Горизонтальная ось
                geomVLine(xintercept = 0.0, color = "#78909C", linetype = "dashed") + // Вертикальная ось
                ggtitle(title) + // Заголовок
                scaleXContinuous("x", limits = xStart to xEnd) // Шкала X

        // Настройка шкалы Y в зависимости от режима
        if (zoomed) {
            chart = chart + scaleYContinuous("f(x)", limits = yMin to yMax)
        } else {
            val range = maxOf(abs(yMin), abs(yMax), 0.1)
            chart = chart + scaleYContinuous("f(x)", limits = -range to range)
        }

        // Добавление интервалов поиска
        intervals?.forEach { (start, end) ->
            chart = chart +
                    geomVLine(xintercept = start, color = "#FF9800", alpha = 0.7) + // Начало интервала
                    geomVLine(xintercept = end, color = "#FF9800", alpha = 0.7) // Конец интервала
        }

        // Подготовка данных для точек
        val pointsX = mutableListOf<Double>()
        val pointsY = mutableListOf<Double>()
        val pointTypes = mutableListOf<String>()

        // Добавление начальных точек
        initialPoints?.forEach { x0 ->
            NonlinearSolver.calculateFunction(x0, a)?.let { y0 ->
                pointsX.add(x0)
                pointsY.add(y0)
                pointTypes.add("Начальное значение")
            }
        }

        // Добавление корней
        roots.forEach { root ->
            NonlinearSolver.calculateFunction(root, a)?.let { yRoot ->
                pointsX.add(root)
                pointsY.add(yRoot)
                pointTypes.add("Решение")
            }
        }

        // Отображение точек на графике
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
                    scaleColorManual( // Цвета для разных типов точек
                        values = listOf("#7CB342", "#D81B60"), // Зеленый для начальных, розовый для решений
                        name = "Точки"
                    )
        }

        return chart
    }

    // Расчет границ по оси Y
    private fun calculateYRange(
        yValues: List<Double>,    // Значения функции
        roots: List<Double>,      // Корни
        initials: List<Double>?,  // Начальные точки
        a: Double,                // Параметр
        zoomed: Boolean           // Флаг детализации
    ): Pair<Double, Double> {
        if (yValues.isEmpty()) return -1.0 to 1.0

        var yMin = yValues.minOrNull() ?: -1.0
        var yMax = yValues.maxOrNull() ?: 1.0

        // Для детализированного вида корректируем границы
        if (zoomed) {
            // Фильтрация выбросов
            val filtered = yValues.filter { abs(it) < 1000 }
            if (filtered.isNotEmpty()) {
                yMin = filtered.minOrNull()!!
                yMax = filtered.maxOrNull()!!
            }

            // Учет значений в особых точках
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

            // Добавление отступов
            val padding = max(0.1, (yMax - yMin) * 0.15)
            yMin -= padding
            yMax += padding
        }

        // Гарантия минимальной высоты графика
        if (abs(yMax - yMin) < 0.1) {
            val center = (yMin + yMax) / 2
            yMin = center - 0.05
            yMax = center + 0.05
        }

        return yMin to yMax
    }
}