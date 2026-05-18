package com.example.emogrow.features.game.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

private val zoneConfig = mapOf(
    ZoneId.LEFT_EYE to ZoneSpec(PartType.EYE, 64.dp, 56.dp, (-48).dp, (-55).dp),
    ZoneId.RIGHT_EYE to ZoneSpec(PartType.EYE, 64.dp, 56.dp, 48.dp, (-55).dp),
    ZoneId.LEFT_EYEBROW to ZoneSpec(PartType.EYEBROW, 60.dp, 28.dp, (-48).dp, (-83).dp),
    ZoneId.RIGHT_EYEBROW to ZoneSpec(PartType.EYEBROW, 60.dp, 28.dp, 48.dp, (-83).dp),
    ZoneId.LEFT_CHEEK to ZoneSpec(PartType.CHEEK, 52.dp, 40.dp, (-80).dp, 20.dp),
    ZoneId.RIGHT_CHEEK to ZoneSpec(PartType.CHEEK, 52.dp, 40.dp, 80.dp, 20.dp),
    ZoneId.NOSE to ZoneSpec(PartType.NOSE, 56.dp, 52.dp, 0.dp, (-3).dp),
    ZoneId.MOUTH to ZoneSpec(PartType.MOUTH, 96.dp, 60.dp, 0.dp, 55.dp),
    ZoneId.SWEAT to ZoneSpec(PartType.SWEAT, 28.dp, 40.dp, 72.dp, 5.dp)
)

private val faceShape = GenericShape { size, _ ->
    // Tạo đường viền khuôn mặt mềm hơn hình tròn: trán rộng, cằm thu nhẹ xuống dưới.
    val width = size.width
    val height = size.height
    moveTo(width * 0.50f, 0f)
    cubicTo(width * 0.84f, 0f, width * 1.00f, height * 0.28f, width * 0.96f, height * 0.52f)
    cubicTo(width * 0.94f, height * 0.82f, width * 0.70f, height * 0.98f, width * 0.50f, height)
    cubicTo(width * 0.30f, height * 0.98f, width * 0.06f, height * 0.82f, width * 0.04f, height * 0.52f)
    cubicTo(width * 0.00f, height * 0.28f, width * 0.16f, 0f, width * 0.50f, 0f)
    close()
}

private data class ZoneSpec(
    val accepts: PartType,
    val width: Dp,
    val height: Dp,
    val x: Dp,
    val y: Dp
)

@Composable
fun FaceCanvas(
    placedParts: Map<ZoneId, FacePart?>,
    draggedPart: FacePart?,
    dragPosition: Offset,
    wrongZones: Set<ZoneId>,
    requiredZones: Set<ZoneId>,
    onZonePositioned: (ZoneId, Offset) -> Unit,
    onPlacedPartTap: (ZoneId) -> Unit
) {
    val faceWidth = GameDesign.faceSize * 0.85f
    val faceHeight = GameDesign.faceSize * 1.02f

    Box(
        modifier = Modifier
            .size(faceWidth, faceHeight)
            .shadow(elevation = 12.dp, shape = faceShape)
            .background(GameDesign.faceBg, faceShape)
            .border(3.dp, GameDesign.borderGold, faceShape),
        contentAlignment = Alignment.Center
    ) {
        ZoneId.entries.filter { zoneId -> requiredZones.contains(zoneId) }.forEach { zoneId ->
            val spec = zoneConfig.getValue(zoneId)
            FaceDropZone(
                modifier = Modifier
                    .size(spec.width, spec.height)
                    .offset(spec.x, spec.y),
                zoneId = zoneId,
                placedPart = placedParts[zoneId],
                draggedPart = draggedPart,
                dragPosition = dragPosition,
                wrongZones = wrongZones,
                accepts = spec.accepts,
                onZonePositioned = onZonePositioned,
                onPlacedPartTap = onPlacedPartTap
            )
        }
    }
}

