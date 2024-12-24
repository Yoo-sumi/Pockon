package com.example.giftbox.ui.detail

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.giftbox.ui.utils.DateTransformation
import com.example.giftbox.R
import com.example.giftbox.model.Gift
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(gift: Gift, onBack: (Gift) -> Unit) {
    val detailViewModel = hiltViewModel<DetailViewModel>()
    detailViewModel.setGift(gift)

    // scroll
    val scrollSate = rememberScrollState()

    // input data
    val inputDataList = listOf(
        detailViewModel.name.value,
        detailViewModel.brand.value,
        detailViewModel.endDate.value,
        detailViewModel.memo.value
    )

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    BackHandler {
        onBack(detailViewModel.gift.value)
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
                        onBack(detailViewModel.gift.value)
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
            GiftImage(detailViewModel.photo.value, detailViewModel.usedDt.value)
//            detailViewModel.setIsShowBottomSheet(!detailViewModel.isShowBottomSheet.value)

            // text field
            for (i in inputDataList.indices) {
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
                if (isUsed) detailViewModel.setIsUsed(true)
                else detailViewModel.setIsShowBottomSheet(false)
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
    }
}

@Composable
fun InputDataTextField(value: String, label: Int, index: Int) {
    var modifier = Modifier
        .fillMaxWidth()
        .padding(top = 5.dp)
    if (index == 3) {
        modifier = modifier.height(200.dp)
    }

    OutlinedTextField(
        readOnly = true,
        modifier = modifier,
        value = value,
        onValueChange = {},
        maxLines = if (index == 3) 50 else 1,
        label = { Text(stringResource(id = label)) },
        visualTransformation = if (index == 2) DateTransformation() else VisualTransformation.None
    )
}

@Composable
fun GiftImage(selectedImage: Uri?, usedDt: String) {
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
                    modifier = Modifier.fillMaxSize(),
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
                        contentScale = ContentScale.Crop
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
//        shape = RectangleShape
        shape = RoundedCornerShape(10.dp)
    )
}
