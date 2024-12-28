package com.example.giftbox.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.LocalMinimumTouchTargetEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.giftbox.R
import com.example.giftbox.data.BrandSearchDataSource
import com.example.giftbox.model.Document
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.list.GiftItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onAdd: () -> Unit, showMap: () -> Unit, onDetail: (Gift) -> Unit) {
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    getLocation(fusedLocationClient) {
        homeViewModel.getGiftList(it)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                title = {
                    Text(text = stringResource(id = R.string.home))
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = stringResource(id = R.string.txt_use_near),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Start
                    )
                    // 기본 padding 없애기
                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentEnforcement provides false,
                    ) {
                        IconButton(onClick = {
                            getLocation(fusedLocationClient) {
                                homeViewModel.getGiftList(it)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "refresh button",
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_open_map),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.clickable {
                            showMap()
                        }
                    )
                }

                // gift item
                LazyRow(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                    itemsIndexed(items = homeViewModel.displayGiftList.value) { index, gift ->
                        HomeGiftItem(gift.first, homeViewModel.formatString(gift.first.endDt), gift.second) {
                            onDetail(gift.first)
                        }
                    }
                }

            }

            // + 추가 버튼
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
}

/** 기프티콘 각각의 카드*/
@Composable
fun HomeGiftItem(gift: Gift, formattedEndDate: String, document: Document, onClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(end = 5.dp)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.width(150.dp)
        ) {
            AsyncImage(
                modifier = Modifier.size(150.dp),
                model = Uri.parse(gift.photo),
                contentDescription = "add photo",
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(5.dp)
            ) {

                Text(
                    text = gift.brand,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = gift.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Box {
                    Text(
                        text = "${document.distance}m",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Text(
                        text = "~ ${formattedEndDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun getLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onComplete: (Location?) -> Unit
) {
    fusedLocationClient.lastLocation
        .addOnSuccessListener {
            onComplete(it)
        }
        .addOnFailureListener {
            onComplete(null)
        }
}