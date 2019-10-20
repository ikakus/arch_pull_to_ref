package com.example.scrollsnadbox

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.ViewCompat


class PullRefView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PullToRefBase(context, attrs, defStyleAttr) {

    val TAG = "test"
    private var currentTargetOffsetTop = 0
    private val container by lazy { target!! }
    private lateinit var headerView: ArchView
    var h: Int = 0

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

        Log.d(TAG, "distance $distance")

        val hcp = distance / (h / 100)
        Log.d(TAG, "percent:  $hcp")

        if (hcp <= 50) {
            val d = (distance / 100) * (100 - hcp)
            Log.d(TAG, "d $d")

            anim(d.toInt())
            move(d.toInt() - currentTargetOffsetTop)
        }

        Log.d(TAG, "_________________________")

    }

    private fun move(offset: Int) {
        ViewCompat.offsetTopAndBottom(container, offset)
        currentTargetOffsetTop = container.top
    }

    private fun anim(offset: Int) {
        headerView.bottom = offset
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        super.onLayout(p0, p1, p2, p3, p4)
        h = height
        headerView.layout(0, 0, width, 0)

        currentTargetOffsetTop = container.top
    }

    override fun ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (target == null) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child.tag != headerTag) {
                    target = child
                    break
                }
            }
        }
    }

    override fun onRelease() {
        val valAnim = ValueAnimator.ofInt(currentTargetOffsetTop, 0)
        valAnim.duration = 100L
        valAnim.interpolator = AccelerateDecelerateInterpolator()
        valAnim.addUpdateListener { it ->
            val value = it.animatedValue as Int
            anim(value)
            move(value - currentTargetOffsetTop)
        }
        valAnim.start();
        Log.d(TAG, "On release offset $currentTargetOffsetTop")
    }

}

open class ArchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    val lightRed = Paint()


    override fun onDraw(canvas: Canvas) {
        val h = top - bottom
        val offset = 0
        val colors = intArrayOf(Color.RED, Color.BLUE)
        val positions = floatArrayOf(0f, 180f / 360f)
        val gradient =
            SweepGradient((width / 2).toFloat(), (measuredHeight / 2).toFloat(), colors, positions)
        lightRed.shader = gradient
        val rectF =
            RectF(left.toFloat() - offset, h.toFloat(), right.toFloat() + offset, bottom.toFloat())
        canvas.drawArc(rectF, 0f, 180f, true, lightRed)
    }
}