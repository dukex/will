package tech.benedicta.will.infrastructure

import android.content.Context

/**
 * How far the model has reviewed the feed.
 *
 * A plain timestamp rather than a column on `Task`: the question is "what has
 * arrived since last time", the answer is the same for every task, and a
 * migration to store it per row would buy nothing.
 */
class SharedPrefsModelReviewWatermark(context: Context) {

    private val prefs = context.getSharedPreferences("model_review", Context.MODE_PRIVATE)

    fun lastReviewed(): Long = prefs.getLong(KEY, 0L)

    fun markReviewed(at: Long) {
        prefs.edit().putLong(KEY, at).apply()
    }

    private companion object {
        const val KEY = "watermark"
    }
}
