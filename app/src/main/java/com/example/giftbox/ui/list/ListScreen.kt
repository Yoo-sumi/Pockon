package com.example.giftbox.ui.list

import androidx.compose.animation.animateContentSize
import com.example.giftbox.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.utils.formatString
import com.example.giftbox.ui.utils.getDday

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(listViewModel: ListViewModel = viewModel(), onDetail: (String) -> Unit, onAdd: () -> Unit) {
    val refreshState = rememberPullToRefreshState()
    var showRemoveDlg by rememberSaveable { mutableStateOf(false) }
    var isEdit by rememberSaveable { mutableStateOf(false) }
    var isAllSelect by rememberSaveable { mutableStateOf(false) }

    if (refreshState.isRefreshing) {
        listViewModel.getGiftList()
        listViewModel.setTopTitle(R.string.top_app_bar_recent)
        refreshState.endRefresh()
        isEdit = false
        listViewModel.clearCheckedGiftList()
    }

    if (showRemoveDlg) {
        RemoveDialog(onConfirm = {
            showRemoveDlg = false
            if (isEdit) {
                listViewModel.deleteSelection {
                    isEdit = !isEdit
                    isAllSelect = false
                    listViewModel.clearCheckedGiftList()
                }
            } else {
                listViewModel.removeGift()
            }
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
                    TopAppBarDropDownMenu(listViewModel.topTitle.value) { title ->
                        listViewModel.setTopTitle(title)
                        listViewModel.orderBy()
                    }
                },
                actions = {
                    val title = if (isEdit) {
                        R.string.btn_delete
                    } else {
                        R.string.btn_edit
                    }
                    Text(
                        modifier = Modifier
                            .padding(end = 15.dp)
                            .clickable {
                                // 삭제
                                if (isEdit) {
                                    if (listViewModel.checkedGiftList.value.isEmpty()) isEdit = false
                                    else showRemoveDlg = true
                                } else { // 편집
                                    isEdit = true
                                    isAllSelect = false
                                    listViewModel.clearCheckedGiftList()
                                }
                            },
                        text = stringResource(id = title),
                        fontSize = 14.sp
                    )
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
                modifier = Modifier.padding(end = 20.dp, start = 20.dp, top = 10.dp)
            ) {
                // chip
                LazyRow {
                    listViewModel.chipElement.value?.let { chips ->
                        val keys = chips.keys.toList()
                        items(chips.size) { idx ->
                            val key = keys[idx]
                            FilterChip(
                                onClick = {
                                    isAllSelect = false
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

                // all delete
                if (isEdit) {
                    Row(
                        modifier = Modifier.padding(top = 5.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                            Checkbox(
                                modifier = Modifier
                                    .scale(0.8f),
                                checked = isAllSelect,
                                onCheckedChange = {
                                    isAllSelect = !isAllSelect
                                    listViewModel.selectAll(isAllSelect)
                                }
                            )
                        }
                        Text(text = "전체 선택")
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
                            GiftItem(
                                isEdit = isEdit,
                                gift = gift,
                                formattedEndDate = formatString(gift.endDt),
                                dDay = getDday(gift.endDt),
                                isCheck = listViewModel.checkedGiftList.value.contains(gift.id),
                                onClick = {
                                    if (isEdit) listViewModel.checkedGift(gift.id)
                                    else onDetail(gift.id)
                                }
                            )
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
fun GiftItem(isEdit: Boolean, gift: Gift, formattedEndDate: String, dDay: Pair<String, Boolean>, isCheck: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth()
            .padding(3.dp)
            .clip(shape = RoundedCornerShape(10.dp))
            .clickable {
                onClick()
            }
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .fillMaxHeight()
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

        // selected color
        if (isEdit && isCheck) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.3f))
            ) {
                CompositionLocalProvider(LocalContentColor provides Color.White) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(30.dp),
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "check")
                }
            }
        }
    }
}

@Composable
fun TopAppBarDropDownMenu(topTitle: Int, setTopTitle: (Int) -> Unit) {
    val expanded = remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier.clickable {
            expanded.value = true
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = topTitle),
            fontSize = 20.sp
        )
        Icon(
            Icons.Filled.KeyboardArrowDown,
            contentDescription = "More Filter"
        )
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
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // title
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "info",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(10.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(color = Color.LightGray)
                )

                // text
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    text = stringResource(id = R.string.dlg_msg_delete),
                    fontSize = 16.sp
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
                        onClick = { onConfirm() },
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

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(color = Color.LightGray)
                    )

                    Button(
                        onClick = { onDismiss() },
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
                }
            }
        }
    }
}