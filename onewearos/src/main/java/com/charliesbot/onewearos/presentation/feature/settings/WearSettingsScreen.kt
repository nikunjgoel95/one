package com.charliesbot.onewearos.presentation.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextButton
import androidx.wear.compose.material3.TextToggleButton
import androidx.wear.compose.material3.TimePicker
import androidx.wear.tooling.preview.devices.WearDevices
import org.koin.androidx.compose.koinViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun WearSettingsScreen(
    viewModel: WearSettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        TimePicker(
            initialTime = uiState.bedtime ?: LocalTime.of(22, 0), // Default 10 PM
            onTimePicked = { newTime ->
                viewModel.setBedtime(newTime)
                showTimePicker = false
            }
        )
    } else {
        WearSettingsContent(
            smartNotificationsEnabled = uiState.smartNotificationsEnabled,
            vibrationEnabled = uiState.vibrationEnabled,
            bedtime = uiState.bedtime,
            notificationStrategy = uiState.notificationStrategy,
            smartReminderTime = uiState.smartReminderTime,
            eatingWindowTime = uiState.eatingWindowTime,
            is36HourFast = uiState.is36HourFast,
            onToggleSmartNotifications = viewModel::toggleSmartNotifications,
            onToggleVibration = viewModel::toggleVibration,
            onSetBedtime = { showTimePicker = true },
            onClearBedtime = { viewModel.setBedtime(null) }
        )
    }
}

@Composable
private fun WearSettingsContent(
    smartNotificationsEnabled: Boolean,
    vibrationEnabled: Boolean,
    bedtime: LocalTime?,
    notificationStrategy: String,
    smartReminderTime: String,
    eatingWindowTime: String?,
    is36HourFast: Boolean,
    onToggleSmartNotifications: () -> Unit,
    onToggleVibration: () -> Unit,
    onSetBedtime: () -> Unit,
    onClearBedtime: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenScaffold {
        ScalingLazyColumn(
            modifier = modifier.fillMaxSize(),
            state = rememberScalingLazyListState(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Smart Notifications Toggle
            item {
                TextToggleButton(
                    checked = smartNotificationsEnabled,
                    onCheckedChange = { onToggleSmartNotifications() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Smart Notifications",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (smartNotificationsEnabled) "✓" else "○",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Vibration Toggle
            item {
                TextToggleButton(
                    checked = vibrationEnabled,
                    onCheckedChange = { onToggleVibration() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vibration",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (vibrationEnabled) "✓" else "○",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bedtime Button
            item {
                TextButton(
                    onClick = onSetBedtime,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Bedtime",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = bedtime?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                                ?: "Not set",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Clear bedtime if set
            if (bedtime != null) {
                item {
                    TextButton(
                        onClick = onClearBedtime,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Clear Bedtime",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Notification schedule display
            if (smartNotificationsEnabled) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Strategy label
                item {
                    Text(
                        text = "Notifications ($notificationStrategy)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Eating window notification (if not 36h fast)
                if (!is36HourFast && eatingWindowTime != null) {
                    item {
                        Text(
                            text = "• $eatingWindowTime",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    item {
                        Text(
                            text = "Eating window (1h before)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
                
                // Smart reminder notification
                item {
                    Text(
                        text = "• $smartReminderTime",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
                item {
                    Text(
                        text = "Start fasting",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
private fun WearSettingsScreenPreview() {
    MaterialTheme {
        WearSettingsContent(
            smartNotificationsEnabled = true,
            vibrationEnabled = true,
            bedtime = LocalTime.of(22, 30),
            notificationStrategy = "based on bedtime",
            smartReminderTime = "4:00 PM",
            eatingWindowTime = "3:00 PM",
            is36HourFast = false,
            onToggleSmartNotifications = {},
            onToggleVibration = {},
            onSetBedtime = {},
            onClearBedtime = {}
        )
    }
}

