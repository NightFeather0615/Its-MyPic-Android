package dev.nightfeather.its_mypic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import coil3.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OverlayService: Service(), OnTouchListener, OnClickListener {
    object Action {
        const val START_SERVICE = "startService"
        const val STOP_SERVICE = "stopService"
        const val SHOW_OVERLAY = "showOverlay"
        const val SHOW_OVERLAY_SINGLE = "showOverlaySingle"
    }

    private var isRunning = false

    private val channelId = "ItsMyPicOverlayService"

    private var windowManager: WindowManager? = null
    private var dialogView: ComposeView? = null
    private var lifecycleOwner: ComposeViewLifecycleOwner? = null
    private var imageData: List<ImageData> = listOf()
    private var imageSearchResult: List<ImageData> = listOf()

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun ImageBrowseDialog(isSingle: Boolean) {
        val localContext = LocalContext.current
        val localConfig = LocalConfiguration.current
        val screenWidth = localConfig.screenWidthDp

        val interactionSource = remember { MutableInteractionSource() }
        val fadeInAnimateState = remember {
            MutableTransitionState(false).apply {
                targetState = true
            }
        }
        var searchText by remember { mutableStateOf("是又怎樣") }
        val searchResultScrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()


        AnimatedVisibility(
            visibleState = fadeInAnimateState,
            enter = fadeIn()
        ) {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Color.hsl(0.0F, 0.0F, 0.0F, 0.5F)
                        )
                        .matchParentSize()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            if (isSingle) {
                                stopService()
                            } else {
                                disposeOverlay()
                            }
                        }
                )
                Column {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(
                                state = searchResultScrollState
                            )
                            .padding(
                                horizontal = (screenWidth * 0.04F).dp
                            )
                    ) {
                        for (searchData in imageSearchResult) {
                            Box(
                                modifier = Modifier
                                    .padding(
                                        horizontal = (screenWidth * 0.02F).dp
                                    )
                                    .width(280.dp)
                                    .height(210.dp)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clip(
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .background(Color(0xFF565e71))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = searchData.toUrl(),
                                        contentDescription = searchData.text,
                                        modifier = Modifier
                                            .width(280.dp)
                                            .height(157.5.dp)
                                    )
                                    Text(
                                        text = searchData.text,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .padding(
                                                vertical = 14.dp,
                                                horizontal = 14.dp
                                            )
                                    )
                                }
                            }
                        }
                    }
                    TextField(
                        value = searchText,
                        onValueChange = {
                            if (it.length > 0) {
                                imageSearchResult = Utils.StringSearch.fuzzySearch(it, imageData)
                            }
                            searchText = it
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 18.sp
                        ),
                        placeholder = {
                            Text(
                                text = "搜尋圖片...",
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .alpha(0.75F)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                "Search Icon",
                                tint = Color.White,
                                modifier = Modifier
                                    .offset(6.dp)
                                    .size(26.dp)
                            )
                        },
                        shape = RoundedCornerShape(60.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color(0xFF565e71),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(
                                horizontal = (screenWidth * 0.06F).dp,
                                vertical = (screenWidth * 0.06F).dp
                            )
                            .border(
                                width = 2.dp,
                                color = Color(0xFF3e4759),
                                shape = RoundedCornerShape(60.dp)
                            )
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(60.dp)
                            )
                            .fillMaxWidth()
                    )
                }
            }
        }

        BackHandler {
            if (isSingle) {
                stopService()
            } else {
                disposeOverlay()
            }
        }
    }

    private fun disposeOverlay() {
        windowManager?.removeViewImmediate(dialogView)
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
        dialogView = null
        windowManager = null
    }

    private fun stopService() {
        disposeOverlay()
        stopForeground(STOP_FOREGROUND_REMOVE)
        isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onCreate()

        if (
            intent?.action == Action.STOP_SERVICE
        ) {
            stopService()
        }

        if (
            intent?.action == Action.START_SERVICE ||
            intent?.action == Action.SHOW_OVERLAY_SINGLE &&
            !isRunning
        ) {
            val channel = NotificationChannel(
                channelId,
                "Overlay Service",
                NotificationManager.IMPORTANCE_HIGH
            )


            val contentPendingIntent: PendingIntent = PendingIntent.getForegroundService(
                this,
                0,
                Intent(this, OverlayService::class.java).apply {
                    this.action = Action.SHOW_OVERLAY
                },
                PendingIntent.FLAG_IMMUTABLE
            )
            val stopServicePendingIntent: PendingIntent = PendingIntent.getForegroundService(
                this,
                0,
                Intent(this, OverlayService::class.java).apply {
                    this.action = Action.STOP_SERVICE
                },
                PendingIntent.FLAG_IMMUTABLE
            )

            NotificationManagerCompat.from(this).createNotificationChannel(channel)

            val notification: Notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Running It's MyPic!!!!! Overlay")
                .setContentText("還在Go...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentPendingIntent)
                .addAction(
                    android.R.drawable.ic_notification_overlay,
                    "STOP SERVICE",
                    stopServicePendingIntent
                )
                .setAutoCancel(true)
                .build()

            startForeground(114514, notification)

            isRunning = true
        }

        if (
            intent?.action == Action.SHOW_OVERLAY ||
            intent?.action == Action.SHOW_OVERLAY_SINGLE &&
            dialogView == null && windowManager == null && lifecycleOwner == null && isRunning
        ) {
            imageData = Utils.Asset.loadImageData(application.assets)

            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager?

            dialogView = ComposeView(this).apply {
                setContent {
                    ImageBrowseDialog(
                        isSingle = intent.action == Action.SHOW_OVERLAY_SINGLE
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
            dialogView!!.setViewTreeOnBackPressedDispatcherOwner(lifecycleOwner!!)

            val coroutineContext = AndroidUiDispatcher.CurrentThread
            val runRecomposeScope = CoroutineScope(coroutineContext)
            val recomposer = Recomposer(coroutineContext)

            dialogView!!.compositionContext = recomposer
            runRecomposeScope.launch {
                recomposer.runRecomposeAndApplyChanges()
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.START or Gravity.TOP
            params.x = 0
            params.y = 0

            windowManager!!.addView(dialogView, params)
        }

        return START_NOT_STICKY
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        v.performClick()
        return false
    }

    override fun onClick(v: View?) {}

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
