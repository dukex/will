package tech.benedicta.will.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import tech.benedicta.will.OverlayEvent
import tech.benedicta.will.WillApplication
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class ActiveTaskOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var backdropView: View? = null

    private var taskId = -1L
    private var taskTitle = ""
    private var taskText = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        taskId = intent?.getLongExtra(EXTRA_TASK_ID, -1L) ?: -1L
        taskTitle = intent?.getStringExtra(EXTRA_TITLE) ?: ""
        taskText = intent?.getStringExtra(EXTRA_TEXT) ?: ""
        if (taskId == -1L || taskTitle.isEmpty()) { stopSelf(); return START_NOT_STICKY }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showBubble()
        return START_STICKY
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    private fun containerDrawable(): GradientDrawable = GradientDrawable().apply {
        setColor(Color.parseColor("#161616"))
        setStroke(dp(1), Color.parseColor("#2A2A2A"))
        cornerRadius = dp(8).toFloat()
    }

    private fun bubbleParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.BOTTOM or Gravity.END
        x = dp(16); y = dp(80)
    }

    private fun showBubble() {
        removeBackdrop()
        removeOverlay()

        val params = bubbleParams()
        var initialX = 0; var initialY = 0
        var touchX = 0f; var touchY = 0f
        var moved = false

        val bubble = TextView(this).apply {
            text = taskTitle
            setTextColor(Color.parseColor("#F0F0F0"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setPadding(dp(16), dp(10), dp(16), dp(10))
            background = containerDrawable()
            maxWidth = dp(200)

            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.alpha = 0.65f
                        initialX = params.x; initialY = params.y
                        touchX = event.rawX; touchY = event.rawY
                        moved = false; true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - touchX).toInt()
                        val dy = (event.rawY - touchY).toInt()
                        if (dx * dx + dy * dy > dp(4) * dp(4)) moved = true
                        params.x = initialX - dx
                        params.y = initialY - dy
                        windowManager?.updateViewLayout(overlayView, params)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        view.alpha = 1f
                        if (!moved) showPanel()
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        view.alpha = 1f; true
                    }
                    else -> false
                }
            }
        }

        windowManager?.addView(bubble, params)
        overlayView = bubble
    }

    private fun showPanel() {
        removeOverlay()
        addBackdrop()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(16))
            background = containerDrawable()
        }

        container.addView(TextView(this).apply {
            text = taskTitle
            setTextColor(Color.parseColor("#F0F0F0"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(dp(220), LinearLayout.LayoutParams.WRAP_CONTENT)
        })

        if (taskText.isNotBlank()) {
            container.addView(TextView(this).apply {
                text = taskText
                setTextColor(Color.parseColor("#888888"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                setPadding(0, dp(4), 0, 0)
                layoutParams = LinearLayout.LayoutParams(dp(220), LinearLayout.LayoutParams.WRAP_CONTENT)
            })
        }

        container.addView(View(this).apply {
            setBackgroundColor(Color.parseColor("#2A2A2A"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
            ).also { it.topMargin = dp(12); it.bottomMargin = dp(4) }
        })

        container.addView(actionRow("mark done", "#F0F0F0", ACTION_DONE))
        container.addView(actionRow("later", "#888888", ACTION_LATER))
        container.addView(actionRow("delete", "#E05252", ACTION_DELETE))

        val panelParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = dp(16); y = dp(80)
        }

        windowManager?.addView(container, panelParams)
        overlayView = container
    }

    private fun actionRow(label: String, colorHex: String, action: String) = TextView(this).apply {
        text = label
        setTextColor(Color.parseColor(colorHex))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        setPadding(0, dp(10), 0, dp(6))
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { view.alpha = 0.65f; false }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { view.alpha = 1f; false }
                else -> false
            }
        }
        setOnClickListener {
            (application as WillApplication).emitOverlayEvent(OverlayEvent(action, taskId))
            returnToLauncher()
            stopSelf()
        }
    }

    private fun returnToLauncher() {
        packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }?.also { startActivity(it) }
    }

    private fun addBackdrop() {
        val backdrop = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { showBubble() }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        windowManager?.addView(backdrop, params)
        backdropView = backdrop
    }

    private fun removeBackdrop() {
        backdropView?.let { runCatching { windowManager?.removeView(it) } }
        backdropView = null
    }

    private fun removeOverlay() {
        overlayView?.let { runCatching { windowManager?.removeView(it) } }
        overlayView = null
    }

    override fun onDestroy() {
        removeOverlay()
        removeBackdrop()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TEXT = "extra_text"
        const val ACTION_DONE = "tech.benedicta.will.TASK_DONE"
        const val ACTION_LATER = "tech.benedicta.will.TASK_LATER"
        const val ACTION_DELETE = "tech.benedicta.will.TASK_DELETE"

        fun show(context: Context, taskId: Long, title: String, text: String) {
            context.startService(
                Intent(context, ActiveTaskOverlayService::class.java)
                    .putExtra(EXTRA_TASK_ID, taskId)
                    .putExtra(EXTRA_TITLE, title)
                    .putExtra(EXTRA_TEXT, text)
            )
        }

        fun hide(context: Context) {
            context.stopService(Intent(context, ActiveTaskOverlayService::class.java))
        }
    }
}
