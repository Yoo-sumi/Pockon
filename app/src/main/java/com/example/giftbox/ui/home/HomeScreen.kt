package com.example.giftbox.ui.home

import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.giftbox.R
import com.example.giftbox.databinding.FragmentMapBinding
import com.example.giftbox.ui.list.TopAppBarDropDownMenu
import com.example.giftbox.ui.pin.PinSettingDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onAdd: () -> Unit) {
//    val homeViewModel = hiltViewModel<HomeViewModel>()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                title = {
                    Text(text = stringResource(id = R.string.home))
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = stringResource(id = R.string.txt_use_near),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = stringResource(id = R.string.btn_open_map),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                GiftItem()
            }

            SmallFloatingActionButton(
                onClick = {
                    onAdd()
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.secondary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(15.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "")
            }
        }
    }
}

@Composable
fun GiftItem() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.width(150.dp)
        ) {
            Image(
                modifier = Modifier.size(150.dp),
                painter = painterResource(id = R.drawable.icon_add_photo),
                contentDescription = "use photo",
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(5.dp)
            ) {

                Text(
                    text = "파리바게뜨",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = "[파리바게뜨] 교환권 5,000원권",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Box {
                    Text(
                        text = "300m",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Text(
                        text = "2024.12.17",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}
