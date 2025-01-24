package com.example.giftbox.ui.home

import android.annotation.SuppressLint
import android.location.Location
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.giftbox.R
import com.example.giftbox.model.Document
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.utils.formatString
import com.example.giftbox.ui.utils.getDday
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onAdd: () -> Unit, showMap: () -> Unit, onDetail: (Gift) -> Unit) {
    val homeViewModel = hiltViewModel<HomeViewModel>()
    val context = LocalContext.current
    val fusedLocationClient = rememberSaveable {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val scrollState = rememberScrollState()

    getLocation(fusedLocationClient) {
        homeViewModel.setLocation(it) // 위치 가져온 다음
        homeViewModel.observeGiftList() // 기프티콘 불러오기
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
                    .verticalScroll(scrollState)
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
                                homeViewModel.setLocation(it)
                                homeViewModel.getBrandInfoList() // 위치 동기화 후 브랜드만 재검색
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "refresh button",
                            )
                        }
                    }

                    Text(
                        text = stringResource(id = R.string.btn_open_map),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .clickable {
                                showMap()
                            }
                            .fillMaxWidth()
                    )
                }

                // gift item
                LazyRow(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                ) {

                    itemsIndexed(items = homeViewModel.displayGiftList.value) { index, gift ->
                        HomeGiftItem(gift.first, formatString(gift.first.endDt), getDday(gift.first.endDt), gift.second) {
                            onDetail(gift.first)
                        }
                    }
                }


                // 기한 만료
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = stringResource(id = R.string.txt_use_end),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Start
                    )
                }

                // gift item
                LazyRow(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                ) {

                    items(items = homeViewModel.closeToGiftList.value) { gift ->
                        HomeGiftItem(gift, formatString(gift.endDt), getDday(gift.endDt)) {
                            onDetail(gift)
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
fun HomeGiftItem(gift: Gift, formattedEndDate: String, dDay: Pair<String, Boolean>, document: Document? = null, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(3.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .clickable {
                onClick()
            }
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .width(150.dp)
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
                    document?.let {
                        Text(
                            text = "${document.distance}m",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                    Text(
                        text = "~ $formattedEndDate",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }

        val color = if (dDay.second) {
            MaterialTheme.colorScheme.outline
        } else {
            MaterialTheme.colorScheme.primary
        }
        Text(
            text = dDay.first,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(2.dp)
                .align(Alignment.TopEnd)
                .background(
                    color = color,
                    shape = CardDefaults.shape
                )
                .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest
        )
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