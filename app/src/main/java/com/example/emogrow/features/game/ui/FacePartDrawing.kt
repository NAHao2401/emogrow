package com.example.emogrow.features.game.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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

    val context = LocalContext.current
    val resName = when (part.id) {
        "eye_happy" -> if (!shouldMirror) "part_eye_happy_left" else "part_eye_happy_right"
        "eye_happy_left" -> "part_eye_happy_left"
        "eye_happy_right" -> "part_eye_happy_right"
        "eye_sad" -> if (!shouldMirror) "part_eye_sad_left" else "part_eye_sad_right"
        "eye_sad_left" -> "part_eye_sad_left"
        "eye_sad_right" -> "part_eye_sad_right"
        "eye_angry" -> if (!shouldMirror) "part_eye_angry_left" else "part_eye_angry_right"
        "eye_angry_left" -> "part_eye_angry_left"
        "eye_angry_right" -> "part_eye_angry_right"
        "eye_scared" -> if (!shouldMirror) "part_eye_scared_left" else "part_eye_scared_right"
        "eye_surprised_left" -> "part_eye_surprised_left"
        "eye_surprised_right" -> "part_eye_surprised_right"
        "eye_worried_left" -> "part_eye_worried_left"
        "eye_worried_right" -> "part_eye_worried_right"
        "eye_shy_left" -> "part_eye_shy_left"
        "eye_shy_right" -> "part_eye_shy_right"
        "eye_proud" -> "part_eye_proud"
        "eyebrows_scared_left" -> "part_eyebrows_scared_left"
        "eyebrows_scared_right" -> "part_eyebrows_scared_right"
        "eyebrows_shy_left" -> "part_eyebrows_shy_left"
        "eyebrows_shy_right" -> "part_eyebrows_shy_right"
        "eyebrow_scared" -> if (!shouldMirror) "part_eyebrows_scared_left" else "part_eyebrows_scared_right"
        "nose_basic" -> "part_nose_normal"
        "mouth_smile" -> "part_mouth_happy"
        "mouth_frown" -> "part_mouth_sad"
        "mouth_angry" -> "part_mouth_angry"
        "mouth_surprised" -> "part_mouth_surprised"
        "mouth_worried" -> "part_mouth_worried"
        "mouth_shy" -> "part_mouth_shy"
        "mouth_proud" -> "part_mouth_proud"
        "mouth_scared" -> "part_mouth_scared"
        "blush_shy" -> "part_blush_shy"
        "sweat" -> "sweat"
        else -> "part_eye_happy_left"
    }

    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName).takeIf { it != 0 }
        ?: R.drawable.part_eye_happy_left

    Image(
        painter = painterResource(id = resId),
        contentDescription = part.label,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

