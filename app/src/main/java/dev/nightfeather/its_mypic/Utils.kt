package dev.nightfeather.its_mypic

import android.Manifest
import android.app.Activity
import android.app.StatusBarManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.drawable.Icon
import android.icu.text.Transliterator
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.gson.stream.JsonReader
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File


object Utils {
    private val httpClient = OkHttpClient()

    object Overlay {
        fun startService(context: Context, isSingle: Boolean = false) {
            val intent = Intent(context, OverlayService::class.java).apply {
                this.action = if (isSingle) OverlayService.Action.SHOW_OVERLAY_SINGLE else OverlayService.Action.START_SERVICE
            }
            context.startForegroundService(intent)
        }
    }

    object Tile {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun requestAddTileService(context: Context) {
            val statusBarManager: StatusBarManager = context.getSystemService(StatusBarManager::class.java)
                ?: return

            statusBarManager.requestAddTileService(
                ComponentName(
                    context,
                    TileService::class.java
                ),
                "MyGo!!!!!",
                Icon.createWithResource(
                    context,
                    R.drawable.ic_launcher_foreground
                ),
                {},
                {}
            )
        }
    }

    object Preferences {
        private const val NAME = "myGoPrefs"

        const val OVERRIDE_DOWNLOAD_IMAGE = "overrideDownloadImage"

        fun getBoolean(context: Context, key: String, defValue: Boolean = false): Boolean {
            val sharedPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            return sharedPrefs.getBoolean(key, defValue)
        }

        fun getInt(context: Context, key: String, defValue: Int = 0): Int {
            val sharedPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            return sharedPrefs.getInt(key, defValue)
        }

        fun getString(context: Context, key: String, defValue: String = ""): String {
            val sharedPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            return sharedPrefs.getString(key, defValue).orEmpty()
        }

        fun getStringSet(context: Context, key: String, defValue: Set<String> = setOf()): Set<String> {
            val sharedPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            return sharedPrefs.getStringSet(key, defValue).orEmpty()
        }

        fun getLong(context: Context, key: String, defValue: Long = 0): Long {
            val sharedPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            return sharedPrefs.getLong(key, defValue)
        }

        fun getFloat(context: Context, key: String, defValue: Float = 0.0F): Float {
            val sharedPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            return sharedPrefs.getFloat(key, defValue)
        }

        fun putBoolean(context: Context, key: String, value: Boolean) {
            val sharedPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            sharedPrefs.putBoolean(key, value)
            sharedPrefs.apply()
        }

        fun putInt(context: Context, key: String, value: Int) {
            val sharedPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            sharedPrefs.putInt(key, value)
            sharedPrefs.apply()
        }

        fun putString(context: Context, key: String, value: String) {
            val sharedPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            sharedPrefs.putString(key, value)
            sharedPrefs.apply()
        }

        fun putStringSet(context: Context, key: String, value: Set<String>) {
            val sharedPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            sharedPrefs.putStringSet(key, value)
            sharedPrefs.apply()
        }

        fun putLong(context: Context, key: String, value: Long) {
            val sharedPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            sharedPrefs.putLong(key, value)
            sharedPrefs.apply()
        }

        fun putFloat(context: Context, key: String, value: Float) {
            val sharedPrefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            sharedPrefs.putFloat(key, value)
            sharedPrefs.apply()
        }
    }

    object Clipboard {
        fun copyImageFromUrl(context: Context, clipboardManager: ClipboardManager, imageData: ImageData) {
            val cacheDir = File(context.cacheDir, "clipboard")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val imageFile = File(cacheDir, "copiedImage.jpg")

            val request = Request.Builder()
                .url(imageData.sourceUrl)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.body != null) {
                    imageFile.writeBytes(response.body!!.bytes())
                }
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "dev.nightfeather.its_mypic.fileProvider",
                imageFile
            )

            val item = ClipData.newUri(
                context.contentResolver,
                imageData.text,
                contentUri
            )

            clipboardManager.setPrimaryClip(item)
        }
    }

    object File {
        private const val IMAGE_FOLDER = "ItsMyPic"

        fun downloadImageFromUrl(context: Context, imageData: ImageData) {
            val picturesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )

            val downloadDir = File(picturesDir, IMAGE_FOLDER)

            if (!downloadDir.exists()) downloadDir.mkdirs()

            val imageName = if (
                Preferences.getBoolean(
                    context,
                    Preferences.OVERRIDE_DOWNLOAD_IMAGE,
                    true
                )
            ) {
                "myGoImage"
            } else {
                "${imageData.episode}_${imageData.frameStart}"
            }

            val imageFile = File(downloadDir, "${imageName}.jpg")

            val request = Request.Builder()
                .url(imageData.sourceUrl)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.body != null) {
                    imageFile.writeBytes(response.body!!.bytes())
                }
            }

            MediaScannerConnection.scanFile(
                context,
                arrayOf(downloadDir.toString()),
                arrayOf("image/jpeg"),
                null
            )
        }
    }

    object Asset {
        fun loadImageData(assets: AssetManager): List<ImageData> {
            val jsonReader = JsonReader(assets.open("imgData/data.json").bufferedReader())
            val result: MutableList<ImageData> = mutableListOf()
            jsonReader.beginArray()
            while (jsonReader.hasNext()) {
                result.add(ImageData(reader = jsonReader))
            }
            jsonReader.endArray()
            jsonReader.close()
            return result.toList()
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

        fun checkStoragePermission(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return

            if (Environment.isExternalStorageManager()) return

            val intent = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            (context as Activity).startActivityForResult(intent, 1)
        }

        @Composable
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        @OptIn(ExperimentalPermissionsApi::class)
        fun CheckNotificationPermission() {
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
        private val transliterator: Transliterator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Transliterator.getInstance("Simplified-Traditional")
        } else {
            null
        }

        fun formatText(text: String): String {
            var formattedText = text

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                formattedText = transliterator?.transliterate(text).orEmpty()
            }

            return formattedText
                .lowercase()
                .replace("妳", "你")
                .replace("\n", "")
                .replace(",", "")
                .replace(" ", "")
        }

        fun containsApproximateSubstring(text: String, query: String): Boolean {
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
