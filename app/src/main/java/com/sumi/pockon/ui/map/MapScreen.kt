package com.sumi.pockon.ui.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.sumi.pockon.R
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.ui.list.GiftItem
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

    Box(modifier = Modifier.fillMaxSize()) {
        NaverMapWithCustomControls(mapViewModel.cameraPosition) {
            mapViewModel.cameraPosition = it
        }
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
fun NaverMapWithCustomControls(
    cameraPosition: CameraPosition?,
    onChangeCamera: (CameraPosition) -> Unit
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }) { view ->
            view.getMapAsync { naverMap ->
                // 기본 UI 비활성화
                naverMap.uiSettings.isLocationButtonEnabled = false
                naverMap.uiSettings.isZoomControlEnabled = false

                // 저장된 상태 복원
                cameraPosition?.let {
                    val update = CameraUpdate.toCameraPosition(it)
                    naverMap.moveCamera(update)
                }

                // 상태 변경 감지 및 저장
                naverMap.addOnCameraChangeListener { _, _ ->
                    onChangeCamera(naverMap.cameraPosition)
                }

                // 최초에 한번만 Follow 모드 설정
                if (naverMap.locationTrackingMode != LocationTrackingMode.Follow) {
                    naverMap.locationTrackingMode = LocationTrackingMode.Follow
                }
            }
        }

        // 내 위치 버튼
        IconButton(
            onClick = {
                mapView.getMapAsync { map ->
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val latLng = LatLng(it.latitude, it.longitude)

                            // 1. Follow 모드 해제
                            map.locationTrackingMode = LocationTrackingMode.None

                            // 2. 카메라 이동
                            val cameraUpdate = CameraUpdate.scrollTo(latLng)
                                .animate(CameraAnimation.Easing)

                            map.moveCamera(cameraUpdate)
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