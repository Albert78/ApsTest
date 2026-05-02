package de.dh.raaps.ui.screens.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalBox
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Position
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import de.dh.raaps.core.api.data.BgSampleKind
import de.dh.raaps.core.api.data.Minutes
import de.dh.raaps.model.ApsTickState
import de.dh.raaps.ui.composables.Blue200
import de.dh.raaps.ui.composables.BlueA200
import de.dh.raaps.ui.composables.OrangeA200
import de.dh.raaps.ui.composables.Red400
import de.dh.raaps.ui.composables.Yellow
import java.util.Calendar
import java.util.Locale

private const val INITIAL_SHOW_HOURS = 4.0

@Composable
fun BgHistoryChart(
    tickStates: List<ApsTickState?>,
    tickInterval: Minutes,
    modifier: Modifier = Modifier,
    lowBgThreshold: Double = 70.0,
    highBgThreshold: Double = 170.0,
    onChartClick: (() -> Unit)? = null
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(tickStates) {
        modelProducer.runTransaction {
            lineSeries {
                val validIndices = tickStates.indices.filter {
                    val bg = tickStates[it]?.bg ?: return@filter false
                    return@filter bg.sampleKind != BgSampleKind.Invalid
                }
                // Series 1: Smoothed values (smoothedValue) - Drawn first (under)
                series(
                    x = validIndices.toList(),
                    y = validIndices.map { tickStates[it]!!.bg!!.smoothedValue.mgdl.toFloat() }
                )
                // Series 2: Raw values (origValue) - Drawn second (over)
                series(
                    x = validIndices.toList(),
                    y = validIndices.map { tickStates[it]!!.bg!!.origValue.mgdl.toFloat() }
                )
            }
        }
    }

    val xAxisValueFormatter = remember(tickStates, tickInterval) {
        CartesianValueFormatter { _, x, _ ->
            val state = tickStates.getOrNull(x.toInt()) ?: return@CartesianValueFormatter ""
            val calendar = Calendar.getInstance().apply {
                timeInMillis = state.tick.value.toLong() * tickInterval.value.toLong() * 60_000L
            }
            String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.HOUR_OF_DAY))
        }
    }

    val xItemPlacer = remember(tickStates, tickInterval) {
        val ticksPerHour = 60 / tickInterval.value.toInt()
        val firstTickValue = tickStates.firstOrNull()?.tick?.value ?: 0
        val offset = (ticksPerHour - (firstTickValue % ticksPerHour)) % ticksPerHour
        HorizontalAxis.ItemPlacer.aligned(spacing = { ticksPerHour }, offset = { offset })
    }

    val rangeProvider = remember {
        object : CartesianLayerRangeProvider {
            override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) = 40.0
            override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
                val baseMax = maxY.coerceAtLeast(200.0) + 10.0
                return baseMax.coerceAtMost(410.0)
            }
        }
    }

    val yAxisItemPlacer = remember {
        object : VerticalAxis.ItemPlacer {
            override fun getLabelValues(
                context: CartesianDrawingContext,
                axisHeight: Float,
                maxLabelHeight: Float,
                position: Axis.Position.Vertical,
            ): List<Double> = getValues(context.ranges.getYRange(position).maxY)

            override fun getWidthMeasurementLabelValues(
                context: CartesianMeasuringContext,
                axisHeight: Float,
                maxLabelHeight: Float,
                position: Axis.Position.Vertical,
            ): List<Double> = getValues(context.ranges.getYRange(position).maxY)

            override fun getHeightMeasurementLabelValues(
                context: CartesianMeasuringContext,
                position: Axis.Position.Vertical,
            ): List<Double> = getValues(context.ranges.getYRange(position).maxY)

            private fun getValues(maxY: Double): List<Double> {
                val values = mutableListOf<Double>()
                var current = 50.0
                while (current <= maxY) {
                    values.add(current)
                    current += 50.0
                }
                return values
            }

            override fun getTopLayerMargin(
                context: CartesianMeasuringContext,
                verticalLabelPosition: Position.Vertical,
                maxLabelHeight: Float,
                maxLineThickness: Float
            ): Float = 0f

            override fun getBottomLayerMargin(
                context: CartesianMeasuringContext,
                verticalLabelPosition: Position.Vertical,
                maxLabelHeight: Float,
                maxLineThickness: Float
            ): Float = 0f
        }
    }

    val scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End)
    val zoomState = rememberVicoZoomState(
        initialZoom = Zoom.x(INITIAL_SHOW_HOURS * 60.0 / tickInterval.value.toDouble())
    )

    val lowBgBox = rememberShapeComponent(fill = Fill(Red400.copy(alpha = 0.2f)))
    val highBgBox = rememberShapeComponent(fill = Fill(OrangeA200.copy(alpha = 0.2f)))

    val decorations = remember(lowBgThreshold, highBgThreshold, lowBgBox, highBgBox) {
        listOf(
            HorizontalBox(
                y = { 0.0..lowBgThreshold },
                box = lowBgBox
            ),
            HorizontalBox(
                y = { highBgThreshold..500.0 },
                box = highBgBox
            )
        ).map { decoration ->
            object : Decoration {
                override fun drawUnderLayers(context: CartesianDrawingContext) {
                    context.canvas.withSave {
                        context.canvas.clipRect(context.layerBounds)
                        decoration.drawUnderLayers(context)
                    }
                }

                override fun drawOverLayers(context: CartesianDrawingContext) {
                    context.canvas.withSave {
                        context.canvas.clipRect(context.layerBounds)
                        decoration.drawOverLayers(context)
                    }
                }
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    // Style for Series 1 (Yellow, Smoothed values)
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(Yellow)),
                        pointProvider = null // No dots for the smoothed line
                    ),
                    // Style for Series 2 (Blue, Raw values)
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(Blue200)),
                        pointProvider = LineCartesianLayer.PointProvider.single(
                            LineCartesianLayer.Point(
                                rememberShapeComponent(
                                    shape = CircleShape,
                                    fill = Fill(BlueA200)
                                ),
                                size = 6.dp
                            )
                        )
                    )
                ),
                rangeProvider = rangeProvider
            ),
            startAxis = VerticalAxis.rememberStart(itemPlacer = yAxisItemPlacer),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = xAxisValueFormatter,
                itemPlacer = xItemPlacer
            ),
            decorations = decorations,
        ),
        modelProducer = modelProducer,
        scrollState = scrollState,
        zoomState = zoomState,
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null, // No ripple to not interfere with chart visuals
            enabled = onChartClick != null,
            onClick = { onChartClick?.invoke() }
        ),
    )
}
