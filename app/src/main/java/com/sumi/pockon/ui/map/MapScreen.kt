package com.sumi.pockon.ui.map

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
import com.sumi.pockon.ui.detail.DetailScreen
import com.sumi.pockon.ui.home.checkLocationPermission
import com.sumi.pockon.ui.list.GiftItem
import com.sumi.pockon.ui.loading.LoadingScreen
import com.sumi.pockon.util.formatString
import com.sumi.pockon.util.getDday

@Composable
fun MapScreen(onBack: () -> Unit, onDetail: (String) -> Unit) {
    val mapViewModel = hiltViewModel<MapViewModel>()
    val context = LocalContext.current
    var detailGift by rememberSaveable { mutableStateOf<Gift?>(null) }
    var isTopScroll by rememberSaveable { mutableStateOf<Boolean?>(false) }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (detailGift != null) {
            // 상세보기
            DetailScreen(id = detailGift!!.id) {
                detailGift = null
            }
        } else {
            NaverMapWithLiveLocation(
                fusedLocationClient = fusedLocationClient,
                mapViewModel = mapViewModel,
                isTopScroll = {
                    isTopScroll = it
                }
            )

            // 뷰페이저
            if (mapViewModel.displayInfoList.value?.isNotEmpty() == true) {
                mapViewModel.selectedMarkerIndex.value?.let { index ->
                    val point = mapViewModel.displayInfoList.value?.get(index)?.second
                    if ((point?.size ?: 0) < 1) return
                    val pagerState = rememberPagerState(pageCount = { point!!.size })
                    // 상세 보기에서 돌아올 때 기존 페이지 유지
                    LaunchedEffect(Unit) {
                        pagerState.scrollToPage(mapViewModel.getPageIndex())
                    }
                    LaunchedEffect(isTopScroll) {
                        if (isTopScroll == true) {
                            pagerState.scrollToPage(0)
                            isTopScroll = false
                        }
                    }
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
                                mapViewModel.setPageIndex(pagerState.currentPage)
                            }
                        )
                    }
                }
            }
            if (mapViewModel.currentLocation.value == null) LoadingScreen()
        }
    }

    BackHandler {
        onBack()
    }
}

@Composable
fun NaverMapWithLiveLocation(
    fusedLocationClient: FusedLocationProviderClient,
    mapViewModel: MapViewModel,
    isTopScroll: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    val locationRef = remember { mutableStateOf<CircleOverlay?>(null) }
    val markerRefs = remember { mutableStateListOf<Marker?>() }

    // 위치 업데이트를 DisposableEffect로 관리
    DisposableEffect(Unit) {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    mapViewModel.updateCurrentLocation(latLng)
                }
            }
        }

        if (checkLocationPermission(context)) {
            // 1. 먼저 단발성으로 빠르게 현재 위치 가져오기
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    mapViewModel.updateCurrentLocation(latLng)
                    // 카메라 이동은 최초 1회만
                    if (!mapViewModel.getIsInitialCameraMoved()) mapViewModel.updateCameraPosition(
                        CameraPosition(latLng, 14.0)
                    )
                    mapViewModel.setIsInitialCameraMoved(true)
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
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }) { view ->
            view.getMapAsync { naverMap ->
                naverMap.uiSettings.isLocationButtonEnabled = false
                naverMap.uiSettings.isZoomControlEnabled = false

                mapViewModel.cameraPosition.value?.let { position ->
                    val update = CameraUpdate.toCameraPosition(position)
                    naverMap.moveCamera(update)
                }

                naverMap.addOnCameraChangeListener { _, _ ->
                    mapViewModel.updateCameraPosition(naverMap.cameraPosition)
                }

                // 실시간 내 위치 마커
                mapViewModel.currentLocation.value?.let { latLng ->
                    if (locationRef.value == null) {
                        val locationOverlay = naverMap.locationOverlay
                        locationOverlay.isVisible = true
                        locationOverlay.position = latLng
                    }
                }

                // 마커 초기화
                if (markerRefs.size != mapViewModel.displayInfoList.value?.size) {
                    markerRefs.clear()
                    mapViewModel.displayInfoList.value?.let { displayInfoList ->
                        markerRefs.addAll(List(displayInfoList.size) { null })
                    }
                }

                // 마커 표시
                mapViewModel.displayInfoList.value?.forEachIndexed { index, info ->
                    val marker = markerRefs[index] ?: Marker().apply {
                        position = LatLng(info.first.y.toDouble(), info.first.x.toDouble())
                        width = if (index == mapViewModel.selectedMarkerIndex.value) 80 else 70
                        height = if (index == mapViewModel.selectedMarkerIndex.value) 110 else 100
                        captionText = info.first.placeName
                        captionTextSize = 9F
                        captionRequestedWidth = 200
                        map = naverMap
                        tag = info.first
                        icon = MarkerIcons.BLACK
                        iconTintColor = if (index == mapViewModel.selectedMarkerIndex.value) android.graphics.Color.RED else android.graphics.Color.parseColor("#00db77")
                        setOnClickListener { overlay ->
                            val document = overlay.tag as? Document ?: return@setOnClickListener false
                            val clickedIndex = mapViewModel.displayInfoList.value?.indexOfFirst { it.first.id == document.id }
                            if (clickedIndex == -1 || clickedIndex == null) return@setOnClickListener false
                            mapViewModel.selectMarker(clickedIndex)
                            isTopScroll(true)
                            mapViewModel.displayInfoList.value?.forEachIndexed { index, _ ->
                                val marker = markerRefs.getOrNull(index)
                                if (index == clickedIndex) {
                                    // 선택된 마커 강조
                                    marker?.apply {
                                        iconTintColor = android.graphics.Color.RED
                                        width = 75
                                        height = 105
                                    }

                                    // 카메라 이동
                                    val latLng = LatLng(document.y.toDouble(), document.x.toDouble())
                                    mapViewModel.updateCameraPosition(
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
                            mapViewModel.updateCurrentLocation(LatLng(it.latitude, it.longitude))
                            mapViewModel.updateCameraPosition(
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
                .background(MaterialTheme.colorScheme.background, shape = CircleShape)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "my location", tint = MaterialTheme.colorScheme.onPrimary)
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
                .background(MaterialTheme.colorScheme.background, shape = CircleShape)
        ) {
            Icon(Icons.Default.Add, contentDescription = "zoom in", tint = MaterialTheme.colorScheme.onPrimary)
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
                .background(MaterialTheme.colorScheme.background, shape = CircleShape)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "zoom out", tint = MaterialTheme.colorScheme.onPrimary)
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