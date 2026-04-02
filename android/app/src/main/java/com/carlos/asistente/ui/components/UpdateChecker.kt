package com.carlos.asistente.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.carlos.asistente.BuildConfig
import com.carlos.asistente.data.remote.ApiClient
import com.carlos.asistente.ui.theme.CoralAccent
import kotlinx.coroutines.launch

@Composable
fun UpdateChecker() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var downloadUrl by remember { mutableStateOf("") }
    var newVersion by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = ApiClient.api.checkVersion()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.versionCode > BuildConfig.VERSION_CODE) {
                        newVersion = body.version
                        downloadUrl = body.downloadUrl
                        showDialog = true
                    }
                }
            } catch (_: Exception) {
                // Silently fail — don't bother user if check fails
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nueva versión disponible") },
            text = { Text("Hay una actualización v$newVersion de Pollito al Rescate. ¿Quieres descargarla?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralAccent)
                ) {
                    Text("Actualizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Más tarde")
                }
            }
        )
    }
}
