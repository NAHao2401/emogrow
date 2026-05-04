package com.example.emogrow.features.game.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.emogrow.R

@Composable
fun FacePartDrawing(
    part: FacePart,
    modifier: Modifier = Modifier,
    isMirrored: Boolean = false
) {
    // Use part.side if available, otherwise fall back to isMirrored parameter
    val shouldMirror = if (part.side != EyeSide.NONE) part.side == EyeSide.RIGHT else isMirrored
    
    val resId: Int = when {
        part.id == "eye_happy" && !shouldMirror -> R.drawable.part_eye_happy_left
        part.id == "eye_happy" && shouldMirror -> R.drawable.part_eye_happy_right
        part.id == "eye_sad" && !shouldMirror -> R.drawable.part_eye_sad_left
        part.id == "eye_sad" && shouldMirror -> R.drawable.part_eye_sad_right
        part.id == "eye_angry" && !shouldMirror -> R.drawable.part_eye_angry_left
        part.id == "eye_angry" && shouldMirror -> R.drawable.part_eye_angry_right
        part.id == "nose_basic" -> R.drawable.part_nose_normal
        part.id == "mouth_smile" -> R.drawable.part_mouth_happy
        part.id == "mouth_frown" -> R.drawable.part_mouth_sad
        part.id == "mouth_angry" -> R.drawable.part_mouth_angry
        else -> R.drawable.part_eye_happy_left
    }

    Image(
        painter = painterResource(id = resId),
        contentDescription = part.label,
        contentScale = ContentScale.Fit,
        // Giữ chiều cao tự nhiên để ảnh PNG không bị bóp hoặc cắt khi render trên mặt.
        modifier = modifier
    )
}

