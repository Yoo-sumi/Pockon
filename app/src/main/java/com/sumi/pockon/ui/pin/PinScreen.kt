package com.sumi.pockon.ui.pin

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumi.pockon.R

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
            .background(colorResource(id = R.color.background)),
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
                                },
                            tint = colorResource(id = R.color.onPrimary)
                        )
                    }
                }

                Text(
                    text = stringResource(id = pinViewModel.getTitle()),
                    style = typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                    color = colorResource(id = R.color.onPrimary)
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
                            val bgColor =
                                if (pinViewModel.inputPin.size > it) R.color.onPrimary else R.color.background
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(15.dp)
                                    .clip(shape = CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = colorResource(id = R.color.onPrimary),
                                        shape = CircleShape
                                    )
                                    .background(colorResource(bgColor))
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
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (pinViewModel.mode.value != 2) {
                    Text(
                        text = stringResource(id = R.string.txt_no_pin_auth),
                        modifier = Modifier
                            .padding(bottom = 5.dp)
                            .clickable {
                                pinViewModel.setMode(4)
                            },
                        textAlign = TextAlign.Center,
                        color = colorResource(id = R.color.onPrimary)
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
                                style = typography.bodyLarge,
                                color = colorResource(id = R.color.onPrimary)
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
                                style = typography.bodyLarge,
                                color = colorResource(id = R.color.onPrimary)
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
                                modifier = Modifier.padding(4.dp),
                                color = colorResource(id = R.color.onPrimary)
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
                        onClick = { }
                    ) {
                        Spacer(modifier = Modifier.padding(4.dp))
                    }

                    PinKeyItem(
                        onClick = { pinViewModel.addPinNum(0) }
                    ) {
                        Text(
                            text = "0",
                            style = typography.bodyLarge,
                            modifier = Modifier.padding(4.dp),
                            color = colorResource(id = R.color.onPrimary)
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
                                .size(20.dp),
                            tint = colorResource(id = R.color.onPrimary)
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
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 80.dp, minHeight = 80.dp)
            .padding(10.dp)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}