package com.example.giftbox.ui.pin

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.sharp.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.giftbox.R
import kotlinx.coroutines.delay

const val pinSize = 6
const val password = "000000"

@Composable
fun PinScreen() {
    val inputPin = remember { mutableStateListOf<Int>() }
    val error = remember { mutableStateOf<String>("") }
    val showSuccess = remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (inputPin.size == pinSize) {
        LaunchedEffect(true) {
            delay(300)

            if (inputPin.joinToString("") == password) {
                showSuccess.value = true
                error.value = ""
            } else {
                inputPin.clear()
                error.value = "PIN 번호가 일치하지 않습니다."
            }
        }
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
                Spacer(modifier = Modifier.height(100.dp))

                Text(
                    text = stringResource(id = R.string.txt_input_pin),
                    style = typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(30.dp))

                if (showSuccess.value) {
                    Text(text = "dfdf")
                } else {
                    Row {
                        (0 until pinSize).forEach {
                            Icon(
                                imageVector = if (inputPin.size > it) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = it.toString(),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(30.dp),
                                tint = Color.Black
                            )
                        }
                    }
                }
                Text(
                    text = error.value,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(50.dp))
            }

            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..3).forEach {
                        PinKeyItem(
                            onClick = { inputPin.add(it) }
                        ) {
                            Text(
                                text = it.toString(),
                                style = typography.bodyLarge
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (4..6).forEach {
                        PinKeyItem(
                            onClick = { inputPin.add(it) }
                        ) {
                            Text(
                                text = it.toString(),
                                style = typography.bodyLarge
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (7..9).forEach {
                        PinKeyItem(
                            onClick = { inputPin.add(it) }
                        ) {
                            Text(
                                text = it.toString(),
                                style = typography.bodyLarge,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(
//                        imageVector = Icons.Default.Check,
//                        contentDescription = "Success",
                        modifier = Modifier
                            .size(25.dp)
                            .clickable { }
                    )
                    PinKeyItem(
                        onClick = { inputPin.add(0) },
                        modifier = Modifier.padding(horizontal = 16.dp,
                            vertical = 8.dp)
                    ) {
                        Text(
                            text = "0",
                            style = typography.bodyLarge,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Clear",
                        modifier = Modifier
                            .size(25.dp)
                            .clickable {
                                if (inputPin.isNotEmpty()) {
                                    inputPin.removeLast()
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun PinKeyItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.padding(8.dp),
    shape: CornerBasedShape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    backgroundColor: Color = MaterialTheme.colorScheme.onPrimary,
    contentColor: Color = contentColorFor(backgroundColor = backgroundColor),
    elevation: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
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

@Preview
@Composable
fun PinScreenPreview() {
    PinScreen()
}