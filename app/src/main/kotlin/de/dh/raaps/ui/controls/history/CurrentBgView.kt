package de.dh.raaps.ui.controls.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.dh.raaps.core.api.data.BgValue
import de.dh.raaps.core.api.data.Timestamp
import de.dh.raaps.ui.composables.AppColorBlue
import de.dh.raaps.ui.composables.LightGreenA700
import de.dh.raaps.ui.composables.Red
import de.dh.raaps.ui.composables.Yellow
import de.dh.raaps.ui.screens.common.shortRelativeTimeAgo
import de.dh.raaps.ui.theme.ApsTheme
import java.util.Locale

@Composable
fun CurrentBgView(
    currentBgUiState: CurrentBgUiState,
    modifier: Modifier = Modifier
) {
    val bgTimestamp = currentBgUiState.timestamp.ms
    var diffMs by remember(bgTimestamp) { mutableLongStateOf(System.currentTimeMillis() - bgTimestamp) }
    LaunchedEffect(bgTimestamp) {
        while (true) {
            val now = System.currentTimeMillis()
            diffMs = now - bgTimestamp
            // Wait until the next 10s boundary relative to the BG timestamp
            val next10sBoundary = ((diffMs / 10000) + 1) * 10000
            val delayTime = next10sBoundary - diffMs
            kotlinx.coroutines.delay(delayTime)
        }
    }

    val bgColor = when {
        currentBgUiState.bgValue.mgdl < 50 -> Red
        currentBgUiState.bgValue.mgdl < 70 -> Yellow
        currentBgUiState.bgValue.mgdl < 180 -> LightGreenA700
        currentBgUiState.bgValue.mgdl < 250 -> Yellow
        else -> Red
    }

    val trendAngle = when (currentBgUiState.trend) {
        BgTrend.DoubleUp -> -60f
        BgTrend.SingleUp -> -45f
        BgTrend.FortyFiveUp -> -25f
        BgTrend.Flat -> 0f
        BgTrend.FortyFiveDown -> 25f
        BgTrend.SingleDown -> 45f
        BgTrend.DoubleDown -> 60f
        BgTrend.NotComputable -> 0f
    }

    Box(
        modifier = modifier
            .size(150.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val mainRadius = size.width * 0.4f
            val strokeWidth = 6.dp.toPx()

            // Pale Blue Ring
            drawCircle(
                color = AppColorBlue.copy(alpha = 0.3f),
                radius = mainRadius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            if (currentBgUiState.isValid) {
                // Highlight Arc around the trend
                drawArc(
                    color = AppColorBlue,
                    startAngle = trendAngle - 20f,
                    sweepAngle = 40f,
                    useCenter = false,
                    topLeft = Offset(center.x - mainRadius, center.y - mainRadius),
                    size = Size(mainRadius * 2, mainRadius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Trend Arrow
                rotate(degrees = trendAngle, pivot = center) {
                    val arrowPath = Path().apply {
                        val xBase = center.x + mainRadius
                        val yBase = center.y
                        val length = 6.dp.toPx()
                        val width = 15.dp.toPx()

                        moveTo(xBase, yBase - width / 2)
                        lineTo(xBase + length, yBase)
                        lineTo(xBase, yBase + width / 2)
                    }
                    drawPath(
                        path = arrowPath,
                        color = AppColorBlue,
                        style = Stroke(
                            width = 8.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        Column(
            modifier = Modifier.offset(y = (-6).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy((-6).dp)
        ) {
            val deltaText = if (currentBgUiState.isValid && currentBgUiState.isDeltaValid)
                currentBgUiState.delta.toDiff(currentBgUiState.glucoseUnit)
            else
                ""
            Text(
                text = deltaText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            val centerText = if (currentBgUiState.isValid) currentBgUiState.bgValue.toString(currentBgUiState.glucoseUnit) else "?"
            Text(
                text = centerText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 48.sp
                ),
                color = bgColor
            )
            val timeAgoText = if (currentBgUiState.isValid) {
                shortRelativeTimeAgo(diffMs)
            } else ""
            Text(
                text = timeAgoText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

fun createSampleGoodBgUiState(): CurrentBgUiState {
    return CurrentBgUiState(
        isLoading = false,
        isError = false,
        isValid = true,
        bgValue = BgValue(125),
        delta = BgValue(+10),
        isDeltaValid = true,
        trend = BgTrend.FortyFiveUp,
        timestamp = Timestamp.now().minusHours(2)
    )
}

@Preview(showBackground = true)
@Composable
fun BgInvalidViewPreview() {
    ApsTheme {
        CurrentBgView(
            currentBgUiState = CurrentBgUiState(
                isLoading = false,
                isError = false,
                isValid = false,
                bgValue = BgValue(325),
                delta = BgValue(+20),
                trend = BgTrend.DoubleUp,
                timestamp = Timestamp.now().minusMinutes(3)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BgVeryHighViewPreview() {
    ApsTheme {
        CurrentBgView(
            currentBgUiState = CurrentBgUiState(
                isLoading = false,
                isError = false,
                isValid = true,
                bgValue = BgValue(325),
                delta = BgValue(+20),
                trend = BgTrend.DoubleUp,
                timestamp = Timestamp.now().minusMinutes(3)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BgHighViewPreview() {
    ApsTheme {
        CurrentBgView(
            currentBgUiState = CurrentBgUiState(
                isLoading = false,
                isError = false,
                isValid = true,
                bgValue = BgValue(225),
                delta = BgValue(+15),
                isDeltaValid = true,
                trend = BgTrend.SingleUp,
                timestamp = Timestamp.now().minusSeconds(20)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BgGoodUpViewPreview() {
    ApsTheme {
        CurrentBgView(
            currentBgUiState = createSampleGoodBgUiState()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BgGoodViewPreview() {
    ApsTheme {
        CurrentBgView(
            currentBgUiState = CurrentBgUiState(
                isLoading = false,
                isError = false,
                isValid = true,
                bgValue = BgValue(125),
                delta = BgValue(+0),
                isDeltaValid = true
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BgLowViewPreview() {
    ApsTheme {
        CurrentBgView(
            currentBgUiState = CurrentBgUiState(
                isLoading = false,
                isError = false,
                isValid = true,
                bgValue = BgValue(60),
                delta = BgValue(-5),
                trend = BgTrend.FortyFiveDown
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BgVeryLowViewPreview() {
    ApsTheme {
        CurrentBgView(
            currentBgUiState = CurrentBgUiState(
                isLoading = false,
                isError = false,
                isValid = true,
                bgValue = BgValue(45),
                delta = BgValue(-10),
                isDeltaValid = true,
                trend = BgTrend.SingleDown
            )
        )
    }
}
