package com.sumi.pockon.ui.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import com.sumi.pockon.R
import com.sumi.pockon.data.model.Document
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.ui.list.GiftItem
import com.sumi.pockon.ui.loading.LoadingScreen
import com.sumi.pockon.util.formatString
import com.sumi.pockon.util.getDday

@Composable
fun MapScreen(onBack: () -> Unit, onDetail: (String) -> Unit) {
    val mapViewModel = hiltViewModel<MapViewModel>()
    val context = LocalContext.current
//    var point by rememberSaveable { mutableStateOf<List<Gift>>(listOf()) }
    val hasFragmentBeenSet = remember { mutableStateOf(false) }
    val fragmentContainerView = remember {
        FragmentContainerView(context).apply {
            id = R.id.fragment_container_view
        }
    }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NaverMapWithLiveLocation(
            fusedLocationClient = fusedLocationClient,
            displayInfoList = mapViewModel.displayInfoList.value,
            selectedIndex = mapViewModel.selectedMarkerIndex.value,
            nearestDoc = mapViewModel.getNearestDoc(),
            cameraPosition = mapViewModel.cameraPosition.value,
            currentLocation = mapViewModel.currentLocation.value,
            onCameraChanged = {
                mapViewModel.updateCameraPosition(it)
            },
            onLocationUpdate = {
                mapViewModel.updateCurrentLocation(it)
            },
            onClick = { index ->
                mapViewModel.selectMarker(index)
//                point = giftList
            }
        )
//        AndroidView(
//            modifier = Modifier.fillMaxSize(),
//            factory = {
//                fragmentContainerView
//            },
//            update = {
//                if (!hasFragmentBeenSet.value) {
//                    val baseContext = (it.context as ContextWrapper).baseContext
//                    val fragmentManager = (baseContext as FragmentActivity).supportFragmentManager
//
//                    val fragment = MapFragment().apply {
//                        setOnClickCallback { giftList ->
//                            point = giftList
//                        }
//                    }
//
//                    fragmentManager.commit {
//                        replace(R.id.fragment_container_view, fragment)
//                        addToBackStack(null)
//                    }
//                    hasFragmentBeenSet.value = true
//                }
//            }
//        )

        if (mapViewModel.displayInfoList.value?.isNotEmpty() == true) {
            mapViewModel.selectedMarkerIndex.value?.let { index ->
                val point = mapViewModel.displayInfoList.value?.get(index)?.second
                if ((point?.size ?: 0) < 1) return
                val pagerState = rememberPagerState(pageCount = { point!!.size })
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
                    val gift = point!![pageIndex]
                    GiftItem(isEdit = false,
                        gift = gift,
                        formattedEndDate = formatString(gift.endDt),
                        dDay = getDday(gift.endDt),
                        isCheck = false,
                        onClick = {
                            // 상세보기 이동
                            onDetail(gift.id)
                        }
                    )
                }
            }
        }
        if (mapViewModel.currentLocation.value == null) LoadingScreen()
    }

    BackHandler {
        onBack()
    }
}

