package dev.nightfeather.its_mypic

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.net.Uri
import android.Manifest
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.app.NotificationManagerCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.gson.GsonBuilder

object Utils {
    object Overlay {
        fun startService(context: Context, isSingle: Boolean = false) {
            val intent = Intent(context, OverlayService::class.java).apply {
                this.action = if (isSingle) OverlayService.Action.SHOW_OVERLAY_SINGLE else OverlayService.Action.START_SERVICE
            }
            context.startForegroundService(intent)
        }
    }

    object Asset {
        fun loadImageData(assets: AssetManager): List<ImageData> {
            val jsonReader = assets.open("imgData/data.json").bufferedReader()
            val gson = GsonBuilder().create()
            val result = gson.fromJson(
                jsonReader.readText(),
                Array<ImageData>::class.java
            ).toList()
            jsonReader.close()
            return result
        }
    }

    object Permission {
        fun checkOverlayPermission(context: Context): Boolean {
            if (Settings.canDrawOverlays(context)) return true

            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            (context as Activity).startActivityForResult(intent, 1)

            return Settings.canDrawOverlays(context)
        }

        @Composable
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        @OptIn(ExperimentalPermissionsApi::class)
        fun CheckNotificationPermission(context: Context) {
            val notificationPermission = rememberPermissionState(
                permission = Manifest.permission.POST_NOTIFICATIONS
            )
            if (!notificationPermission.status.isGranted) {
                SideEffect {
                    notificationPermission.launchPermissionRequest()
                }
            }
        }
    }

    object StringSearch {
        fun fuzzySearch(query: String, list: List<ImageData>): List<ImageData> {
            return list.filter { item ->
                containsApproximateSubstring(item.text, query)
            }
        }

        private fun containsApproximateSubstring(text: String, query: String): Boolean {
            if (query.isEmpty()) return true
            if (text.length < query.length) return false

            for (i in 0..text.length - query.length) {
                val substring = text.substring(i, i + query.length)
                if (levenshteinDistance(query, substring)) {
                    return true
                }
            }
            return false
        }

        private fun levenshteinDistance(s1: String, s2: String): Boolean {
            val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
            for (i in 0..s1.length) {
                for (j in 0..s2.length) {
                    dp[i][j] = when {
                        i == 0 -> j
                        j == 0 -> i
                        else -> minOf(
                            dp[i - 1][j - 1] + if (s1[i - 1] == s2[j - 1]) 0 else 1,
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1
                        )
                    }
                }
            }
            return dp[s1.length][s2.length] == 0
        }
    }
}
