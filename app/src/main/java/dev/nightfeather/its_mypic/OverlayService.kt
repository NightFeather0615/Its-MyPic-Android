package dev.nightfeather.its_mypic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.WindowManager
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class OverlayService: Service(), OnTouchListener, OnClickListener {
    private val channelId = "ItsMyPicOverlay"

    private var windowManager: WindowManager? = null
    private var dialogView: ComposeView? = null
    private var lifecycleOwner: ComposeViewLifecycleOwner? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onCreate()

        Log.d("asdasdasdasdasd", intent?.flags.toString())

        if (
            intent?.flags == Intent.FLAG_ACTIVITY_NEW_TASK ||
            intent?.flags == Intent.FLAG_ACTIVITY_SINGLE_TOP
        ) {
            val channel = NotificationChannel(
                channelId,
                "ItsMyPicOverlay",
                NotificationManager.IMPORTANCE_HIGH
            )

            val notificationIntent = Intent(this, OverlayService::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
            }
            val pendingIntent: PendingIntent = PendingIntent.getForegroundService(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
            )

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

            val notification: Notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Running It's MyPic!!!!! Overlay")
                .setContentText("還在Go...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            startForeground(114514, notification)
        }

        if (
            intent?.flags == Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT ||
            intent?.flags == Intent.FLAG_ACTIVITY_SINGLE_TOP &&
            dialogView == null && windowManager == null && lifecycleOwner == null
        ) {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager?

            dialogView = ComposeView(this).apply {
                setContent {
                    ImageBrowseDialog(
                        isSingle = intent.flags == Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                }
            }

            lifecycleOwner = ComposeViewLifecycleOwner().also {
                it.onCreate()
                it.attachToDecorView(dialogView)
            }

            dialogView!!.setViewTreeLifecycleOwner(lifecycleOwner)
            dialogView!!.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            dialogView!!.setViewTreeViewModelStoreOwner(lifecycleOwner)

            val coroutineContext = AndroidUiDispatcher.CurrentThread
            val runRecomposeScope = CoroutineScope(coroutineContext)
            val recomposer = Recomposer(coroutineContext)

            dialogView!!.compositionContext = recomposer
            runRecomposeScope.launch {
                recomposer.runRecomposeAndApplyChanges()
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.START or Gravity.TOP
            params.x = 0
            params.y = 0

            windowManager!!.addView(dialogView, params)
        }

        return START_NOT_STICKY
    }

    @Composable
    fun ImageBrowseDialog(isSingle: Boolean) {
        ElevatedButton (
            onClick = {
                windowManager?.removeViewImmediate(dialogView)
                lifecycleOwner?.onDestroy()
                lifecycleOwner = null
                dialogView = null
                windowManager = null
                if (isSingle) stopForeground(STOP_FOREGROUND_REMOVE)
            }
        ) {
            Text(
                text = "Click Me"
            )
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        v.performClick()
        Log.d(TAG, " ++++ On touch")
        return false
    }

    override fun onClick(v: View?) {
        Log.d(TAG, " ++++ On click")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dialogView != null) {
            windowManager!!.removeView(dialogView)
            dialogView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val TAG = "OverlayService"
    }
}