@SuppressLint("MissingPermission")
@Composable
fun NaverMapWithLiveLocation(
    fusedLocationClient: FusedLocationProviderClient,
    displayInfoList: List<Pair<Document, List<Gift>>>?,
    selectedIndex: Int?,
    nearestDoc: Document?,
    cameraPosition: CameraPosition?,
    currentLocation: LatLng?,
    onCameraChanged: (CameraPosition) -> Unit,
    onLocationUpdate: (LatLng) -> Unit,
    onClick: (Int) -> Unit
) {
    val mapView = rememberMapViewWithLifecycle()
    var isInitialCameraMoved by remember { mutableStateOf(false) }
    val locationRef = remember { mutableStateOf<CircleOverlay?>(null) }
    val markerRefs = remember { mutableStateListOf<Marker?>() }
//    val markerList = remember { mutableStateListOf<Marker>() }

    // 위치 업데이트를 DisposableEffect로 관리
    DisposableEffect(Unit) {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    onLocationUpdate(latLng)
                    // 카메라 이동은 최초 1회만
                    if (!isInitialCameraMoved) {
                        onCameraChanged(CameraPosition(latLng, 14.0))
                        isInitialCameraMoved = true
                    }
                }
            }
        }

        // 1. 먼저 단발성으로 빠르게 현재 위치 가져오기
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                onLocationUpdate(latLng)
                // 카메라 이동은 최초 1회만
                if (!isInitialCameraMoved) {
                    onCameraChanged(CameraPosition(latLng, 14.0))
                    isInitialCameraMoved = true
                }
            }
        }

        // 2. 이후 주기적인 위치 업데이트 요청
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L // 2초마다 위치 업데이트
        ).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        onDispose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }) { view ->
            view.getMapAsync { naverMap ->
                naverMap.uiSettings.isLocationButtonEnabled = false
                naverMap.uiSettings.isZoomControlEnabled = false

                if (cameraPosition != null) {
                    val update = CameraUpdate.toCameraPosition(cameraPosition)
                    naverMap.moveCamera(update)
                }

                naverMap.addOnCameraChangeListener { _, _ ->
                    onCameraChanged(naverMap.cameraPosition)
                }

                // 실시간 내 위치 마커 - 도형으로 표시
                currentLocation?.let { latLng ->
                    if (locationRef.value == null) {
                        // 정확도 범위 원 (얕은 파란색)
                        val accuracyCircle = CircleOverlay().apply {
                            center = latLng
                            radius = 30.0 // 위치 정확도 반영 (ex. 30m)
                            color = Color(0x330096FF).copy(0.2f).toArgb() // 연한 파란색 (#0096FF 20% 투명)
                            outlineColor = Color(0x550096FF).toArgb() // 테두리 약간 진하게
                            outlineWidth = 2
                            map = naverMap
                        }

// 중앙 점 (진한 파란색 점)
                        val centerDot = CircleOverlay().apply {
                            center = latLng
                            radius = 6.0 // 반지름 6m = 화면에서 점처럼 보임
                            color = Color(0xFF0096FF.toInt()).toArgb() // 진한 파란색
                            outlineColor = Color.White.toArgb()
                            outlineWidth = 2
                            map = naverMap
                        }
//                        locationRef.value = CircleOverlay().apply {
//                            center = latLng
//                            radius = 15.0 // 반지름 (미터 단위)
//                            color = Color.Blue.copy(alpha = 0.8f).toArgb()
//                            outlineColor = Color.Blue.toArgb()
//                            outlineWidth = 2
//                            map = naverMap
//                        }
                    } else {
                        (locationRef.value)?.center = latLng
                    }
                }

                // 마커 초기화
                if (markerRefs.size != displayInfoList?.size) {
                    markerRefs.clear()
                    if (displayInfoList != null) {
                        markerRefs.addAll(List(displayInfoList.size) { null })
                    }
                }

                // 마커 표시
                displayInfoList?.forEachIndexed { index, info ->
                    val marker = markerRefs[index] ?: Marker().apply {
                        position = LatLng(info.first.y.toDouble(), info.first.x.toDouble())
                        width = if (index == selectedIndex) 80 else 70
                        height = if (index == selectedIndex) 110 else 100
                        captionText = info.first.placeName
                        captionTextSize = 9F
                        captionRequestedWidth = 200
                        map = naverMap
                        tag = info.first
                        icon = MarkerIcons.BLACK
                        iconTintColor = if (index == selectedIndex) android.graphics.Color.RED else android.graphics.Color.parseColor("#00db77")
                        setOnClickListener { overlay ->
                            val document = overlay.tag as? Document ?: return@setOnClickListener false
                            val clickedIndex = displayInfoList.indexOfFirst { it.first.id == document.id }
                            if (clickedIndex == -1) return@setOnClickListener false
                            onClick.invoke(clickedIndex)
                            displayInfoList.forEachIndexed { index, _ ->
                                val marker = markerRefs.getOrNull(index)
                                if (index == clickedIndex) {
                                    // 선택된 마커 강조
                                    marker?.apply {
                                        iconTintColor = android.graphics.Color.RED
                                        width = 80
                                        height = 110
                                    }

                                    // ViewPager 업데이트 필요 시 콜백
//                                    onClick?.invoke(displayInfoList[index].second)

                                    // 카메라 이동
                                    val latLng = LatLng(document.y.toDouble(), document.x.toDouble())
                                    onCameraChanged(
                                        CameraPosition(
                                            latLng,
                                            14.0
                                        )
                                    )
                                } else {
                                    // 나머지 마커 기본 스타일
                                    marker?.apply {
                                        iconTintColor = android.graphics.Color.parseColor("#00db77")
                                        width = 70
                                        height = 100
                                    }
                                }
                            }
                            true
                        }
//                        if (nearestDoc?.x == info.first.x && nearestDoc?.y == info.first.y) {
//                            onClick.invoke(info.second)
//                        }
                    }
                    markerRefs[index] = marker
                }

                naverMap.locationTrackingMode = LocationTrackingMode.Follow
            }
        }

        // 내 위치 버튼
        IconButton(
            onClick = {
                mapView.getMapAsync { map ->
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            onLocationUpdate(LatLng(it.latitude, it.longitude))
                            onCameraChanged(
                                CameraPosition(
                                    LatLng(it.latitude, it.longitude),
                                    14.0
                                )
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 200.dp)
                .background(Color.White, shape = CircleShape)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "내 위치", tint = Color.Black)
        }


        // 확대 버튼
        IconButton(
            onClick = {
                mapView.getMapAsync { map ->
                    val zoom = map.cameraPosition.zoom + 1
                    map.moveCamera(CameraUpdate.zoomTo(zoom))
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 260.dp)
                .background(Color.White, shape = CircleShape)
        ) {
            Icon(Icons.Default.Add, contentDescription = "확대", tint = Color.Black)
        }

        // 축소 버튼
        IconButton(
            onClick = {
                mapView.getMapAsync { map ->
                    val zoom = map.cameraPosition.zoom - 1
                    map.moveCamera(CameraUpdate.zoomTo(zoom))
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 320.dp)
                .background(Color.White, shape = CircleShape)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "축소", tint = Color.Black)
        }
    }
}


@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map_view
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                mapView.onCreate(Bundle())
            }

            override fun onStart(owner: LifecycleOwner) {
                mapView.onStart()
            }

            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }

            override fun onStop(owner: LifecycleOwner) {
                mapView.onStop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    return mapView
}