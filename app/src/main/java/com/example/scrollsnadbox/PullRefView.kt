package com.example.scrollsnadbox

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class PullRefView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PullToRefBase(context, attrs, defStyleAttr) {

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
        headerView.bottom = distance.toInt()
        if (distance >= 0) {
            container.top = distance.toInt()
        } else {
            container.top = 0
        }
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        super.onLayout(p0, p1, p2, p3, p4)
        headerView.layout(0, 0, width, 0)
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
        container.top = 0
        headerView.bottom = 0

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
        canvas.drawArc(rectF, 0f, 180f, false, p)
    }
}