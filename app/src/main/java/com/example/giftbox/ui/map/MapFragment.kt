package com.example.giftbox.ui.map

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.giftbox.databinding.FragmentMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private lateinit var binding: FragmentMapBinding
    private lateinit var mapView: MapView
    private lateinit var naverMap : NaverMap
    private lateinit var locationSource: FusedLocationSource
    private val mapViewModel: MapViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        mapViewModel.keywordList.observe(viewLifecycleOwner) {
            val documentList = mapViewModel.getDocumentList()
            documentList.forEach { documents ->
                documents.forEach { document ->
                    // 마커 찍기
                    val marker = Marker()
                    marker.position = LatLng(document.y.toDouble(), document.x.toDouble())
                    marker.width = 70
                    marker.height = 100
                    marker.map = naverMap
                }
            }
        }

        mapViewModel.giftList.observe(viewLifecycleOwner) { giftList ->
            val adapter = GiftItemAdapter(giftList, requireActivity())
            binding.viewPager.adapter = adapter
            // 뷰페이저 초기화
            initViewPager()
        }

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

        mapViewModel.getAllBrands() // 로컬에서 가져오기
        mapViewModel.getAllGift() // 로컬에서 가져오기
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