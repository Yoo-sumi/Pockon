package com.example.giftbox.ui.detail

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.giftbox.ui.utils.DateTransformation
import com.example.giftbox.R
import com.example.giftbox.model.Gift

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(gift: Gift, onBack: () -> Unit) {
    val addViewModel = hiltViewModel<DetailViewModel>()
    addViewModel.setGift(gift)

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
            GiftImage(addViewModel.photo.value)

            // text field
            for (i in inputDataList.indices) {
                InputDataTextField(
                    value = inputDataList[i],
                    label = addViewModel.getLabelList(i),
                    index = i
                )
            }

            // use button
            Button(
                onClick = {

                },
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Text(text = stringResource(id = R.string.btn_use))
            }
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
fun GiftImage(selectedImage: Bitmap?) {
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
                Image(
                    modifier = Modifier.fillMaxSize(),
                    bitmap = selectedImage.asImageBitmap(),
                    contentDescription = "add photo",
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}