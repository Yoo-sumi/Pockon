package com.example.giftbox.ui.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.giftbox.R

@Composable
fun PinScreen(onSuccess: () -> Unit) {
    val pinViewModel = hiltViewModel<PinViewModel>()

    if (pinViewModel.mode.value == 4) {
        onSuccess()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (pinViewModel.mode.value == 1) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier
                                .padding(15.dp)
                                .clickable {
                                    pinViewModel.setMode(0)
                                }
                        )
                    }
                }

                Text(
                    text = stringResource(id = pinViewModel.getTitle()),
                    style = typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(30.dp))

                if (pinViewModel.showSuccess.value && pinViewModel.mode.value != 0) { // pin auth success
                    if (pinViewModel.mode.value != 4) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            modifier = Modifier
                                .size(30.dp)
                        )
                    }
                    onSuccess()
                } else { // pin circle shape
                    Row {
                        (0 until pinViewModel.getPinSize()).forEach {
                            val bgColor = if (pinViewModel.inputPin.size > it) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceContainerLowest
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(15.dp)
                                    .clip(shape = CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                    .background(bgColor)
                            )
                        }
                    }
                }

                // error msg
                Text(
                    text = pinViewModel.error.value?.let { stringResource(id = it) } ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(50.dp))
            }

            // pin keypad
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                if (pinViewModel.mode.value != 2) {
                    Text(
                        text = stringResource(id = R.string.txt_no_pin_auth),
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .clickable {
                                pinViewModel.setMode(4)
                            },
                        textAlign = TextAlign.Center
                    )
                }
                // 1~3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..3).forEach {
                        PinKeyItem(
                            onClick = { pinViewModel.addPinNum(it) }
                        ) {
                            Text(
                                text = it.toString(),
                                style = typography.bodyLarge
                            )
                        }
                    }
                }
                // 4~6
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (4..6).forEach {
                        PinKeyItem(
                            onClick = { pinViewModel.addPinNum(it) }
                        ) {
                            Text(
                                text = it.toString(),
                                style = typography.bodyLarge
                            )
                        }
                    }
                }
                // 7~9
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (7..9).forEach {
                        PinKeyItem(
                            onClick = { pinViewModel.addPinNum(it) }
                        ) {
                            Text(
                                text = it.toString(),
                                style = typography.bodyLarge,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
                // 0~<-
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PinKeyItem(
                        onClick = {  }
                    ) {
                        Spacer(modifier = Modifier.padding(4.dp))
                    }

                    PinKeyItem(
                        onClick = { pinViewModel.addPinNum(0) }
                    ) {
                        Text(
                            text = "0",
                            style = typography.bodyLarge,
                            modifier = Modifier.padding(4.dp)
                        )
                    }

                    PinKeyItem(
                        onClick = {
                            if (pinViewModel.inputPin.isNotEmpty()) {
                                pinViewModel.removeLastPin()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Clear",
                            modifier = Modifier
                                .size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PinKeyItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    backgroundColor: Color = MaterialTheme.colorScheme.onPrimary,
    contentColor: Color = contentColorFor(backgroundColor = backgroundColor),
    elevation: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.padding(8.dp),
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor,
        tonalElevation = elevation,
        onClick = {
            onClick()
        }
    ) {
        Box(
            modifier = Modifier.defaultMinSize(minWidth = 64.dp, minHeight = 64.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun PinSettingDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                textAlign = TextAlign.Start,
                text = stringResource(id = R.string.title_msg_use_pin),
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                textAlign = TextAlign.Start,
                text = stringResource(id = R.string.dlg_msg_use_pin),
                fontSize = 18.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm() }
            ) {
                Text(text = stringResource(id = R.string.btn_pin_setting))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(text = stringResource(id = R.string.btn_pin_not_use))
            }
        },
//        shape = RectangleShape
        shape = RoundedCornerShape(10.dp)
    )
}