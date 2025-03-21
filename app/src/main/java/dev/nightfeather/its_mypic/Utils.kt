package dev.nightfeather.its_mypic

import android.Manifest
import android.app.Activity
import android.app.StatusBarManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.database.Cursor
import android.graphics.drawable.Icon
import android.icu.text.Transliterator
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
        fun copyPlainText(clipboardManager: ClipboardManager, text: String) {
            val item = ClipData.newPlainText(
                text,
                text
            )

            clipboardManager.setPrimaryClip(item)
        }

        fun copyImageFromUrl(context: Context, clipboardManager: ClipboardManager, imageData: ImageData) {
            val cacheDir = File(context.cacheDir, "clipboard")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val imageFile = File(cacheDir, "copiedImage.jpg")

            val request = Request.Builder()
                .url(imageData.sourceUrl + ".jpg")
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
        fun downloadImageFromUrl(context: Context, imageData: ImageData) {
            val imageName = if (
                Preferences.getBoolean(
                    context,
                    Preferences.OVERRIDE_DOWNLOAD_IMAGE,
                    true
                )
            ) {
                "myGoImage.jpg"
            } else {
                "${imageData.episode}_${imageData.framePrefer}.jpg"
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.DownloadColumns.DISPLAY_NAME, imageName)
                put(MediaStore.DownloadColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.DownloadColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ItsMyPic")
            }

            val extVolumeUri: Uri = MediaStore.Files.getContentUri("external")

            val cursor: Cursor? = context.contentResolver.query(
                extVolumeUri,
                null,
                MediaStore.DownloadColumns.DISPLAY_NAME + " = ? AND " + MediaStore.DownloadColumns.MIME_TYPE + " = ?",
                arrayOf(imageName, "image/jpeg"),
                null
            )

            var fileUri: Uri? = null

            if (cursor != null && cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val nameIndex = cursor.getColumnIndex(MediaStore.DownloadColumns.DISPLAY_NAME)
                    if (nameIndex > -1) {
                        val displayName = cursor.getString(nameIndex)
                        if (displayName == imageName) {
                            val idIndex = cursor.getColumnIndex(MediaStore.DownloadColumns._ID)
                            if (idIndex > -1) {
                                val id = cursor.getLong(idIndex)
                                fileUri = ContentUris.withAppendedId(extVolumeUri, id)
                            }
                        }
                    }
                }

                cursor.close()
            } else {
                fileUri = context.contentResolver.insert(extVolumeUri, contentValues)
            }

            if (fileUri != null) {
                val os = context.contentResolver.openOutputStream(fileUri, "wt")

                if (os != null) {
                    val request = Request.Builder()
                        .url(imageData.sourceUrl + ".jpg")
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        if (response.body != null) {
                            os.write(response.body!!.bytes())
                            os.close()
                        }
                    }
                }

            }
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
        private val transliterator = Transliterator.getInstance("Simplified-Traditional")

        fun formatText(text: String): String {
            val formattedText = transliterator?.transliterate(text).orEmpty()

            return formattedText
                .lowercase()
                .replace("妳", "你")
                .replace("她", "他")
                .replace("甚麼", "什麼")
                .replace("\n", "")
                .replace(",", "")
                .replace(" ", "")
        }

        fun calcDistance(text: String, query: String): Pair<Int, Int> {
            if (query.isEmpty()) return Pair(0, 0)
            val queryList = query.asIterable().toMutableList()
            var dist = 0
            var match = 0
            for (textChar in text.asIterable().withIndex()) {
                if (queryList.isEmpty()) {
                    val remainDist = text.length - textChar.index
                    return Pair(match, dist + remainDist)
                }
                if (queryList.remove(textChar.value)) {
                    match += 1
                } else {
                    dist += 1
                }
            }

            return Pair(match, dist)
        }
    }
}
