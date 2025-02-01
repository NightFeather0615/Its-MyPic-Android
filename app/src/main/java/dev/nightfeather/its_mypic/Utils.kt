package dev.nightfeather.its_mypic

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object Utils {
    fun checkOverlayPermission(context: Context): Boolean {
        if (Settings.canDrawOverlays(context)) return true

        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        (context as Activity).startActivityForResult(intent, 1)

        return Settings.canDrawOverlays(context)
    }

    fun startOverlayService(context: Context, isSingle: Boolean = false) {
        val intent = Intent(context, OverlayService::class.java).apply {
            flags = if (isSingle) Intent.FLAG_ACTIVITY_SINGLE_TOP else Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startForegroundService(intent)
    }
}