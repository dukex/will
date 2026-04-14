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
import android.view.WindowManager
import android.widget.TextView

class ActiveTaskOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: TextView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title = intent?.getStringExtra(EXTRA_TITLE)
        if (title == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        showBubble(title)
        return START_STICKY
    }

    private fun showBubble(title: String) {
        removeOverlay()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val dm = resources.displayMetrics
        fun dp(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), dm).toInt()

        val pill = GradientDrawable().apply {
            setColor(Color.parseColor("#E6000000"))
            cornerRadius = dp(24).toFloat()
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = dp(16)
            y = dp(80)
        }

        var initialX = 0; var initialY = 0
        var touchX = 0f; var touchY = 0f

        val bubble = TextView(this).apply {
            text = title
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setPadding(dp(16), dp(10), dp(16), dp(10))
            background = pill
            maxWidth = dp(200)

            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x; initialY = params.y
                        touchX = event.rawX; touchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX - (event.rawX - touchX).toInt()
                        params.y = initialY - (event.rawY - touchY).toInt()
                        windowManager?.updateViewLayout(this, params)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val dx = event.rawX - touchX
                        val dy = event.rawY - touchY
                        if (dx * dx + dy * dy < dp(8) * dp(8)) performClick()
                        true
                    }
                    else -> false
                }
            }

            setOnClickListener { stopSelf() }
        }

        windowManager?.addView(bubble, params)
        overlayView = bubble
    }

    private fun removeOverlay() {
        overlayView?.let { runCatching { windowManager?.removeView(it) } }
        overlayView = null
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"

        fun show(context: Context, title: String) {
            context.startService(
                Intent(context, ActiveTaskOverlayService::class.java)
                    .putExtra(EXTRA_TITLE, title)
            )
        }

        fun hide(context: Context) {
            context.stopService(Intent(context, ActiveTaskOverlayService::class.java))
        }
    }
}
