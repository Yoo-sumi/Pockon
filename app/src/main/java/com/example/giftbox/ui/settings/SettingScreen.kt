package com.example.giftbox.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.giftbox.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(onUsedGift: () -> Unit) {
    var checkedAlarm by remember { mutableStateOf(true) }
    var checkedPwd by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                title = {
                    Text(text = stringResource(id = R.string.setting))
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                // 사용내역
                SettingItem("사용내역", true)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                        onUsedGift()
                    }
                ) {
                    SettingItem("사용완료 기프티콘")
                }

                // 설정
                SettingItem("설정", true)


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingItem("사용 임박 알림")

                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .fillMaxWidth()
                    ) {
                        Switch(
                            modifier = Modifier
                                .scale(0.5f)
                                .align(Alignment.CenterEnd),
                            checked = checkedAlarm,
                            onCheckedChange = {
                                checkedAlarm = it
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingItem("비밀번호 사용")

                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .fillMaxWidth()
                    ) {
                        Switch(
                            modifier = Modifier
                                .scale(0.5f)
                                .align(Alignment.CenterEnd),
                            checked = checkedPwd,
                            onCheckedChange = {
                                checkedPwd = it
                            }
                        )
                    }
                }

                // 사용자
                SettingItem("사용자", true)
                SettingItem("로그아웃")
                SettingItem("회원 탈퇴")
            }
        }
    }
}

@Composable
fun SettingItem(text: String, isTitle: Boolean = false) {
    val modifier = if (isTitle) {
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp)
    } else {
        Modifier
            .wrapContentSize()
            .padding(start = 15.dp, end = 15.dp, top = 10.dp, bottom = 10.dp)
    }

    Text(
        modifier = modifier,
        text = text,
    )
}

@Composable
@Preview
fun PreviewScreen() {
    SettingScreen {

    }
}