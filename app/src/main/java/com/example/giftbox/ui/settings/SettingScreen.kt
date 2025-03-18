package com.example.giftbox.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.giftbox.R
import com.example.giftbox.ui.list.ConfirmDialog
import kotlinx.coroutines.launch

@Composable
fun SettingScreen(onUsedGift: () -> Unit, movePinScreen: () -> Unit, moveLogInScreen: () -> Unit, isLoading: (Boolean) -> Unit) {
    val settingViewModel = hiltViewModel<SettingViewModel>()

    var showLogoutDlg by remember { mutableStateOf(false) }
    var showRemoveDlg by remember { mutableStateOf(false) }
    var checkedAlarm by rememberSaveable { mutableStateOf(settingViewModel.getIsNotiEndDt()) }
    var checkedPwd by rememberSaveable { mutableStateOf(settingViewModel.getIsAuthPin()) }

    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column {
            SettingScreenTopBar()
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
                        text = stringResource(id = R.string.txt_usage_history),
                        isTitle = true
                    )

                    SettingItem(
                        text = stringResource(id = R.string.txt_usage_history),
                        onClick = {
                            onUsedGift()
                        }
                    )

                    // 설정
                    SettingItem(
                        text = stringResource(id = R.string.setting),
                        isTitle = true
                    )

                    SettingItem(
                        text = stringResource(id = R.string.txt_noti_of_imminent_use),
                        isSwitch = true,
                        checked = checkedAlarm,
                        onCheck = {
                            checkedAlarm = !checkedAlarm
                            settingViewModel.onOffNotiEndDt(checkedAlarm)
                        }
                    )

                    SettingItem(
                        text = stringResource(id = R.string.txt_use_pwd),
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
                        text = stringResource(id = R.string.txt_user),
                        isTitle = true
                    )

                    SettingItem(
                        text = stringResource(id = R.string.txt_logout),
                        onClick = {
                            showLogoutDlg = true
                        }
                    )

                    if (!settingViewModel.getIsGuestMode()) {
                        SettingItem(
                            text = stringResource(id = R.string.txt_remove_account),
                            onClick = {
                                showRemoveDlg = true
                            }
                        )
                    }
                }
            }
        }

        if (showLogoutDlg) {
            ConfirmDialog(
                text = if (settingViewModel.getIsGuestMode()) {
                    R.string.dlg_msg_logout_in_guest
                } else {
                    R.string.dlg_msg_logout
                },
                onConfirm = {
                    isLoading(true)
                    showLogoutDlg = false
                    settingViewModel.logout()
                    moveLogInScreen()
                    isLoading(false)
                },
                onDismiss = {
                    showLogoutDlg = false
                }
            )
        }

        if (showRemoveDlg) {
            ConfirmDialog(
                text = R.string.dlg_msg_remove_account,
                onConfirm = {
                    showRemoveDlg = false
                    isLoading(true)
                    settingViewModel.removeAccount { result ->
                        isLoading(false)
                        if (result) { // 로그인 화면으로 이동
                            moveLogInScreen()
                        } else { // "회원탈퇴에 실패했습니다."
                            scope.launch {
                                snackbarHostState.showSnackbar(message = context.getString(R.string.msg_remove_account_fail))
                            }
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
}

@Composable
fun SettingScreenTopBar() {
    // topbar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(10.dp)
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = R.string.setting),
            fontSize = 18.sp,
        )
    }
}