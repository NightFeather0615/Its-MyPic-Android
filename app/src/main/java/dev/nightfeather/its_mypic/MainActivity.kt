package dev.nightfeather.its_mypic

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Utils.Permission.CheckNotificationPermission()
            }
        }
    }
}
