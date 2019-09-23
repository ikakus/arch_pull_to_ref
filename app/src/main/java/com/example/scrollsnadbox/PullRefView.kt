package com.example.scrollsnadbox

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat

class PullRefView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PullToRefBase(context, attrs, defStyleAttr) {

    val TAG = "test"
    private var currentTargetOffsetTop = 0
    private val container by lazy { mTarget!! }
    private lateinit var headerView: ArchView

    private val headerTag = "tag"

    init {
        createProgressView()
    }

    private fun createProgressView() {
        headerView = ArchView(context)
        headerView.tag = headerTag
        addView(headerView)
    }


    override fun onPull(distance: Float) {
        var dist = distance.toInt() - currentTargetOffsetTop

        move(dist)

        Log.d(TAG, "dist $dist")
        Log.d(TAG, "offset $currentTargetOffsetTop")

    }

    private fun move(offset: Int) {
        ViewCompat.offsetTopAndBottom(container, offset)
        currentTargetOffsetTop = container.top
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        super.onLayout(p0, p1, p2, p3, p4)
        headerView.layout(0, 0, width, 0)

        currentTargetOffsetTop = container.top
    }

    override fun ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child.tag != headerTag) {
                    mTarget = child
                    break
                }
            }
        }
    }

    override fun onRelease() {
        move(0 - currentTargetOffsetTop)
        Log.d(TAG, "On release offset $currentTargetOffsetTop")
    }

}

open class ArchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    override fun onDraw(canvas: Canvas) {
        val p = Paint()
        val h = top - bottom
        val offset = 150
        val rectF =
            RectF(left.toFloat() - offset, h.toFloat(), right.toFloat() + offset, bottom.toFloat())
        p.color = Color.BLACK
        canvas.drawArc(rectF, 0f, 180f, true, p)
    }
}