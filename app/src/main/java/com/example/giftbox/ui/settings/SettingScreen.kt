package com.example.giftbox.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.giftbox.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(onUsedGift: () -> Unit, movePinScreen: () -> Unit, moveLogInScreen: () -> Unit) {
    val settingViewModel = hiltViewModel<SettingViewModel>()

    var showLogoutDlg by remember { mutableStateOf(false) }
    var showRemoveDlg by remember { mutableStateOf(false) }
    var checkedAlarm by rememberSaveable { mutableStateOf(settingViewModel.getIsNotiEndDt()) }
    var checkedPwd by rememberSaveable { mutableStateOf(settingViewModel.getIsAuthPin()) }

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
                SettingItem(
                    text = "사용내역",
                    isTitle = true
                )

                SettingItem(
                    text = "사용완료 기프티콘",
                    onClick = {
                        onUsedGift()
                    }
                )

                // 설정
                SettingItem(
                    text = "설정",
                    isTitle = true
                )

                SettingItem(
                    text = "사용 임박 알림",
                    isSwitch = true,
                    checked = checkedAlarm,
                    onCheck = {
                        checkedAlarm = !checkedAlarm
                        settingViewModel.onOffNotiEndDt(checkedAlarm)
                    }
                )

                SettingItem(
                    text = "비밀번호 사용",
                    isSwitch = true,
                    checked = checkedPwd,
                    onCheck = {
                        checkedPwd = !checkedPwd
                        if (checkedPwd) {
                            movePinScreen()
                        } else {
                            settingViewModel.offAuthPin()
                        }
                    },
                )

                // 사용자
                SettingItem(
                    text = "사용자",
                    isTitle = true
                )

                SettingItem(
                    text = "로그아웃",
                    onClick = {
                        showLogoutDlg = true
                    }
                )

                SettingItem(
                    text = "회원 탈퇴",
                    onClick = {
                        showRemoveDlg = true
                    }
                )
            }
        }

        if (showLogoutDlg) {
            LogoutDialog(
                onConfirm = {
                    showLogoutDlg = false
                    settingViewModel.logout()
                    moveLogInScreen()
                },
                onDismiss = {
                    showLogoutDlg = false
                }
            )
        }

        if (showRemoveDlg) {
            RemoveAccountDialog(
                onConfirm = {
                    showRemoveDlg = false
                    settingViewModel.removeAccount { result ->
                        if (result) {
                            moveLogInScreen()
                        }
                        else { // 네트워크가 불안정합니다. 인터넷 연결을 확인해주세요.
                        }
                    }
                },
                onDismiss = {
                    showRemoveDlg = false
                }
            )
        }
    }
}

@Composable
fun SettingItem(text: String, isTitle: Boolean = false, isSwitch: Boolean = false, checked: Boolean = false, onCheck: () -> Unit = {}, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .drawBehind {
                val strokeWidth = 1f
                val y = size.height - strokeWidth / 2
                drawLine(
                    Color.LightGray,
                    Offset(0f, y),
                    Offset(size.width, y),
                    strokeWidth
                )
            }
    ) {
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

        if (isSwitch) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Switch(
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .scale(0.6f)
                        .align(Alignment.CenterEnd),
                    checked = checked,
                    onCheckedChange = {
                        onCheck()
                    }
                )
            }
        }
    }
}

// 기프티콘 제거 묻는 다이얼로그
@Composable
fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        text = {
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.dlg_msg_logout),
                fontSize = 18.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm() }
            ) {
                Text(text = stringResource(id = R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(text = stringResource(id = R.string.btn_cancel))
            }
        },
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun RemoveAccountDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        text = {
            Text(
                textAlign = TextAlign.Center,
                text = stringResource(id = R.string.dlg_msg_remove_account),
                fontSize = 18.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm() }
            ) {
                Text(text = stringResource(id = R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(text = stringResource(id = R.string.btn_cancel))
            }
        },
        shape = RoundedCornerShape(10.dp)
    )
}