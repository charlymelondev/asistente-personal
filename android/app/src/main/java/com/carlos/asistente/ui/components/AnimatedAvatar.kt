package com.carlos.asistente.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    size: Dp = 120.dp,
    stickerAsset: String = "sticker.png"
) {
    val context = LocalContext.current
    val bitmap = remember(stickerAsset) {
        context.assets.open(stickerAsset).use { stream ->
            BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
    }

    if (bitmap == null) return

    val infiniteTransition = rememberInfiniteTransition(label = "avatar")

    when (state) {
        AvatarState.GREETING -> {
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
            StickerImage(bitmap, size, modifier.graphicsLayer {
                translationY = bounce
                rotationZ = tilt
            })
        }

        AvatarState.LISTENING -> {
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
            StickerImage(bitmap, size, modifier.graphicsLayer {
                scaleX = pulse; scaleY = pulse
                rotationZ = sway
            })
        }

        AvatarState.THINKING -> {
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
            StickerImage(bitmap, size, modifier.graphicsLayer {
                rotationZ = tilt
                translationY = floatY
            })
        }

        AvatarState.CELEBRATING -> {
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
            StickerImage(bitmap, size, modifier.graphicsLayer {
                rotationZ = wiggle
                translationY = jump
                scaleX = scaleUp; scaleY = scaleUp
            })
        }

        AvatarState.DONE -> {
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
            StickerImage(bitmap, size, modifier.graphicsLayer {
                scaleX = pop; scaleY = pop
                rotationZ = nod
            })
        }

        AvatarState.RELAXED -> {
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
            StickerImage(bitmap, size, modifier.graphicsLayer {
                translationY = floatY
                scaleX = breathe; scaleY = breathe
            })
        }
    }
}

@Composable
private fun StickerImage(
    bitmap: androidx.compose.ui.graphics.ImageBitmap,
    size: Dp,
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
    }
}
