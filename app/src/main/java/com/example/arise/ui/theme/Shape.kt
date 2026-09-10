package com.example.arise.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

// Sharp corners globally — no soft rounded corners
val AriseShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

/**
 * 45° chamfered corner shape — the signature sci-fi HUD look.
 * Each corner is cut at 45 degrees instead of rounded.
 */
class SciFiCutCornerShape(private val cutSizePx: Float = 24f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cut = cutSizePx.coerceAtMost(minOf(size.width, size.height) / 4f)
        val path = Path().apply {
            moveTo(cut, 0f)
            lineTo(size.width - cut, 0f)
            lineTo(size.width, cut)
            lineTo(size.width, size.height - cut)
            lineTo(size.width - cut, size.height)
            lineTo(cut, size.height)
            lineTo(0f, size.height - cut)
            lineTo(0f, cut)
            close()
        }
        return Outline.Generic(path)
    }
}
