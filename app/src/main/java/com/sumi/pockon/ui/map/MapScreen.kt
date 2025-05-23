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
    var point by rememberSaveable { mutableStateOf<List<Gift>>(listOf()) }
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
            nearestDoc = mapViewModel.getNearestDoc(),
            cameraPosition = mapViewModel.cameraPosition.value,
            currentLocation = mapViewModel.currentLocation.value,
            onCameraChanged = {
                mapViewModel.updateCameraPosition(it)
            },
            onLocationUpdate = {
                mapViewModel.updateCurrentLocation(it)
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

@SuppressLint("MissingPermission")
@Composable
fun NaverMapWithLiveLocation(
    fusedLocationClient: FusedLocationProviderClient,
    displayInfoList: List<Pair<Document, List<Gift>>>?,
    nearestDoc: Document?,
    cameraPosition: CameraPosition?,
    currentLocation: LatLng?,
    onCameraChanged: (CameraPosition) -> Unit,
    onLocationUpdate: (LatLng) -> Unit
) {
    val mapView = rememberMapViewWithLifecycle()
    var isInitialCameraMoved by remember { mutableStateOf(false) }
    val locationRef = remember { mutableStateOf<CircleOverlay?>(null) }
    val markerRefs = remember { mutableStateListOf<Marker?>() }
//    val markerList = remember { mutableStateListOf<Marker>() }

    // 위치 업데이트를 DisposableEffect로 관리
    DisposableEffect(Unit) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        ).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    onLocationUpdate(LatLng(it.latitude, it.longitude))
                    if (!isInitialCameraMoved) {
                        onCameraChanged(
                            CameraPosition(
                                LatLng(it.latitude, it.longitude),
                                15.0
                            )
                        )
                    }
                    isInitialCameraMoved = true
                }
            }
        }

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
                        locationRef.value = CircleOverlay().apply {
                            center = latLng
                            radius = 10.0 // 반지름 (미터 단위)
                            color = Color.Blue.copy(alpha = 0.8f).toArgb()
                            outlineColor = Color.Blue.toArgb()
                            outlineWidth = 2
                            map = naverMap
                        }
                    } else {
                        (locationRef.value)?.center = latLng
                    }
//                    if (markerRef.value == null) {
//                        markerRef.value = Marker().apply {
//                            position = latLng
//                            iconTintColor = Color.BLUE.hashCode()
//                            width = 80
//                            height = 80
//                            map = naverMap
//                        }
//                    } else {
//                        markerRef.value?.position = latLng
//                    }
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
                        width = 70
                        height = 100
                        captionText = info.first.placeName
                        captionTextSize = 9F
                        captionRequestedWidth = 200
                        map = naverMap
                        tag = info.first
                        icon = MarkerIcons.BLACK
//                        iconTintColor = if (index == selectedIndex) Color.Red.hashCode() else Color.Blue.hashCode()
                        setOnClickListener {
//                            mapViewModel.selectMarker(index)
                            true
                        }
                    }
                    if (markerRefs[index] == null) {
                        markerRefs[index] = marker
                    } else {
                        markerRefs[index]?.position = LatLng(info.first.y.toDouble(), info.first.x.toDouble())
//                        markerRefs[index]?.iconTintColor = if (index == selectedIndex) Color.Red.hashCode() else Color.Blue.hashCode()
                    }
                }

                naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
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
                                    15.0
                                )
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp)
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
                .padding(end = 16.dp, bottom = 160.dp)
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
                .padding(end = 16.dp, bottom = 220.dp)
                .background(Color.White, shape = CircleShape)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "축소", tint = Color.Black)
        }

        if (currentLocation == null) LoadingScreen()
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