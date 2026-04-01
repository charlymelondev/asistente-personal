package com.carlos.asistente.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AvatarState {
    GREETING,
    LISTENING,
    THINKING,
    CELEBRATING,
    DONE,
    RELAXED
}

@Composable
fun AnimatedAvatar(
    state: AvatarState,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val context = LocalContext.current
    val bitmap = remember {
        context.assets.open("sticker.png").use { stream ->
            BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
    }

    if (bitmap == null) return

    val infiniteTransition = rememberInfiniteTransition(label = "avatar")

    val overlayEmoji = when (state) {
        AvatarState.GREETING -> "\uD83D\uDC4B"
        AvatarState.LISTENING -> "\uD83C\uDFA4"
        AvatarState.THINKING -> "\uD83D\uDCAD"
        AvatarState.CELEBRATING -> "\uD83C\uDF89"
        AvatarState.DONE -> "\u2705"
        AvatarState.RELAXED -> "\uD83D\uDE34"
    }

    when (state) {
        AvatarState.GREETING -> {
            // Wave / bounce side to side
            val bounce by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = -10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "bounce"
            )
            val tilt by infiniteTransition.animateFloat(
                initialValue = -5f, targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "tilt"
            )
            StickerWithEmoji(bitmap, size, overlayEmoji, modifier.graphicsLayer {
                translationY = bounce
                rotationZ = tilt
            })
        }

        AvatarState.LISTENING -> {
            // Pulse like heartbeat + glow
            val pulse by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 1.12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "pulse"
            )
            val sway by infiniteTransition.animateFloat(
                initialValue = -3f, targetValue = 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "sway"
            )
            StickerWithEmoji(bitmap, size, overlayEmoji, modifier.graphicsLayer {
                scaleX = pulse; scaleY = pulse
                rotationZ = sway
            })
        }

        AvatarState.THINKING -> {
            // Slow tilt + float
            val tilt by infiniteTransition.animateFloat(
                initialValue = -4f, targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "tilt"
            )
            val floatY by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = -5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "float"
            )
            StickerWithEmoji(bitmap, size, overlayEmoji, modifier.graphicsLayer {
                rotationZ = tilt
                translationY = floatY
            })
        }

        AvatarState.CELEBRATING -> {
            // Dance! Wiggle + jump + scale
            val wiggle by infiniteTransition.animateFloat(
                initialValue = -8f, targetValue = 8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(250, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "wiggle"
            )
            val jump by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = -15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(350, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "jump"
            )
            val scaleUp by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "scale"
            )
            StickerWithEmoji(bitmap, size, overlayEmoji, modifier.graphicsLayer {
                rotationZ = wiggle
                translationY = jump
                scaleX = scaleUp; scaleY = scaleUp
            })
        }

        AvatarState.DONE -> {
            // Happy pop
            val pop by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "pop"
            )
            val nod by infiniteTransition.animateFloat(
                initialValue = -3f, targetValue = 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "nod"
            )
            StickerWithEmoji(bitmap, size, overlayEmoji, modifier.graphicsLayer {
                scaleX = pop; scaleY = pop
                rotationZ = nod
            })
        }

        AvatarState.RELAXED -> {
            // Gentle float + slow breathing
            val floatY by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = -8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "float"
            )
            val breathe by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 1.03f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ), label = "breathe"
            )
            StickerWithEmoji(bitmap, size, overlayEmoji, modifier.graphicsLayer {
                translationY = floatY
                scaleX = breathe; scaleY = breathe
            })
        }
    }
}

@Composable
private fun StickerWithEmoji(
    bitmap: androidx.compose.ui.graphics.ImageBitmap,
    size: Dp,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size, size * 1.5f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = "Sticker",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // Emoji badge top-right
        Text(
            text = emoji,
            fontSize = (size.value * 0.25f).sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
        )
    }
}
