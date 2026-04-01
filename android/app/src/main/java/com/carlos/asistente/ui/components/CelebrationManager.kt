package com.carlos.asistente.ui.components

import androidx.compose.runtime.Composable

@Composable
fun CelebrationManager(
    showCreateCelebration: Boolean,
    showDoneCelebration: Boolean,
    onDismissCreate: () -> Unit,
    onDismissDone: () -> Unit
) {
    if (showCreateCelebration) {
        CelebrationOverlay(
            stickerAsset = "sticker_create.png",
            message = "¡A por ello Papí!",
            onDismiss = onDismissCreate
        )
    }
    if (showDoneCelebration) {
        CelebrationOverlay(
            stickerAsset = "sticker_done.png",
            message = "¡Bien hecho Papí!",
            onDismiss = onDismissDone
        )
    }
}
