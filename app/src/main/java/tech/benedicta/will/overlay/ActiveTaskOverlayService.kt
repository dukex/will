package tech.benedicta.will.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
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

    private fun dp(v: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()
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

        val bg = GradientDrawable().apply {
            setColor(Color.parseColor("#E6000000"))
            cornerRadius = dp(24).toFloat()
        }

        val bubble = TextView(this).apply {
            setText(taskTitle)
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setPadding(dp(16), dp(10), dp(16), dp(10))
            background = bg
            maxWidth = dp(200)

            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
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
                        if (!moved) showPanel()
                        true
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

        val panelBg = GradientDrawable().apply {
            setColor(Color.parseColor("#F0000000"))
            cornerRadius = dp(16).toFloat()
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(16))
            background = panelBg
        }

        container.addView(TextView(this).apply {
            setText(taskTitle)
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(dp(220), LinearLayout.LayoutParams.WRAP_CONTENT)
        })

        if (taskText.isNotBlank()) {
            container.addView(TextView(this).apply {
                setText(taskText)
                setTextColor(Color.parseColor("#AAFFFFFF"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                setPadding(0, dp(4), 0, 0)
                layoutParams = LinearLayout.LayoutParams(dp(220), LinearLayout.LayoutParams.WRAP_CONTENT)
            })
        }

        container.addView(View(this).apply {
            setBackgroundColor(Color.parseColor("#33FFFFFF"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
            ).also { it.topMargin = dp(12); it.bottomMargin = dp(4) }
        })

        fun actionRow(label: String, color: String, action: String) = TextView(this).apply {
            setText(label)
            setTextColor(Color.parseColor(color))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(0, dp(8), 0, dp(4))
            setOnClickListener {
                sendBroadcast(Intent(action).putExtra(EXTRA_TASK_ID, taskId))
                stopSelf()
            }
        }

        container.addView(actionRow("Mark as done", "#4CAF50", ACTION_DONE))
        container.addView(actionRow("Remind me later", "#AAFFFFFF", ACTION_LATER))
        container.addView(actionRow("Delete", "#F44336", ACTION_DELETE))

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
