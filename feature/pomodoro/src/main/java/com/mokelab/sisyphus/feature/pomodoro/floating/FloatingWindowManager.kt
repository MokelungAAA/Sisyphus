package com.mokelab.sisyphus.feature.pomodoro.floating

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.*
import kotlin.math.abs

/**
 * 悬浮窗管理器
 * 管理番茄钟悬浮窗的显示、隐藏、手势和边缘吸附
 */
class FloatingWindowManager(private val context: Context) :
    LifecycleOwner, SavedStateRegistryOwner {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var floatingView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    // Lifecycle support for ComposeView
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // State exposed to Compose
    private val _progress = mutableFloatStateOf(0f)
    private val _remainingMinutes = mutableIntStateOf(0)
    private val _isPaused = mutableStateOf(false)
    private val _isRunning = mutableStateOf(false)
    private val _subjectName = mutableStateOf("")

    // Callbacks
    var onSingleClick: (() -> Unit)? = null
    var onDoubleClick: (() -> Unit)? = null
    var onLongPress: (() -> Unit)? = null
    var onDismiss: (() -> Unit)? = null

    private var lastTapTime = 0L
    private var longPressJob: Job? = null
    private var isDragging = false
    private var downX = 0f
    private var downY = 0f

    fun show() {
        if (floatingView != null) return

        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        floatingView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(this@FloatingWindowManager)
            setViewTreeSavedStateRegistryOwner(this@FloatingWindowManager)
            setContent {
                FloatingPomodoroContent(
                    progress = _progress.floatValue,
                    remainingMinutes = _remainingMinutes.intValue,
                    isPaused = _isPaused.value,
                    isRunning = _isRunning.value,
                    subjectName = _subjectName.value
                )
            }
        }

        setupGestures()
        windowManager.addView(floatingView, layoutParams)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun hide() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        floatingView?.let { windowManager.removeView(it) }
        floatingView = null
        scope.cancel()
    }

    fun updateProgress(progress: Float, remainingMinutes: Int, isPaused: Boolean, isRunning: Boolean, subjectName: String) {
        _progress.floatValue = progress
        _remainingMinutes.intValue = remainingMinutes
        _isPaused.value = isPaused
        _isRunning.value = isRunning
        _subjectName.value = subjectName
    }

    private fun setupGestures() {
        floatingView?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    isDragging = false

                    // Long press detection
                    longPressJob?.cancel()
                    longPressJob = scope.launch {
                        delay(800)
                        onLongPress?.invoke()
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY

                    if (!isDragging && (abs(dx) > 10 || abs(dy) > 10)) {
                        isDragging = true
                        longPressJob?.cancel()
                    }

                    if (isDragging) {
                        layoutParams?.x = (event.rawX - view.width / 2).toInt()
                        layoutParams?.y = (event.rawY - view.height / 2).toInt()
                        try {
                            windowManager.updateViewLayout(floatingView, layoutParams)
                        } catch (_: Exception) {}
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    longPressJob?.cancel()

                    if (!isDragging) {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 300) {
                            // Double tap
                            onDoubleClick?.invoke()
                            lastTapTime = 0L
                        } else {
                            // Single tap (delayed to detect double tap)
                            lastTapTime = now
                            scope.launch {
                                delay(300)
                                if (lastTapTime == now) {
                                    onSingleClick?.invoke()
                                }
                            }
                        }
                    } else {
                        // Snap to nearest edge after drag
                        edgeSnap()
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun edgeSnap() {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val currentX = layoutParams?.x ?: return
        val targetX = if (currentX > screenWidth / 2) {
            screenWidth - (floatingView?.width ?: 100)
        } else {
            0
        }

        scope.launch {
            val startX = currentX
            val steps = 10
            for (i in 1..steps) {
                val fraction = i.toFloat() / steps
                // Ease-out curve
                val easedFraction = 1f - (1f - fraction) * (1f - fraction)
                layoutParams?.x = startX + ((targetX - startX) * easedFraction).toInt()
                try {
                    windowManager.updateViewLayout(floatingView, layoutParams)
                } catch (_: Exception) {}
                delay(16) // ~60fps
            }
        }
    }

    companion object {
        private const val TAG = "FloatingWindowManager"
    }
}
