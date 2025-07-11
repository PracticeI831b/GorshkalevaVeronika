import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import equation.ui.EquationApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Решение нелинейных уравнений"
    ) {
        EquationApp()
    }
}