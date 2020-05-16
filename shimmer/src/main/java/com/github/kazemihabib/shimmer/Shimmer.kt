package com.github.kazemihabib.shimmer

import androidx.animation.AnimationClockObservable
import androidx.compose.Composable
import androidx.compose.remember
import androidx.ui.animation.asDisposableClock
import androidx.ui.core.*
import androidx.ui.geometry.Rect
import androidx.ui.graphics.Paint
import androidx.ui.graphics.painter.drawCanvas
import androidx.ui.graphics.withSaveLayer
import androidx.ui.unit.PxSize
import androidx.ui.unit.ipx

@Composable
fun Modifier.shimmer(
    durationMs: Int = 3000,
    delay: Int = 0,
    repeatMode: RepeatMode = RepeatMode.RESTART,
    clock: AnimationClockObservable? = null
): Modifier = composed {
    @Suppress("NAME_SHADOWING") // don't allow usage of the parameter clock, only the disposable
    val clock = (clock ?: AnimationClockAmbient.current).asDisposableClock()
    val theme = ShimmerThemeAmbient.current

    val shimmerModifier =
        remember(theme, delay, durationMs, repeatMode, clock) { //TODO("Decrease the reallocation")
            ShimmerModifier(
                shimmerTheme = theme,
                durationMs = durationMs,
                delay = delay,
                repeatMode = repeatMode,
                clock = clock
            )
        }

    shimmerModifier
}

internal class ShimmerModifier(
    val shimmerTheme: ShimmerTheme,
    val durationMs: Int,
    val delay: Int,
    val repeatMode: RepeatMode,
    clock: AnimationClockObservable
) : DrawModifier, LayoutModifier {

    private var size: PxSize = PxSize(0.ipx, 0.ipx)
    private val paint = Paint()
    private val factory: ShimmerEffect = shimmerTheme.run {
        factory.create(
            baseAlpha = baseAlpha,
            durationMs = durationMs,
            repeatMode = repeatMode,
            tilt = tilt,
            dropOff = dropOff,
            intensity = intensity,
            highlightAlpha = highlightAlpha,
            direction = direction,
            delay = delay,
            clock = clock
        )
    }

    override fun ContentDrawScope.draw() {
        drawCanvas { canvas, _ ->
            canvas.withSaveLayer(
                Rect(
                    0f,
                    0f,
                    this@ShimmerModifier.size.width.value,
                    this@ShimmerModifier.size.height.value
                ), paint
            ) {
                drawContent()
                factory.draw(
                    canvas,
                    this@ShimmerModifier.size
                )
            }
        }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
        layoutDirection: LayoutDirection
    ): MeasureScope.MeasureResult {
        val placeable = measurable.measure(constraints)
        size = PxSize(width = placeable.width, height = placeable.height)
        factory.updateSize(size)
        return layout(placeable.width, placeable.height) {
            placeable.place(0.ipx, 0.ipx)
        }
    }
}

enum class RepeatMode {
    RESTART,
    REVERSE
}

