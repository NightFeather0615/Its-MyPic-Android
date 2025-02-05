package dev.nightfeather.its_mypic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil3.compose.AsyncImage
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

    private var isServiceRunning = false
    private var isOverlayShown = false

    private val channelId = "ItsMyPicOverlayService"

    private var windowManager: WindowManager? = null
    private var dialogView: ComposeView? = null
    private var composeViewOwner: ComposeViewOwner? = null
    private var imageData: List<ImageData> = listOf()

    @Composable
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    private fun ImageBrowseDialog(isSingle: Boolean) {
        val localContext = LocalContext.current
        val localClipboardManager = LocalClipboardManager.current
        val localConfig = LocalConfiguration.current

        val screenWidth = localConfig.screenWidthDp

        val searchViewModel = SearchViewModel(imageData)

        val coroutineScope = rememberCoroutineScope()

        val interactionSource = remember { MutableInteractionSource() }
        val fadeInAnimateState = remember {
            MutableTransitionState(false).apply {
                targetState = true
            }
        }

        val searchResultScrollState = rememberLazyListState()
        val searchQueryText by searchViewModel.queryText.collectAsState()
        val imageSearchResult by searchViewModel.searchResult.collectAsState()

        val downloadToast = Toast.makeText(localContext, "下載圖片...", Toast.LENGTH_SHORT)
        val saveToast = Toast.makeText(localContext, "保存成功", Toast.LENGTH_SHORT)
        val copyToast = Toast.makeText(localContext, "複製成功", Toast.LENGTH_SHORT)

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
                                removeOverlay()
                            }
                        }
                )
                Column {
                    LazyRow(
                        state = searchResultScrollState,
                        contentPadding = PaddingValues(
                            horizontal = (screenWidth * 0.04F).dp
                        )
                    ) {
                        itemsIndexed(imageSearchResult) { _, searchData ->
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
                                    .combinedClickable(
                                        role = Role.Button,
                                        onClickLabel = "Copy Image to Clipboard",
                                        onClick = {
                                            downloadToast.show()
                                            coroutineScope.launch {
                                                withContext(Dispatchers.IO) {
                                                    Utils.Clipboard.copyImageFromUrl(
                                                        context = localContext,
                                                        clipboardManager = localClipboardManager.nativeClipboard,
                                                        imageData = searchData
                                                    )
                                                }
                                                downloadToast.cancel()
                                                copyToast.show()
                                                if (isSingle) {
                                                    stopService()
                                                } else {
                                                    removeOverlay()
                                                }
                                            }
                                        },
                                        onLongClickLabel = "Save Image",
                                        onLongClick = {
                                            downloadToast.show()
                                            coroutineScope.launch {
                                                withContext(Dispatchers.IO) {
                                                    Utils.File.downloadImageFromUrl(
                                                        context = localContext,
                                                        imageData = searchData
                                                    )
                                                }
                                                downloadToast.cancel()
                                                saveToast.show()
                                                if (isSingle) {
                                                    stopService()
                                                } else {
                                                    removeOverlay()
                                                }
                                            }
                                        }
                                    )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = searchData.sourceUrl,
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
                        value = searchQueryText,
                        onValueChange = searchViewModel::onQueryTextChanged,
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
                            .height(58.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }

    private fun attachOverlay() {
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

        isOverlayShown = true
    }

    private fun removeOverlay() {
        if (dialogView != null && isOverlayShown) {
            windowManager?.removeViewImmediate(dialogView)
        }
        isOverlayShown = false
    }

    private fun disposeOverlayView() {
        composeViewOwner?.onDestroy()
        composeViewOwner = null
        dialogView = null
    }

    private fun initService(intent: Intent) {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager?

        imageData = Utils.Asset.loadImageData(application.assets)

        dialogView = ComposeView(this).apply {
            setContent {
                ImageBrowseDialog(
                    isSingle = intent.action == Action.SHOW_OVERLAY_SINGLE
                )
            }
        }

        composeViewOwner = ComposeViewOwner().also {
            it.onCreate()
            it.attachToDecorView(dialogView)
        }

        val channel = NotificationChannel(
            channelId,
            "快速存取遮罩服務",
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

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("快速存取 MyGO 圖")
            .setContentText("還在 Go...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                android.R.drawable.ic_media_pause,
                "終止服務",
                stopServicePendingIntent
            )
            .setAutoCancel(true)

        if (intent.action != Action.SHOW_OVERLAY_SINGLE) {
            notification.setContentIntent(contentPendingIntent)
        }

        startForeground(114514, notification.build())

        isServiceRunning = true
    }

    private fun stopService() {
        removeOverlay()
        disposeOverlayView()
        stopForeground(STOP_FOREGROUND_REMOVE)
        windowManager = null
        isServiceRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onCreate()

        if (intent == null) throw Exception("Can't start OverlayService with null intent.")

        if (
            intent.action == Action.STOP_SERVICE
        ) {
            stopService()
        }

        if (
            (intent.action == Action.START_SERVICE || intent.action == Action.SHOW_OVERLAY_SINGLE) &&
            !isServiceRunning
        ) {
            initService(intent)
        }

        if (
            (intent.action == Action.SHOW_OVERLAY || intent.action == Action.SHOW_OVERLAY_SINGLE) &&
            !isOverlayShown && isServiceRunning
        ) {
            attachOverlay()
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
        stopService()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
