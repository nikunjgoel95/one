package com.charliesbot.onewearos.presentation.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextToggleButton
import androidx.wear.tooling.preview.devices.WearDevices
import com.charliesbot.onewearos.R
import com.charliesbot.shared.core.components.FastingProgressBar
import com.charliesbot.shared.core.components.TimeInfoDisplay
import com.charliesbot.shared.core.utils.calculateProgressFraction
import com.charliesbot.shared.core.utils.convertMillisToLocalDateTime
import com.charliesbot.shared.core.utils.formatTimestamp
import org.koin.androidx.compose.koinViewModel

@Composable
fun rememberIsLargeScreen(): Boolean {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    return remember(screenWidthDp) {
        screenWidthDp >= 225
    }
}


@Composable
fun WearTodayScreen(viewModel: WearTodayViewModel = koinViewModel()) {
    val isLargeScreen = rememberIsLargeScreen()
    val startTimeInMillis by viewModel.startTimeInMillis.collectAsStateWithLifecycle()
    val elapsedTime by viewModel.elapsedTime.collectAsStateWithLifecycle()
    val startTimeInLocalDateTime =
        convertMillisToLocalDateTime(startTimeInMillis)
    val isFasting by viewModel.isFasting.collectAsStateWithLifecycle()
    val fastingGoalId by viewModel.fastingGoalId.collectAsStateWithLifecycle()
    val fastButtonLabel =
        if (isFasting) stringResource(R.string.end_fast) else stringResource(R.string.start_fasting)

    val timeLabel = if (isFasting) {
        formatTimestamp(elapsedTime)
    } else {
        stringResource(R.string.target_duration_hours)
    }
    val currentGoal = com.charliesbot.shared.core.constants.PredefinedFastingGoals.getGoalById(fastingGoalId)
    val totalGoalMillis = currentGoal.durationMillis

    ScreenScaffold {
        FastingProgressBar(
            progress = calculateProgressFraction(elapsedTime, totalGoalMillis),
            strokeWidth = 8.dp,
            indicatorColor = MaterialTheme.colorScheme.primaryDim,
            trackColor = MaterialTheme.colorScheme.onBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = timeLabel,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = if (isLargeScreen) 30.sp else 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(15.dp))
                AnimatedVisibility(
                    visible = isFasting
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TimeInfoDisplay(
                            title = stringResource(R.string.label_started),
                            date = startTimeInLocalDateTime,
                            isForWear = true
                        )
                        TimeInfoDisplay(
                            title = stringResource(R.string.label_goal),
                            date = startTimeInLocalDateTime.plusHours(currentGoal.durationDisplay.toLongOrNull() ?: 16L),
                            isForWear = true
                        )
                    }
                }
                Spacer(Modifier.height(15.dp))
                TextToggleButton(
                    checked = !isFasting,
                    onCheckedChange = {
                        if (isFasting) viewModel.onStopFasting() else viewModel.onStartFasting()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(fastButtonLabel, fontSize = if (isLargeScreen) 16.sp else 12.sp)
                }
            }
        }
    }
}

@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearTodayScreen()
}
