package com.example.giftbox.ui.map

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.giftbox.model.Document
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.list.GiftItem
import com.example.giftbox.ui.utils.formatString
import com.example.giftbox.ui.utils.getDday
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons

@Composable
fun MapScreen(onBack: () -> Unit) {
    val mapViewModel = hiltViewModel<MapViewModel>()
    val markerList = remember { mutableMapOf<String, Marker>() }
    var point: List<Gift> by rememberSaveable { mutableStateOf(listOf()) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context).apply {
                    getMapAsync { map ->
                        // 지도 초기화가 완료되었을 때 호출되는 콜백
                        // 지도 초기화 완료 후 처리
                        val activity = context as? FragmentActivity
                        activity?.let {
                            val locationSource = FusedLocationSource(it, 1)
                            map.locationSource = locationSource
                        }
                        map.uiSettings.isLocationButtonEnabled = true
                        map.locationTrackingMode = LocationTrackingMode.Follow
                        mapViewModel.displayInfoList.value.forEach {
                            // 마커 찍기
                            val marker = Marker()
                            marker.position = LatLng(it.first.y.toDouble(), it.first.x.toDouble())
                            marker.width = 70
                            marker.height = 100
                            marker.captionText = it.first.placeName
                            marker.captionTextSize = 9F
                            marker.captionRequestedWidth = 200
                            marker.map = map
                            marker.tag = it.first
                            marker.icon = MarkerIcons.BLACK
                            marker.onClickListener = Overlay.OnClickListener { overlay ->
                                val document = overlay.tag as Document
                                markerList.forEach { (id, marker) ->
                                    if (id == document.id) {
                                        // 뷰페이저 셋팅
                                        point = it.second

                                        marker.iconTintColor = Color.RED
                                        marker.width = 90
                                        marker.height = 120
                                    } else {
                                        marker.iconTintColor = Color.parseColor("#00db77")
                                        marker.width = 70
                                        marker.height = 100
                                    }
                                }
                                // 카메라 이동
                                val cameraUpdate = CameraUpdate.scrollTo(LatLng(document.y.toDouble(), document.x.toDouble()))
                                    .animate(CameraAnimation.Easing)
                                map.moveCamera(cameraUpdate)
                                false
                            }

                            // 가장 가까운 곳
                            mapViewModel.getNearestDoc()?.let { nearestDoc ->
                                if (nearestDoc.x == it.first.x && nearestDoc.y == it.first.y) {
                                    // 초기엔 가장 가까운곳으로
                                    // 뷰페이저 셋팅
                                    point = it.second

                                    // 카메라 이동
                                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(nearestDoc.y.toDouble(), nearestDoc.x.toDouble()))
                                    map.moveCamera(cameraUpdate)

                                    marker.iconTintColor = Color.RED
                                    marker.width = 90
                                    marker.height = 120
                                } else {
                                    marker.iconTintColor = Color.parseColor("#00db77")
                                }
                            }
                            markerList[it.first.id] = marker
                        }
                    }
                }
            }
        )

        if (point.isNotEmpty() ) {
            val pagerState = rememberPagerState(pageCount = { point.size })
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fill,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.BottomCenter)
            ) { pageIndex ->
                val gift = point[pageIndex]
                GiftItem(
                    isEdit = false,
                    gift = gift,
                    formattedEndDate = formatString(gift.endDt),
                    dDay = getDday(gift.endDt),
                    isCheck = false,
                    onClick = {
                        // 상세보기 이동
                    }
                )
            }
        }
    }

}
