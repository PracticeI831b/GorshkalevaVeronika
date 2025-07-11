package equation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import equation.solver.Solution
import equation.solver.findSolution

// Основной UI компонент приложения
@Composable
fun EquationApp() {
    // Состояния приложения
    var paramInput by remember { mutableStateOf("1.0") } // Ввод параметра
    var solution by remember { mutableStateOf<Solution?>(null) } // Решение
    var error by remember { mutableStateOf("") } // Ошибка
    var loading by remember { mutableStateOf(false) } // Флаг загрузки
    var showDetail by remember { mutableStateOf(true) } // Режим отображения графика

    // Цветовая схема (синяя тема)
    MaterialTheme(colors = lightColors(
            primary = Color(0xFF0D47A1),       // Основной синий
            primaryVariant = Color(0xFF002171), // Темный синий
            secondary = Color(0xFF0277BD),     // Светлый синий
            background = Color(0xFFE3F2FD),    // Фоновый голубой
            surface = Color(0xFFFFFFFF)        // Белый для поверхностей
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Заголовок приложения
            Header()

            // Панель ввода параметра
            ParameterInput(
                value = paramInput,
                loading = loading,
                onValueChange = { paramInput = it },
                onSolve = {
                    loading = true
                    error = ""
                    solution = null
                    // Запуск вычислений
                    findSolution(paramInput) { result, message ->
                        solution = result
                        error = message
                        loading = false
                    }
                }
            )

            // Отображение ошибок
            if (error.isNotEmpty()) {
                ErrorMessage(error)
            }

            // Отображение результатов
            if (solution != null || loading) {
                SolutionVisualization(
                    solution = solution,
                    loading = loading,
                    showDetail = showDetail,
                    onViewToggle = { showDetail = !showDetail }
                )
            }
        }
    }
}

// Заголовок приложения
@Composable
private fun Header() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Название уравнения
        Text(
            text = "Решение уравнения √(x + a) = 1/x",
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            ),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Карточка с точностью вычислений
        Card(
            backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
            elevation = 0.dp,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "Точность: 0.001",
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 14.sp
            )
        }
    }
}

// Панель ввода параметра
@Composable
private fun ParameterInput(
    value: String,                 // Текущее значение
    loading: Boolean,              // Флаг загрузки
    onValueChange: (String) -> Unit, // Обработчик изменения
    onSolve: () -> Unit            // Обработчик вычисления
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Поле ввода параметра
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Введите параметр a") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = !loading,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = MaterialTheme.colors.secondary
            )
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Кнопка запуска вычислений
        Button(
            onClick = onSolve,
            enabled = !loading,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White
            ),
            modifier = Modifier.height(56.dp)
        ) {
            Icon(Icons.Filled.Calculate, "Вычислить")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Решить")
        }
    }
}

// Отображение сообщений об ошибках
@Composable
private fun ErrorMessage(text: String) {
    Text(
        text = text,
        color = Color(0xFFD32F2F), // Красный цвет ошибки
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

// Визуализация решения
@Composable
private fun SolutionVisualization(
    solution: Solution?,     // Результаты вычислений
    loading: Boolean,        // Флаг загрузки
    showDetail: Boolean,     // Флаг детализации
    onViewToggle: () -> Unit // Переключение режима
) {
    Column(
        modifier = Modifierit
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        // Панель управления видом графика
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Вид графика:",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.weight(1f)
            )

            // Текущий режим
            Text(
                text = if (showDetail) "Детальный" else "Общий",
                color = MaterialTheme.colors.primary
            )

            // Кнопка переключения режима
            IconButton(onClick = onViewToggle) {
                Icon(
                    imageVector = if (showDetail)
                        Icons.Filled.ZoomOut else // Иконка уменьшения
                        Icons.Filled.ZoomIn,      // Иконка увеличения
                    contentDescription = "Переключить вид",
                    tint = MaterialTheme.colors.primary
                )
            }
        }

        // Область отображения графика
        Card(
            elevation = 8.dp,
            backgroundColor = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(300.dp)
        ) {
            when {
                loading -> LoadingIndicator()
                solution == null -> Placeholder("Начните вычисления")
                else -> ChartRenderer.PlotView(
                    chart = if (showDetail) solution.detailedChart else solution.mainChart
                )
            }
        }

        // Детали решения
        SolutionDetails(solution)
    }
}

// Панель с результатами вычислений
@Composable
private fun SolutionDetails(solution: Solution?) {
    Card(
        elevation = 4.dp,
        backgroundColor = Color(0xFFE1F5FE), // Светло-голубой фон
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок результатов
            Text(
                text = "Результаты вычислений:",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Отображение результатов или заглушки
            if (solution != null) {
                DetailItem("Найденный корень:", "%.5f".format(solution.rootValue))
                DetailItem("Значение функции:", "%.7f".format(solution.functionValue))
                DetailItem("Шагов вычислений:", solution.steps.toString())
                DetailItem("Начальное приближение:", "%.5f".format(solution.initialApproximation))
            } else {
                Text("Данные отсутствуют", color = Color.Gray)
            }
        }
    }
}

// Элемент детализации результата
@Composable
private fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        // Название параметра
        Text(
            text = label,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.width(180.dp)
        )
        // Значение параметра
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF01579B) // Темно-синий цвет
        )
    }
}

// Индикатор загрузки
@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colors.primary)
    }
}

// Заглушка для пустых состояний
@Composable
private fun Placeholder(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colors.primary.copy(alpha = 0.6f)
        )
    }
}