package dev.nightfeather.its_mypic

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

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

    fun checkNotificationPermission(context: Context): Boolean {
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) return true

        val intent = Intent(
            Settings.ACTION_APP_NOTIFICATION_SETTINGS,
            Uri.parse("package:${context.packageName}")
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            (context as Activity).requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        } else {
            (context as Activity).startActivityForResult(intent, 1)
        }

        return Settings.canDrawOverlays(context)
    }

    fun startOverlayService(context: Context, isSingle: Boolean = false) {
        val intent = Intent(context, OverlayService::class.java).apply {
            this.action = if (isSingle) OverlayService.Action.SHOW_OVERLAY_SINGLE else OverlayService.Action.START_SERVICE
        }
        context.startForegroundService(intent)
    }
}