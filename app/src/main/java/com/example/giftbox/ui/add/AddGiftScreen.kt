package com.example.giftbox.ui.add

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.giftbox.ui.utils.DateTransformation
import com.example.giftbox.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGifticon(onBack: (Boolean) -> Unit) {
    val addViewModel = hiltViewModel<AddViewModel>()
    val context = LocalContext.current

    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // select photo
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            addViewModel.setPhoto(it)
        }
    }
    // scroll
    val scrollSate = rememberScrollState()

    // input data
    val inputDataList = listOf(
        addViewModel.name.value,
        addViewModel.brand.value,
        addViewModel.endDate.value,
        addViewModel.memo.value
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_add_gift),
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onBack(false)
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
            GiftImage(addViewModel.photo.value, galleryLauncher)

            // text field
            for (i in inputDataList.indices) {
                InputDataTextField(
                    value = inputDataList[i],
                    label = addViewModel.getLabelList(i),
                    index = i,
                    onValueChange = { index, value ->
                        addViewModel.setGift(index, value)
                    },
                    onDatePicker = {
                        if (i == 2) {
                            addViewModel.changeDatePickerState()
                        }
                    }
                )
            }

            // add button
            Button(
                onClick = {
                    val msg = addViewModel.isValid()
                    if (msg != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar(message = context.getString(msg))
                        }
                    } else {
                        addViewModel.addGift { result ->
                            if (result) onBack(true)
                            else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(message = context.getString(R.string.msg_no_register))
                                }
                            }
                        }
                    }
                },
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Text(text = stringResource(id = R.string.btn_add))
            }
        }

        // DatePicker
        if (addViewModel.isShowDatePicker.value) {
            CustomDatePickerDialog(
                selectedDate = addViewModel.endDate.value,
                onCancel = { addViewModel.changeDatePickerState() },
                onConfirm = {
                    addViewModel.changeDatePickerState()
                    addViewModel.setGift(2, value = it)
                }
            )
        }
    }
}

@Composable
fun InputDataTextField(value: String, label: Int, index: Int, onValueChange: (Int, String) -> Unit, onDatePicker: () -> Unit) {
    var modifier = Modifier
        .fillMaxWidth()
        .padding(top = 5.dp)
    if (index == 3) {
        modifier = modifier.height(200.dp)
    }

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = {
            if (it.length > 8 && index == 2) return@OutlinedTextField
            onValueChange(index, it)
        },
        maxLines = if (index == 3) 50 else 1,
        label = { Text(stringResource(id = label)) },
        visualTransformation = if (index == 2) DateTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (index == 2) {
                IconButton(onClick = { onDatePicker() }) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "DateRange"
                    )
                }
            }
        },
        keyboardOptions = if (index == 2) KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number) else KeyboardOptions.Default
    )
}

@Composable
fun GiftImage(selectedImage: Uri?, galleryLauncher:  ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>) {
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
                .background(Color.LightGray)
                .clickable {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
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
                    contentDescription = "add photo",
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    selectedDate: String,
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    DatePickerDialog(
        onDismissRequest = { },
        confirmButton = { },
        colors = DatePickerDefaults.colors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(5.dp)
    ) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = if (selectedDate.isNotEmpty()) {
                val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                formatter.parse(selectedDate)?.time ?: System.currentTimeMillis()
            } else System.currentTimeMillis(),
        )

        DatePicker(state = datePickerState)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(onClick = {
                onCancel()
            }) {
                Text(text = stringResource(id = R.string.btn_cancel))
            }

            Spacer(modifier = Modifier.width(5.dp))

            Button(onClick = {
                datePickerState.selectedDateMillis?.let { selectedDateMillis ->
                    val yyyyMMdd = SimpleDateFormat(
                        "yyyyMMdd",
                        Locale.getDefault()
                    ).format(Date(selectedDateMillis))
                    onConfirm(yyyyMMdd)
                }
            }) {
                Text(text = stringResource(id = R.string.btn_confirm))
            }
        }
    }
}