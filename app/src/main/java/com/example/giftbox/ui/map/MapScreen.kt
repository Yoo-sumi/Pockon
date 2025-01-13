package com.example.giftbox.ui.map

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.giftbox.R
import com.example.giftbox.ui.detail.DetailScreen

@Composable
fun MapScreen(onBack: () -> Unit) {
    val mapViewmodel = hiltViewModel<MapViewModel>()

    if (mapViewmodel.selectedGift.value != null) {
        // 클릭한 기프티콘 상세보기
        DetailScreen(mapViewmodel.selectedGift.value!!) {
            mapViewmodel.setSelectedGift(null)
        }
    } else if (mapViewmodel.displayInfoList.value.isNotEmpty()) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth(),
            factory = { context ->
                FragmentContainerView(context).apply {
                    id = R.id.fragment_container_view
                }
            },
            update = {
                val fragmentManager = (it.context as FragmentActivity).supportFragmentManager
                fragmentManager.commit {
                    replace(
                        R.id.fragment_container_view,
                        MapFragment(mapViewmodel) { gift ->
                            mapViewmodel.setSelectedGift(gift)
                        }
                    )
                }
            }
        )
    }

}