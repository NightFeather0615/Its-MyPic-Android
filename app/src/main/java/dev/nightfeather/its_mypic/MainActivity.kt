package dev.nightfeather.its_mypic

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.nightfeather.its_mypic.ui.theme.ItsMyPicAndroidTheme

class MainActivity : ComponentActivity() {
    private val startImageData = ImageData(
        text = "來,開始溝通吧",
        episode = "4",
        frameStart = 11368,
        frameEnd = 11408,
        segmentId = 1198
    )

    @Composable
    fun InfoDialog(state: MutableState<Boolean>, title: String, text: String) {
        AlertDialog(
            onDismissRequest = {
                state.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.value = false
                    }
                ) {
                    Text(
                        text = "關閉"
                    )
                }
            },
            icon = {
                Icon(
                    Icons.Default.Info,
                    "Settings Info"
                )
            },
            text = {
                Text(
                    text = text
                )
            },
            title = {
                Text(
                    text = title
                )
            }
        )
    }

    @Composable
    fun SettingsBlock() {
        val localContext = LocalContext.current
        val localConfig = LocalConfiguration.current

        val screenWidth = localConfig.screenWidthDp

        val infoDialogState = remember {
            mutableStateOf(false)
        }
        val infoDialogTitle = remember {
            mutableStateOf("")
        }
        val infoDialogText = remember {
            mutableStateOf("")
        }

        var overrideDownloadImageDropdownValue by remember {
            mutableStateOf(
                Utils.Preferences.getBoolean(
                    localContext,
                    Utils.Preferences.OVERRIDE_DOWNLOAD_IMAGE,
                    true
                )
            )
        }

        if (infoDialogState.value) InfoDialog(
            infoDialogState,
            infoDialogTitle.value,
            infoDialogText.value
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(
                    horizontal = (screenWidth * 0.16F).dp
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        end = 26.dp
                    )
            ) {
                Text(
                    text = "覆寫下載的圖片",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(
                            bottom = 2.dp
                        )
                )
                IconButton(
                    onClick = {
                        infoDialogTitle.value = "覆寫下載的圖片"
                        infoDialogText.value = "開啟 - 將圖片寫入同個檔案\n關閉 - 將圖片分開儲存"
                        infoDialogState.value = true
                    },
                    modifier = Modifier
                        .scale(0.8F)
                        .alpha(0.8F)
                        .offset((-12).dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        "Settings Info"
                    )
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Switch(
                    checked = overrideDownloadImageDropdownValue,
                    onCheckedChange = {
                        overrideDownloadImageDropdownValue = it
                        Utils.Preferences.putBoolean(
                            localContext,
                            Utils.Preferences.OVERRIDE_DOWNLOAD_IMAGE,
                            it
                        )
                    }
                )
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val localContext = LocalContext.current
            val localConfig = LocalConfiguration.current

            val screenWidth = localConfig.screenWidthDp

            ItsMyPicAndroidTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        AsyncImage(
                            model = startImageData.toUrl(),
                            contentDescription = startImageData.text,
                            modifier = Modifier
                                .padding(
                                    horizontal = (screenWidth * 0.06F).dp
                                )
                                .clip(
                                    shape = RoundedCornerShape(16.dp)
                                )
                        )
                        Spacer(
                            modifier = Modifier
                                .height(16.dp)
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Button(
                                onClick = {
                                    Utils.Tile.requestAddTileService(localContext)
                                }
                            ) {
                                Text(
                                    text = "新增快速設定方塊",
                                    fontSize = 22.sp,
                                    modifier = Modifier
                                        .padding(6.dp)
                                )
                            }
                        }
                        Spacer(
                            modifier = Modifier
                                .height(16.dp)
                        )
                        Button(
                            onClick = {
                                if (Utils.Permission.checkOverlayPermission(localContext)) {
                                    Utils.Overlay.startService(localContext)
                                }
                            }
                        ) {
                            Text(
                                text = "開始溝通",
                                fontSize = 22.sp,
                                modifier = Modifier
                                    .padding(6.dp)
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .height(28.dp)
                        )
                        SettingsBlock()
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Utils.Permission.CheckNotificationPermission()
            }
        }
    }
}
