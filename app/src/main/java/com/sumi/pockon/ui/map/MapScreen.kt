package com.sumi.pockon.ui.map

import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.hilt.navigation.compose.hiltViewModel
import com.sumi.pockon.R
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.ui.list.GiftItem
import com.sumi.pockon.util.formatString
import com.sumi.pockon.util.getDday

@Composable
fun MapScreen(onBack: () -> Unit, onDetail: (String) -> Unit) {
    val context = LocalContext.current
    var point by rememberSaveable { mutableStateOf<List<Gift>>(listOf()) }
    val hasFragmentBeenSet = remember { mutableStateOf(false) }
    val fragmentContainerView = remember {
        FragmentContainerView(context).apply {
            id = R.id.fragment_container_view
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                fragmentContainerView
            },
            update = {
                if (!hasFragmentBeenSet.value) {
                    val baseContext = (it.context as ContextWrapper).baseContext
                    val fragmentManager = (baseContext as FragmentActivity).supportFragmentManager

                    val fragment = MapFragment().apply {
                        setOnClickCallback { giftList ->
                            point = giftList
                        }
                    }

                    fragmentManager.commit {
                        replace(R.id.fragment_container_view, fragment)
                        addToBackStack(null)
                    }
                    hasFragmentBeenSet.value = true
                }
            }
        )

        if (point.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { point.size })
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fill,
                contentPadding = PaddingValues(horizontal = 15.dp), // 좌우 여백 추가
                pageSpacing = 5.dp, // 각 페이지 사이 여백 추가
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.BottomCenter)
            ) { pageIndex ->
                val gift = point[pageIndex]
                GiftItem(isEdit = false,
                    gift = gift,
                    formattedEndDate = formatString(gift.endDt),
                    dDay = getDday(gift.endDt),
                    isCheck = false,
                    onClick = {
                        // 상세보기 이동
                        onDetail(gift.id)
                    })
            }
        }
    }

    BackHandler {
        onBack()
    }
}