package com.example.giftbox.ui.map

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.giftbox.databinding.FragmentMapBinding
import com.example.giftbox.model.Document
import com.example.giftbox.model.Gift
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons

class MapFragment(
    private val mapViewModel: MapViewModel,
    val onClick: (Gift) -> Unit
) : Fragment(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private lateinit var binding: FragmentMapBinding
    private lateinit var mapView: MapView
    private lateinit var naverMap : NaverMap
    private lateinit var locationSource: FusedLocationSource
    private val markerList = mutableMapOf<String, Marker>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        return binding.root
    }

    override fun onMapReady(map: NaverMap) {
        this.naverMap = map
        // 현재 위치
        naverMap.locationSource = locationSource
        // 현재 위치 버튼 기능
        naverMap.uiSettings.isLocationButtonEnabled = false
        // 위치를 추적하면서 카메라도 따라 움직인다.
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        // 현재 위치 버튼
        binding.btnLocation.map = naverMap

        show()
    }

    private fun show() {
        markerList.clear()
        mapViewModel.displayInfoList.value.forEach {
            // 마커 찍기
            val marker = Marker()
            marker.position = LatLng(it.first.y.toDouble(), it.first.x.toDouble())
            marker.width = 70
            marker.height = 100
            marker.captionText = it.first.placeName
            marker.captionTextSize = 9F
            marker.captionRequestedWidth = 200
            marker.map = naverMap
            marker.tag = it.first
            marker.icon = MarkerIcons.BLACK
            marker.onClickListener = Overlay.OnClickListener { overlay ->
                val document = overlay.tag as Document
                markerList.forEach { (id, marker) ->
                    if (id == document.id) {
                        // 뷰페이저 셋팅
                        val adapter = GiftItemAdapter(it.second, requireActivity()) { gift ->
                            onClick(gift)
                        }
                        binding.viewPager.adapter = adapter

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
                naverMap.moveCamera(cameraUpdate)
                false
            }

            // 가장 가까운 곳
            mapViewModel.getNearestDoc()?.let { nearestDoc ->
                if (nearestDoc.x == it.first.x && nearestDoc.y == it.first.y) {
                    // 초기엔 가장 가까운곳으로
                    // 뷰페이저 셋팅
                    initViewPager()
                    val adapter = GiftItemAdapter(it.second, requireActivity()) { gift ->
                        onClick(gift)
                    }
                    binding.viewPager.adapter = adapter

                    // 카메라 이동
                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(nearestDoc.y.toDouble(), nearestDoc.x.toDouble()))
                    naverMap.moveCamera(cameraUpdate)

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

    private fun initViewPager() {
        val previewWidth = 45
        val itemMargin = 20
        val decoMargin = previewWidth + itemMargin
        val pageTransX = decoMargin + previewWidth
        val decoration = PageDecoration(decoMargin)

        binding.viewPager.also {
            it.offscreenPageLimit = 1
            it.addItemDecoration(decoration)
            it.setPageTransformer { page, position ->
                page.translationX = position * - pageTransX
            }
        }
    }
}

private class PageDecoration(private val margin: Int): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = margin
        outRect.right = margin
    }

}