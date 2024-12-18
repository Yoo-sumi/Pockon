package com.example.giftbox.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.example.giftbox.R


@Composable
fun MapScreen(onBack: () -> Unit) {
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
                    MapFragment()
                )
            }
        }
    )
}