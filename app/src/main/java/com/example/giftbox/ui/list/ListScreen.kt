package com.example.giftbox.ui.list

import androidx.compose.animation.animateContentSize
import com.example.giftbox.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.utils.formatString
import com.example.giftbox.ui.utils.getDday

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(listViewModel: ListViewModel = viewModel(), onDetail: (Gift) -> Unit, onAdd: () -> Unit) {
    val refreshState = rememberPullToRefreshState()
    var showRemoveDlg by remember { mutableStateOf(false) }

    if (refreshState.isRefreshing) {
        listViewModel.getGiftList()
        listViewModel.setTopTitle(R.string.top_app_bar_recent)
        refreshState.endRefresh()
    }

    if (showRemoveDlg) {
        RemoveDialog(onConfirm = {
            listViewModel.removeGift()
            showRemoveDlg = false
        }) {
            showRemoveDlg = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                title = {
                    Text(text = stringResource(id = listViewModel.topTitle.value))
                },
                actions = {
                    TopAppBarDropDownMenu { title ->
                        listViewModel.setTopTitle(title)
                        listViewModel.orderBy()
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(refreshState.nestedScrollConnection)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // chip
                LazyRow {
                    listViewModel.chipElement.value?.let { chips ->
                        val keys = chips.keys.toList()
                        items(chips.size) { idx ->
                            val key = keys[idx]
                            FilterChip(
                                onClick = {
                                    listViewModel.changeChipState(listOf(key))
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

                // gift item
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    itemsIndexed(items = listViewModel.copyGiftList.value) { index, gift ->
                        val swipeState = rememberSwipeToDismissBoxState()

                        val text: String
                        val alignment: Alignment
                        val color: Color

                        when (swipeState.dismissDirection) {
                            SwipeToDismissBoxValue.EndToStart -> {
                                text = stringResource(id = R.string.txt_used)
                                alignment = Alignment.CenterEnd
                                color = colorResource(id = R.color.dark_green)
                            }

                            SwipeToDismissBoxValue.StartToEnd -> {
                                text = stringResource(id = R.string.txt_delete)
                                alignment = Alignment.CenterStart
                                color = Color.Red.copy(alpha = 0.8f)
                            }

                            SwipeToDismissBoxValue.Settled -> {
                                text = stringResource(id = R.string.txt_used)
                                alignment = Alignment.CenterEnd
                                color = Color.Green.copy(alpha = 0.8f)
                            }
                        }

                        SwipeToDismissBox(
                            modifier = Modifier.animateContentSize(),
                            state = swipeState,
                            enableDismissFromEndToStart = true,
                            enableDismissFromStartToEnd = true,
                            backgroundContent = {
                                Box(
                                    contentAlignment = alignment,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(3.dp)
                                        .clip(shape = RoundedCornerShape(10.dp))
                                        .background(color)
                                ) {
                                    Text(
                                        modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                                        text = text,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        ) {
                            GiftItem(gift = gift, formatString(gift.endDt), getDday(gift.endDt)) { onDetail(it) }
                        }

                        when (swipeState.currentValue) {
                            SwipeToDismissBoxValue.EndToStart -> {
                                LaunchedEffect(swipeState) {
                                    listViewModel.usedGift(gift)
                                    swipeState.snapTo(SwipeToDismissBoxValue.Settled)
                                }
                            }

                            SwipeToDismissBoxValue.StartToEnd -> {
                                LaunchedEffect(swipeState) {
                                    listViewModel.setRemoveGift(gift)
                                    swipeState.snapTo(SwipeToDismissBoxValue.Settled)
                                    showRemoveDlg = true
                                }
                            }

                            SwipeToDismissBoxValue.Settled -> {

                            }
                        }
                    }
                }
            }

            PullToRefreshContainer(
                state = refreshState,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopCenter)
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
}

@Composable
fun GiftItem(gift: Gift, formattedEndDate: String, dDay: Pair<String, Boolean>, onDetail: (Gift) -> Unit) {
    Box(
        modifier = Modifier
            .padding(3.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .clickable {
                onDetail(gift)
            }
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .height(120.dp)
        ) {
            Box(modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant)) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = gift.photo,
                    contentDescription = "photo",
                    contentScale = ContentScale.Crop
                )
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

@Composable
fun TopAppBarDropDownMenu(setTopTitle: (Int) -> Unit) {
    val expanded = remember {
        mutableStateOf(false)
    }

    Box {
        IconButton(onClick = {
            expanded.value = true
        }) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "More Filter"
            )
        }
    }

    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false },
    ) {
        val recentTitle = R.string.top_app_bar_recent
        DropdownMenuItem(
            onClick = {
                expanded.value = false
                setTopTitle(recentTitle)
            },
            text = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(id = recentTitle)
                )
            }
        )

        HorizontalDivider()

        val dDayTitle = R.string.top_app_bar_end_date
        DropdownMenuItem(
            onClick = { expanded.value = false
                setTopTitle(dDayTitle)
            },
            text = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = stringResource(id = dDayTitle)
                )
            }
        )
    }
}

// 기프티콘 제거 묻는 다이얼로그
@Composable
fun RemoveDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
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