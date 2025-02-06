package com.example.giftbox.ui.detail

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.giftbox.ui.utils.DateTransformation
import com.example.giftbox.R
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.utils.decimalFormat
import com.example.giftbox.ui.utils.thousandSeparatorTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(id: String, onBack: () -> Unit) {
    val detailViewModel = hiltViewModel<DetailViewModel>()
    detailViewModel.getGift(id)

    // scroll
    val scrollSate = rememberScrollState()

    // input data
    val inputDataList = listOf(
        detailViewModel.name.value,
        detailViewModel.brand.value,
        detailViewModel.cash.value,
        detailViewModel.endDate.value,
        detailViewModel.memo.value
    )

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_detail_gift),
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack
                            , contentDescription = "back button"
                        )
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollSate)
                .padding(25.dp)
        ) {
            // gift image
            GiftImage(detailViewModel.photo.value, detailViewModel.usedDt.value) {
                if (detailViewModel.usedDt.value.isEmpty()) {
                    detailViewModel.setIsShowBottomSheet(true)
                } else {
                    detailViewModel.setIsShowCancelDialog(true)
                }
            }

            // text field
            for (i in inputDataList.indices) {
                if (i == 2 && detailViewModel.cash.value.isEmpty()) continue
                InputDataTextField(
                    value = inputDataList[i],
                    label = detailViewModel.getLabelList(i),
                    index = i
                )
            }

            // use or cancel button
            Button(
                onClick = {
                    if (detailViewModel.usedDt.value.isEmpty()) {
                        detailViewModel.setIsShowBottomSheet(true)
                    } else {
                        detailViewModel.setIsShowCancelDialog(true)
                    }
                },
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                colors = if (detailViewModel.usedDt.value.isNotEmpty()) ButtonDefaults.buttonColors(containerColor = Color.LightGray) else ButtonDefaults.buttonColors()
            ) {
                if (detailViewModel.usedDt.value.isEmpty()) Text(text = stringResource(id = R.string.btn_use))
                else Text(text = stringResource(id = R.string.btn_use_cancel), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        if (detailViewModel.isShowBottomSheet.value) {
            GiftBottomSheet(detailViewModel.photo.value, scope, sheetState) { isUsed ->
                if (isUsed) {
                    if (detailViewModel.cash.value.isNotEmpty()) {
                        detailViewModel.setIsShowUseCashDialog(true)
                        detailViewModel.setIsShowBottomSheet(false)
                    } else {
                        detailViewModel.setIsUsed(true)
                    }
                } else {
                    detailViewModel.setIsShowBottomSheet(false)
                }
            }
        }
        if (detailViewModel.isShowCancelDialog.value) {
            UsedCancelDialog(
                onConfirm = {
                    detailViewModel.setIsUsed(false)
                    detailViewModel.setIsShowCancelDialog(false)
                },
                onDismiss = {
                    detailViewModel.setIsShowCancelDialog(false)
                }
            )
        }

        // 금액권 사용금액 입력 다이얼로그
        if (detailViewModel.isShowUseCashDialog.value) {
            Dialog(onDismissRequest = {}) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    UseCashDialog(
                        remainCash = detailViewModel.cash.value,
                        onCancel = {
                            detailViewModel.setIsShowUseCashDialog(false)
                        },
                        onConfirm = { useCash ->
                            detailViewModel.setIsUsed(true, useCash)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InputDataTextField(value: String, label: Int, index: Int) {
    var modifier = Modifier
        .fillMaxWidth()
        .padding(top = 5.dp)
    if (index == 4) {
        modifier = modifier.height(150.dp)
    }

    OutlinedTextField(
        readOnly = true,
        modifier = modifier,
        value = value,
        onValueChange = {},
        maxLines = if (index == 4) 50 else 1,
        label = { Text(stringResource(id = label)) },
        visualTransformation = when(index) {
            2 -> thousandSeparatorTransformation(true)
            3 -> DateTransformation()
            else -> VisualTransformation.None
        }
    )
}

@Composable
fun GiftImage(selectedImage: Uri?, usedDt: String, onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
        ) {
            if (selectedImage == null) {
                Image(
                    modifier = Modifier
                        .width(80.dp)
                        .height(80.dp),
                    painter = painterResource(id = R.drawable.icon_add_photo),
                    contentDescription = "add photo",
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            onClick()
                        },
                    model = selectedImage,
                    contentDescription = "detail photo",
                    contentScale = ContentScale.Crop
                )
            }

            if (usedDt.isNotEmpty()) {
                UsedStamp(usedDt)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftBottomSheet(image: Uri?, scope: CoroutineScope, sheetState: SheetState, onDismiss: (Boolean) -> Unit) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    ModalBottomSheet(
        modifier = Modifier
            .heightIn(max = screenHeight - 10.dp)
            .fillMaxSize(),
        onDismissRequest = {
            onDismiss(false)
        },
        sheetState = sheetState
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp, start = 25.dp, end = 25.dp)
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .weight(8f)
                    .padding(bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (image == null) {
                    Image(
                        modifier = Modifier.fillMaxWidth(),
                        painter = painterResource(id = R.drawable.icon_add_photo),
                        contentDescription = "use photo",
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = image,
                        contentDescription = "use photo",
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RectangleShape,
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismiss(true)
                        }
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.btn_use_complete))
            }
        }
    }
}


@Composable
fun UsedStamp(usedDate: String) {
    Box(modifier = Modifier
        .size(200.dp)
        .background(Color.Black.copy(0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(80.dp)
                .rotate(-20f)
                .border(
                    width = 3.dp,
                    color = Color.White
                )
        )
        Box(
            modifier = Modifier
                .width(130.dp)
                .height(70.dp)
                .rotate(-20f)
        )
        Box(
            modifier = Modifier
                .width(125.dp)
                .height(65.dp)
                .rotate(-20f)
                .border(
                    width = 7.dp,
                    color = Color.White
                )
        )

        Text(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            text = stringResource(id = R.string.txt_used_stamp, usedDate),
            fontSize = 20.sp,
            modifier = Modifier
                .width(110.dp)
                .height(50.dp)
                .rotate(-20f)
        )
    }
}

@Composable
fun UsedCancelDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        text = {
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.dlg_msg_use_cancel),
                fontSize = 18.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm() }
            ) {
                Text(text = stringResource(id = R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(text = stringResource(id = R.string.btn_cancel))
            }
        },
        shape = RoundedCornerShape(10.dp)
    )
}

// 사용 금액 입력 다이얼로그
@Composable
fun UseCashDialog(remainCash: String, onCancel: () -> Unit, onConfirm: (Int) -> Unit){
    var inputCash by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // title
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            text = stringResource(id = R.string.txt_use_cash_inpur),
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(color = Color.LightGray)
        )

        // cash button
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val useCashList = listOf(1000, 5000, 10000, remainCash.toInt())
            items(useCashList.size) { idx ->
                Spacer(modifier = Modifier.width(4.dp))
                FilterChip(
                    onClick = {
                        val total = ((if (inputCash.isEmpty()) 0 else inputCash.toInt()) + useCashList[idx]).toString()
                        inputCash = if (total.toInt() >= remainCash.toInt()) remainCash else total
                    },
                    label = {
                        Text(
                            text = if (idx == useCashList.lastIndex) {
                                stringResource(id = R.string.btn_all_cash)
                            } else {
                                stringResource(id = R.string.format_cash, decimalFormat(useCashList[idx]))
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        )
                    },
                    selected = false,
                    shape = RoundedCornerShape(50.dp),
                    colors = FilterChipDefaults.filterChipColors().copy(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        selectedContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    border = null
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }

        // input cash
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 20.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                modifier = Modifier
                    .wrapContentWidth()
                    .alignByBaseline(),
                value = inputCash,
                onValueChange = {
                    val input = if (it.isEmpty()) 0 else it.toInt()
                    inputCash = if (input >= remainCash.toInt()) remainCash else it
                },
                visualTransformation = thousandSeparatorTransformation(false),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    textAlign = TextAlign.End
                ),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            Text(
                modifier = Modifier
                    .wrapContentWidth()
                    .alignByBaseline(),
                text = stringResource(id = R.string.format_cash, ""),
                fontSize = 24.sp,
                textAlign = TextAlign.End
            )
        }

        // remain cash
        Text(
            text = stringResource(id = R.string.format_remain_cash, decimalFormat(remainCash.toInt() - (if (inputCash.isEmpty()) 0 else inputCash.toInt()))),
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 20.dp, bottom = 10.dp),
            fontSize = 10.sp
        )

        // bottom button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(color = Color.LightGray)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Button(
                onClick = { onCancel() },
                shape = RectangleShape,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Red,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
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

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(color = Color.LightGray)
            )
            Button(
                onClick = { onConfirm(remainCash.toInt() - (if (inputCash.isEmpty()) 0 else inputCash.toInt())) },
                shape = RectangleShape,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                ),
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
        }
    }
}