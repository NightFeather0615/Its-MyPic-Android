package dev.nightfeather.its_mypic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import dev.nightfeather.its_mypic.ui.theme.ItsMyPicAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ItsMyPicAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Button(
                        modifier = Modifier.padding(innerPadding),
                        onClick = {
                            if (Utils.checkOverlayPermission(this)) {
                                Utils.startOverlayService(this);
                            }
                        }
                    ) {
                        Text("又在Go...")
                    }
                }
            }
        }
    }
}