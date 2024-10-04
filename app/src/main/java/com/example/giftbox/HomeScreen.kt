package com.example.giftbox

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableList
import org.checkerframework.checker.guieffect.qual.UI

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel(), onAdd: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = stringResource(id = R.string.home),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Black,
            modifier = Modifier.align(Alignment.Center)
        )
        SmallFloatingActionButton(
            onClick = {
                onAdd()
            },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.secondary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(15.dp)
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "")
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGifticon(viewModel: HomeViewModel = viewModel(), onBack: () -> Unit, onAddPhoto: (String) -> Unit) {
    val isLoad by viewModel.isLoad.collectAsStateWithLifecycle()

    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // select photo
    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    // check permission
    val context = LocalContext.current
    val launcherMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
        if (areGranted) {
            galleryLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(message = context.getString(R.string.msg_permission_photo))
            }
        }
    }

    // scroll
    val scrollSate = rememberScrollState()

    // input data
    var inputDataList by remember { mutableStateOf(List(4) { "" }) }
    val labelList = listOf(
        stringResource(id = R.string.txt_name),
        stringResource(id = R.string.txt_brand),
        stringResource(id = R.string.txt_end_date),
        stringResource(id = R.string.txt_memo)
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
            .padding(25.dp)
            .fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollSate)
        ) {
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
                            checkPermission(context, launcherMultiplePermissions) {
                                if (it) {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                            }
                        }
                ) {
                    if (selectedImageUri == null) {
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
                            model = selectedImageUri,
                            contentDescription = "selected photo",
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // text field
            for (i in inputDataList.indices) {
                val lines = if (i == inputDataList.lastIndex) 300 else 50
                val height = if (i == inputDataList.lastIndex) 200 else null
                InputDataTextField(
                    value = inputDataList[i],
                    label = labelList[i],
                    height = height,
                    index = i,
                    maxLines = lines
                ) { index, value ->
                    val dataList = mutableListOf<String>()
                    inputDataList.forEachIndexed { inx, v ->
                        if (inx == index) dataList.add(value)
                        else dataList.add(v)
                    }
                    inputDataList = dataList
                }
            }
            // add button
            Button(
                onClick = {
                    viewModel.addGift(inputDataList)
                },
                shape = RectangleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Text(text = "등록하기")
            }
        }
    }
}

@Composable
fun InputDataTextField(value: String, label: String, index: Int, height: Int?, maxLines: Int, onValueChange: (Int, String) -> Unit) {
    val modifier = if (height != null) {
        Modifier
            .fillMaxWidth()
            .height(height.dp)
            .padding(top = 5.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
    }

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = {
            onValueChange(index, it)
        },
        label = { Text(label) },
        maxLines = maxLines
    )
}

fun checkPermission(
    context: Context,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    result: (Boolean) -> Unit
) {
    val permissions = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf( Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf( Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    if (permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
    }) {
        result(true)
    }

    else {
        result(false)
        launcher.launch(permissions)
    }
}