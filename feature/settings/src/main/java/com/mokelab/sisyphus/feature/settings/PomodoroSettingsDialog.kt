package com.mokelab.sisyphus.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PomodoroSettingsDialog(
    workDuration: Int,
    breakDuration: Int,
    onWorkDurationChange: (Int) -> Unit,
    onBreakDurationChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var workText by remember { mutableStateOf(workDuration.toString()) }
    var breakText by remember { mutableStateOf(breakDuration.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("番茄钟设置") },
        text = {
            Column {
                OutlinedTextField(
                    value = workText,
                    onValueChange = { value ->
                        workText = value
                        value.toIntOrNull()?.let { onWorkDurationChange(it) }
                    },
                    label = { Text("工作时长（分钟）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = breakText,
                    onValueChange = { value ->
                        breakText = value
                        value.toIntOrNull()?.let { onBreakDurationChange(it) }
                    },
                    label = { Text("休息时长（分钟）") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}
