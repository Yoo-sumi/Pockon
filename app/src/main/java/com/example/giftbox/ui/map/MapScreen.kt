package com.example.giftbox.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.giftbox.model.Document
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.detail.DetailScreen
import com.example.giftbox.ui.list.GiftItem
import com.example.giftbox.ui.utils.formatString
import com.example.giftbox.ui.utils.getDday
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import kotlinx.coroutines.tasks.await
import kotlin.concurrent.thread

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(onBack: () -> Unit) {
    val mapViewModel = hiltViewModel<MapViewModel>()
    var markerList = rememberSaveable { mutableMapOf<String, Marker>() }
    var point: List<Gift> by rememberSaveable { mutableStateOf(listOf()) }
    val context = LocalContext.current
    var detailGift by rememberSaveable { mutableStateOf<Gift?>(null) }
    var naverMap: NaverMap? = remember { null }
    var lastLocation: Location? = remember { null }
    val mapView = remember { MapView(context) }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val activity = remember {
        context as? FragmentActivity
    }

    // 라이프사이클 처리
    LaunchedEffect(mapView) {
        mapView.onCreate(null)
        mapView.onResume()

        mapView.getMapAsync { map ->

            activity?.let {
                val locationSource = FusedLocationSource(it, 1000)
                lastLocation = locationSource.lastLocation
                map.locationSource = locationSource
            }
            map.uiSettings.isLocationButtonEnabled = false
            map.uiSettings.isZoomControlEnabled = false
            map.locationTrackingMode = LocationTrackingMode.Follow

            markerList = drawMap(
                map = map,
                displayInfoList = mapViewModel.displayInfoList.value,
                nearestDoc = mapViewModel.getNearestDoc(),
                onClick = { document, gift ->
                    if (document == null) {
                        point = gift
                    } else {
                        markerList.forEach { (id, marker) ->
                            if (id == document.id) {
                                // 뷰페이저 셋팅
                                point = gift

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
                    }
                }
            )

            naverMap = map
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView.onPause()
            mapView.onDestroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                mapView
            }
        )

// 현재 위치 버튼을 위한 AndroidView (LocationButtonView 사용)

        Box(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.TopCenter)
                .padding(10.dp)
        ) {
            // AndroidView 배치
            AndroidView(
                factory = { context ->
                    val locationButtonView = LocationButtonView(context).apply {
                        this.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    locationButtonView
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
            )

            // 그 위에 투명한 박스를 겹쳐 놓기
            Box(
                modifier = Modifier
                    .matchParentSize() // 부모 Box와 크기 맞추기
                    .background(androidx.compose.ui.graphics.Color.Transparent) // 투명한 배경
                    .clickable {
                        Log.d("지도", "locationSource ${lastLocation}")
                        lastLocation?.let {
                            val cameraUpdate = CameraUpdate.scrollTo(
                                LatLng(it.latitude, it.longitude)
                            )
                            naverMap?.moveCamera(cameraUpdate)
                        } ?: Log.d("지도", "현재 위치를 가져올 수 없습니다.")
                    }
            )
        }

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
                GiftItem(
                    isEdit = false,
                    gift = gift,
                    formattedEndDate = formatString(gift.endDt),
                    dDay = getDday(gift.endDt),
                    isCheck = false,
                    onClick = {
                        // 상세보기 이동
                        detailGift = gift
                    }
                )
            }
        }

        detailGift?.let {
            Box(modifier = Modifier.fillMaxSize()) {
                DetailScreen(id = it.id) {
                    detailGift = null
                }
            }
        }
    }

}
//
//@Composable
//fun CustomMapButton() {
//    Box(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        // 커스텀 위치 버튼
//        IconButton(
//            onClick = { /* 현재 위치로 이동하는 로직 추가 */ },
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(16.dp)
//                .background(Color.White, shape = CircleShape)
//                .border(1.dp, Color.Gray, CircleShape)
//        ) {
//            Icon(Icons.Default.LocationOn, contentDescription = "현재 위치", tint = Color.Blue)
//            Icon(Icons.Default.MyLocation, contentDescription = "현재 위치", tint = Color.Blue)
//        }
//
//        // 커스텀 확대 버튼
//        IconButton(
//            onClick = { googleMap.animateCamera(CameraUpdateFactory.zoomIn()) },
//            modifier = Modifier
//                .align(Alignment.BottomCenter) // 화면 하단 중앙에 배치
//                .padding(16.dp)
//                .background(Color.White, shape = CircleShape)
//                .border(1.dp, Color.Gray, shape = CircleShape)
//        ) {
//            Icon(Icons.Default.Add, contentDescription = "확대", tint = Color.Black)
//        }
//
//        // 커스텀 축소 버튼
//        IconButton(
//            onClick = { googleMap.animateCamera(CameraUpdateFactory.zoomOut()) },
//            modifier = Modifier
//                .align(Alignment.BottomCenter) // 화면 하단 중앙에 배치
//                .padding(16.dp)
//                .background(Color.White, shape = CircleShape)
//                .border(1.dp, Color.Gray, shape = CircleShape)
//        ) {
//            Icon(Icons.Default.Remove, contentDescription = "축소", tint = Color.Black)
//        }
//    }
//}

private fun drawMap(map: NaverMap, displayInfoList: List<Pair<Document, List<Gift>>>, nearestDoc: Document?, onClick: (document: Document?, List<Gift>) -> Unit): MutableMap<String, Marker> {
    val newMarkerList = mutableMapOf<String, Marker>()

    displayInfoList.forEach {
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
            onClick(document, it.second)
            false
        }

        // 가장 가까운 곳
        nearestDoc?.let { nearestDoc ->
            if (nearestDoc.x == it.first.x && nearestDoc.y == it.first.y) {
                // 초기엔 가장 가까운곳으로
                // 뷰페이저 셋팅
                onClick(null, it.second)

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
        newMarkerList[it.first.id] = marker
    }

    return newMarkerList
}