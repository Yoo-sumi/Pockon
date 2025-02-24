package com.example.giftbox.ui.used

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.giftbox.model.Document
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.detail.UsedStamp
import com.example.giftbox.ui.home.HomeGiftItem
import com.example.giftbox.ui.utils.formatString
import com.example.giftbox.ui.utils.getDday

@Composable
fun UsedScreen(onDetail: (String) -> Unit) {
    val usedViewModel = hiltViewModel<UsedViewModel>()

    Column {
        Text(
            modifier = Modifier.fillMaxWidth().padding(10.dp).padding(top = 8.dp),
            text = "사용완료 기프티콘",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        // gift item
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(items = usedViewModel.giftList.value) { index, gift ->
                UsedGiftItem(gift, formatString(gift.endDt), getDday(gift.endDt)) {
                    onDetail(gift.id)
                }
            }
        }
    }
}


/** 기프티콘 각각의 카드*/
@Composable
fun UsedGiftItem(gift: Gift, formattedEndDate: String, dDay: Pair<String, Boolean>, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(10.dp))
            .clickable {
                onClick()
            }
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            Box {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .size(200.dp),
                    model = gift.photo,
                    contentDescription = "add photo",
                    contentScale = ContentScale.Crop
                )
                if (gift.usedDt.isNotEmpty()) {
                    UsedStamp(gift.usedDt)
                }
            }
            Column(
                modifier = Modifier.padding(5.dp)
            ) {

                Text(
                    text = gift.brand,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = gift.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Box {
                    Text(
                        text = "~ $formattedEndDate",
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