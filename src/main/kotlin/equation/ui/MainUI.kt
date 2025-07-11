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
import org.jetbrains.letsPlot.Figure

@Composable
fun EquationApp() {
    var paramInput by remember { mutableStateOf("1.0") }
    var solution by remember { mutableStateOf<Solution?>(null) }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showDetail by remember { mutableStateOf(true) }

    MaterialTheme(colors = lightColors(
        primary = Color(0xFF1976D2),
        primaryVariant = Color(0xFF0D47A1),
        secondary = Color(0xFF03A9F4),
        background = Color(0xFFE3F2FD),
        surface = Color(0xFFBBDEFB)
    )) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header()

            ParameterInput(
                value = paramInput,
                loading = loading,
                onValueChange = { paramInput = it },
                onSolve = {
                    loading = true
                    error = ""
                    solution = null
                    findSolution(paramInput) { result, message ->
                        solution = result
                        error = message
                        loading = false
                    }
                }
            )

            if (error.isNotEmpty()) {
                ErrorMessage(error)
            }

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

@Composable
private fun Header() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Решение уравнения √(x + a) = 1/x",
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            ),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Text(
            text = "Точность вычислений: 0.001",
            color = MaterialTheme.colors.primary.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun ParameterInput(
    value: String,
    loading: Boolean,
    onValueChange: (String) -> Unit,
    onSolve: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
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

@Composable
private fun ErrorMessage(text: String) {
    Text(
        text = text,
        color = Color(0xFFD32F2F),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SolutionVisualization(
    solution: Solution?,
    loading: Boolean,
    showDetail: Boolean,
    onViewToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Визуализация решения:",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onViewToggle) {
                Icon(
                    imageVector = if (showDetail)
                        Icons.Filled.ZoomOut else
                        Icons.Filled.ZoomIn,
                    contentDescription = "Переключить вид",
                    tint = MaterialTheme.colors.primary
                )
            }
            Text(
                text = if (showDetail) "Детальный" else "Общий",
                color = MaterialTheme.colors.primary
            )
        }

        Card(
            elevation = 8.dp,
            backgroundColor = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(300.dp)
        ) {
            if (loading) {
                LoadingIndicator()
            } else if (solution == null) {
                Placeholder("Начните вычисления")
            } else {
                ChartRenderer.PlotView(
                    chart = if (showDetail) solution.detailedChart else solution.mainChart
                )
            }
        }

        SolutionDetails(solution)
    }
}

@Composable
private fun SolutionDetails(solution: Solution?) {
    Card(
        elevation = 4.dp,
        backgroundColor = Color(0xFFE1F5FE),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Результаты вычислений:",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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

@Composable
private fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.width(180.dp)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF01579B)
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colors.primary)
    }
}

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