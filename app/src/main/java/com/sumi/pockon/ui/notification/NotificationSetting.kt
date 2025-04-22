package com.sumi.pockon.ui.notification

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
        Box(
            modifier = Modifier
                .clickable {
                    notificationSettingViewModel.toggleIsShowTimePickerWheelDialog()
                }
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp),
                text = "\uD83D\uDD52 알림 시간: 오전 9시",
            )
        }
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

    // 시간 입력 다이얼로그
    if (notificationSettingViewModel.isShowTimePickerWheelDialog.value) {
        Dialog(onDismissRequest = {}) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                TimePickerWheelDialog(onCancel = { /*TODO*/ }) {

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

@Composable
fun TimePickerWithAmPm(
    onConfirm: (String, Int, Int) -> Unit // AM/PM, hour, minute 전달
) {
    var selectedAmPmIndex by rememberSaveable { mutableIntStateOf(0) } // 0 = 오전, 1 = 오후
    var selectedHourIndex by rememberSaveable { mutableIntStateOf(8) } // 기본값 8시
    var selectedMinuteIndex by rememberSaveable { mutableIntStateOf(0) } // 기본값 0분

    val amPmList = listOf("오전", "오후")
    val hourList = (1..12).map { it.toString().padStart(2, '0') }
    val minuteList = (0..59).map { it.toString().padStart(2, '0') }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            WheelPickerWithHighlight(
                items = amPmList,
                selectedIndex = selectedAmPmIndex,
                onItemSelected = { selectedAmPmIndex = it },
                modifier = Modifier.weight(1f)
            )
            WheelPickerWithHighlight(
                items = hourList,
                selectedIndex = selectedHourIndex,
                onItemSelected = { selectedHourIndex = it },
                modifier = Modifier.weight(1f)
            )
            WheelPickerWithHighlight(
                items = minuteList,
                selectedIndex = selectedMinuteIndex,
                onItemSelected = { selectedMinuteIndex = it },
                modifier = Modifier.weight(1f)
            )
        }

//        Spacer(modifier = Modifier.height(24.dp))
//
//        Button(onClick = {
//            val amPm = amPmList[selectedAmPmIndex]
//            val hour = hourList[selectedHourIndex].toInt()
//            val minute = minuteList[selectedMinuteIndex].toInt()
//            onConfirm(amPm, hour, minute)
//        }) {
//            Text("확인")
//        }
    }
}

@Composable
fun WheelPickerWithHighlight(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleItemsCount = 3
    val itemHeight = 48.dp

    Box(modifier = modifier.height(itemHeight * visibleItemsCount)) {
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) // 하이라이트 배경
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            itemsIndexed(items) { index, item ->
                Box(modifier = Modifier.height(itemHeight)) {
                    Text(
                        text = item,
                        fontSize = 20.sp,
                        fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                        color = if (index == selectedIndex)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(index) }
                            .align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// 사용 금액 입력 다이얼로그
@Composable
fun TimePickerWheelDialog(onCancel: () -> Unit, onConfirm: (Int) -> Unit) {
    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // title
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                text = "\uD83D\uDD52",
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = MaterialTheme.colorScheme.outline)
            )

            // timepicker
            TimePickerWithAmPm { isAm, hour, minute ->
//                Log.d("TimePicker", "Selected: ${if (isAm) "AM" else "PM"} $hour:$minute")
            }

            // bottom button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = MaterialTheme.colorScheme.outline)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Button(
                    onClick = {  },
                    shape = RectangleShape,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.outline,
                        disabledContentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_confirm),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(color = MaterialTheme.colorScheme.outline)
                )

                Button(
                    onClick = { onCancel() },
                    shape = RectangleShape,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.error,
                        disabledContainerColor = MaterialTheme.colorScheme.outline,
                        disabledContentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_cancel),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PPPP() {
    TimePickerWheelDialog(
        onCancel = {},
        onConfirm = {}
    )
}