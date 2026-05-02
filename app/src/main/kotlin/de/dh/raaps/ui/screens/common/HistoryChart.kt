package de.dh.raaps.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import de.dh.raaps.ui.composables.Blue200
import de.dh.raaps.ui.composables.BlueA200
import de.dh.raaps.ui.composables.Orange600
import de.dh.raaps.ui.composables.Red400

@Composable
private fun BasicLineChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
) {
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(),
            ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}

@Composable
fun BgHistoryChart(modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }

//    val glucoseSeries = LineSeries(
//        dataPoints = listOf(
//            ChartPoint(0f, 110f),
//            ChartPoint(1f, 150f),
//            ChartPoint(2f, 115f),
//            ChartPoint(3f, 120f),
//            ChartPoint(4f, 149f),
//            ChartPoint(5f, 125f)
//        ),
//        dotColor = BlueA200,
//        pathColor = Blue200,
//        interpolation = Interpolation.Spline
//    )
//
//    val highLine = LineSeries(
//        dataPoints = listOf(
//            ChartPoint(0f, 170f),
//            ChartPoint(5f, 170f)
//        ),
//        dotColor = Orange600,
//        pathColor = Orange600,
//        showPoints = false,
//        strokeWidth = 1.dp
//    )
//
//    val lowLine = LineSeries(
//        dataPoints = listOf(
//            ChartPoint(0f, 60f),
//            ChartPoint(5f, 60f)
//        ),
//        dotColor = Red400,
//        pathColor = Red400,
//        showPoints = false,
//        strokeWidth = 1.dp
//    )
//
//    val xAxis = AxisConfigBuilder()
//        .range(0f, 5f)
//        .steps(5)
//        .build()
//
//    val yAxis = AxisConfigBuilder()
//        .range(40f, 250f)
//        .steps(
//            4,
//            { _ , value -> "${value.toInt()}"}
//        )
//        .build()
//

    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            // Learn more: https://patrykandpatrick.com/z5ah6v.
            lineSeries { series(13, 8, 7, 12, 0, 1, 15, 14, 0, 11, 6, 12, 0, 11, 12, 11) }
        }
    }
    BasicLineChart(modelProducer, modifier)
}