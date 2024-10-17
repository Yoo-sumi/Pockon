package com.example.giftbox.ui.list

import com.example.giftbox.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.utils.stringTobitmap

data class ChipState(
    var text: String,
    var isSelected: MutableState<Boolean>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(modifier: Modifier = Modifier) {
    val listViewModel = hiltViewModel<ListViewModel>()
    val refreshState = rememberPullToRefreshState()

    if (refreshState.isRefreshing) {
        listViewModel.getGiftList()
        refreshState.endRefresh()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(refreshState.nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            LazyRow {
                listViewModel.chipElement.value?.let { chips ->
                    val keys = chips.keys.toList()
                    items(chips.size){ idx ->
                        val key = keys[idx]
                        FilterChip(
                            onClick = {
                                listViewModel.changeChipState(key)
                            },
                            label = {
                                if (idx == 0) {
                                    Text(
                                        color =  if (chips[key] == true) MaterialTheme.colorScheme.onPrimary else Color.Unspecified,
                                        text = stringResource(id = R.string.chip_all),
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        color =  if (chips[key] == true) MaterialTheme.colorScheme.onPrimary else Color.Unspecified,
                                        text = keys[idx],
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            selected = chips[key] ?: false,
                            shape = RoundedCornerShape(50.dp),
                            colors = FilterChipDefaults.filterChipColors().copy(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                selectedContainerColor = MaterialTheme.colorScheme.primary
                            ),
                            border = null
                        )
                        Spacer(modifier = Modifier.padding(3.dp))
                    }
                }
            }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    )
            ) {
                items(items = listViewModel.copyGiftList.value,
                    key = { gift -> gift.document }
                ) { gift ->
                    GiftItem(gift = gift, listViewModel.formatString(gift.endDate), listViewModel.getDday(gift.endDate))
                }
            }
        }

        PullToRefreshContainer(
            state = refreshState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun GiftItem(gift: Gift, formattedEndDate: String, dDay: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(3.dp)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .height(120.dp)
            ) {
                Box(modifier = Modifier
                    .size(120.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)) {
                    stringTobitmap(gift.photo)?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "photo",
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .align(Alignment.Bottom)
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = gift.brand
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        text = gift.name
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right,
                        text = "~ $formattedEndDate"
                    )
                }
            }

            Text(
                text = "D${dDay}",
                modifier = Modifier
                    .padding(2.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CardDefaults.shape
                    )
                    .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            )
        }
    }
}

@Preview
@Composable
fun ListScreenPrieview() {
    ListScreen()
}