package com.sumi.pockon.ui.notification

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumi.pockon.R

@Composable
fun NotificationSettingScreen(onBack: () -> Unit) {
    val notificationSettingViewModel = hiltViewModel<NotificationSettingViewModel>()
    val day = notificationSettingViewModel.getDayList()

    BackHandler {
        notificationSettingViewModel.changeNotiEndDt()
        onBack()
    }

    Column {
        NotificationSettingScreenTopBar {
            notificationSettingViewModel.changeNotiEndDt()
            onBack()
        }
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
            text = stringResource(id = R.string.txt_noti_of_imminent_use_select),
            fontSize = 10.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
        LazyColumn {
            items(day.size) { idx ->
                Column(
                    modifier = Modifier
                        .clickable {
                            notificationSettingViewModel.setSeletedDay(day[idx])
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterStart),
                            text = if (idx == 0) stringResource(id = R.string.txt_today) else stringResource(id = R.string.txt_day_before, day[idx])
                        )

                        if (notificationSettingViewModel.seletedDay.value == day[idx]) {
                            Icon(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Check",
                                tint = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationSettingScreenTopBar(onBack: () -> Unit) {
    // topbar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
            }
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = {
                onBack()
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back button"
            )
        }
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = R.string.txt_noti_of_imminent_use),
            fontSize = 16.sp,
        )
    }
}