@Composable
private fun BoxScope.FaceDropZone(
    modifier: Modifier,
    zoneId: ZoneId,
    placedPart: FacePart?,
    draggedPart: FacePart?,
    dragPosition: Offset,
    wrongZones: Set<ZoneId>,
    accepts: PartType,
    onZonePositioned: (ZoneId, Offset) -> Unit,
    onPlacedPartTap: (ZoneId) -> Unit
) {
    val pulse by rememberInfiniteTransition(label = "zonePulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(animation = tween(650), repeatMode = RepeatMode.Reverse),
        label = "zonePulseScale"
    )
    val placedScale by animateFloatAsState(
        targetValue = if (placedPart == null) 0f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 260f),
        label = "placedScale"
    )

    var isNearByDrag by remember(zoneId) { mutableStateOf(false) }
    val isWrong = zoneId in wrongZones
    val wrongAlpha = if (isWrong) {
        // Khi ghép sai, nhấp nháy viền đỏ để bé nhận ra zone cần sửa.
        val transition = rememberInfiniteTransition(label = "wrongZonePulse")
        transition.animateFloat(
            initialValue = 0.65f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(300),
                repeatMode = RepeatMode.Reverse
            ),
            label = "wrongZoneAlpha"
        ).value
    } else {
        0f
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val center = coordinates.positionInRoot() + Offset(
                    x = coordinates.size.width / 2f,
                    y = coordinates.size.height / 2f
                )
                onZonePositioned(zoneId, center)
                // Kiểm tra vị trí kéo để bật glow cho đúng ô phù hợp.
                isNearByDrag = draggedPart != null && draggedPart.type == accepts &&
                    dragPosition.getDistanceTo(center) <= 90f
            }
            .graphicsLayer {
                if (isNearByDrag) {
                    scaleX = pulse
                    scaleY = pulse
                }
            }
            .clickable(enabled = placedPart != null) { onPlacedPartTap(zoneId) },
        contentAlignment = Alignment.Center
    ) {
        if (placedPart == null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val stroke = 2.5.dp.toPx()
                        val inset = stroke / 2f
                        drawRoundRect(
                            color = if (isNearByDrag) hoverColor(draggedPart?.emotion) else Color(0xFFCCBB88),
                            topLeft = Offset(inset, inset),
                            size = Size(size.width - stroke, size.height - stroke),
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = stroke,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                            )
                        )
                    }
            )
        } else {
            FacePartDrawing(
                part = placedPart,
                modifier = Modifier
                    .size(
                        width = when (placedPart.type) {
                            PartType.EYE -> 56.dp
                            PartType.NOSE -> 60.dp
                            PartType.MOUTH -> 104.dp
                            PartType.EYEBROW -> 52.dp
                            PartType.CHEEK -> 44.dp
                            PartType.SWEAT -> 24.dp
                        },
                        height = when (placedPart.type) {
                            PartType.EYE -> 50.dp
                            PartType.NOSE -> 56.dp
                            PartType.MOUTH -> 64.dp
                            PartType.EYEBROW -> 24.dp
                            PartType.CHEEK -> 32.dp
                            PartType.SWEAT -> 34.dp
                        }
                    )
                    .graphicsLayer {
                        scaleX = placedScale
                        scaleY = placedScale
                    },
                isMirrored = zoneId == ZoneId.RIGHT_EYE || zoneId == ZoneId.RIGHT_EYEBROW
            )

            if (isWrong) {
                // Đè lớp đỏ lên zone sai để báo vị trí cần sửa.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 3.dp,
                            color = Color(0xFFFF4757).copy(alpha = wrongAlpha),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = Color(0xFFFF4757).copy(alpha = 0.15f * wrongAlpha),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }
        }
    }
}

private fun hoverColor(emotion: EmotionType?): Color = when (emotion) {
    EmotionType.HAPPY -> Color(0xFFFF9500)
    EmotionType.SAD -> Color(0xFF0984E3)
    EmotionType.ANGRY -> Color(0xFFD63031)
    EmotionType.SURPRISED -> Color(0xFF9B59B6)
    EmotionType.SCARED -> Color(0xFF2ECC71)
    EmotionType.WORRIED -> Color(0xFF636E72)
    EmotionType.SHY -> Color(0xFFF368E0)
    EmotionType.PROUD -> Color(0xFFFFD32A)
    EmotionType.LOVE -> Color(0xFFFF6B9E)
    EmotionType.CALM -> Color(0xFF3AAED8)
    EmotionType.TIRED -> Color(0xFF7C73E6)
    EmotionType.LONELY -> Color(0xFF627D98)
    EmotionType.CONFUSED -> Color(0xFFF59E0B)
    EmotionType.JEALOUS -> Color(0xFFD63031)
    EmotionType.EXCITED -> Color(0xFFFF9500)
    null -> Color(0xFFE8C97A)
}

private fun Offset.getDistanceTo(other: Offset): Float {
    val dx = x - other.x
    val dy = y - other.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}




