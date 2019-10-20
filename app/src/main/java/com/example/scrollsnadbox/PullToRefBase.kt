package com.example.scrollsnadbox

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.core.view.*
import androidx.core.widget.ListViewCompat


abstract class PullToRefBase @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr),
    NestedScrollingParent,
    NestedScrollingChild {

    private val LOG_TAG = PullToRefBase::class.java.simpleName

    abstract fun onPull(distance: Float)
    abstract fun onRelease()

    private var activePointerId: Int = 0
    private var initialMotionY: Float = 0.toFloat()
    private var initialDownY: Float = 0.toFloat()
    private var isBeingDragged: Boolean = false

    // If nested scrolling is enabled, the total amount that needed to be
    // consumed by this as the nested scrolling parent is used in place of the
    // overscroll determined by MOVE events in the onTouch handler
    private var totalUnconsumed: Float = 0.toFloat()
    private val nestedScrollingParentHelper: NestedScrollingParentHelper =
        NestedScrollingParentHelper(this)
    private val nestedScrollingChildHelper: NestedScrollingChildHelper =
        NestedScrollingChildHelper(this)
    private val parentScrollConsumed = IntArray(2)
    private val parentOffsetInWindow = IntArray(2)
    private var nestedScrollInProgress: Boolean = false


    open var target: View? = null // the target of the gesture

    init {
        isNestedScrollingEnabled = true
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        val width = measuredWidth
        val height = measuredHeight
        if (childCount == 0) {
            return
        }
        if (target == null) {
            ensureTarget()
        }
        if (target == null) {
            return
        }
        val child = target
        val childLeft = paddingLeft
        val childTop = paddingTop
        val childWidth = width - paddingLeft - paddingRight
        val childHeight = height - paddingTop - paddingBottom
        child?.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (target == null) {
            ensureTarget()
        }
        if (target == null) {
            return
        }
        target?.measure(
            MeasureSpec.makeMeasureSpec(
                measuredWidth - paddingLeft - paddingRight,
                MeasureSpec.EXACTLY
            ), MeasureSpec.makeMeasureSpec(
                measuredHeight - paddingTop - paddingBottom,
                MeasureSpec.EXACTLY
            )
        )

    }

    open fun ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (target == null) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                target = child
                break
            }
        }
    }

    private fun startDragging(y: Float) {
        if (!isBeingDragged) {
            initialMotionY = initialDownY
            isBeingDragged = true
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked
        var pointerIndex = -1

        if (!isEnabled || nestedScrollInProgress
        ) {
            // Fail fast if we're not in a state where a swipe is possible
            return false
        }


        when (action) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = ev.getPointerId(0)
                isBeingDragged = false
                pointerIndex = ev.findPointerIndex(activePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                initialDownY = ev.getY(pointerIndex)
            }

            MotionEvent.ACTION_UP -> {
                isBeingDragged = false
                onRelease()
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_MOVE -> {
                pointerIndex = ev.findPointerIndex(activePointerId)
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.")
                    return false
                }

                val y = ev.getY(pointerIndex)
                startDragging(y)

                if (isBeingDragged) {
                    val overscrollTop = (y - initialMotionY)
                    onPull(overscrollTop)
                }

            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                pointerIndex = ev.actionIndex
                if (pointerIndex < 0) {
                    Log.e(
                        LOG_TAG,
                        "Got ACTION_POINTER_DOWN event but have an invalid action index."
                    )
                    return false
                }
                activePointerId = ev.getPointerId(pointerIndex)
            }
        }
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked
        var pointerIndex = -1


        if (!isEnabled || nestedScrollInProgress
        ) {
            // Fail fast if we're not in a state where a swipe is possible
            return false
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                activePointerId = ev.getPointerId(0)
                isBeingDragged = false
                pointerIndex = ev.findPointerIndex(activePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                initialDownY = ev.getY(pointerIndex)
            }

            MotionEvent.ACTION_UP -> {
                isBeingDragged = false
                onRelease()
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_MOVE -> {
                pointerIndex = ev.findPointerIndex(activePointerId)
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.")
                    return false
                }

                val y = ev.getY(pointerIndex)
                startDragging(y)

                if (isBeingDragged) {
                    val overscrollTop = (y - initialMotionY)
                    onPull(overscrollTop)
                }
                return false

            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                pointerIndex = ev.actionIndex
                if (pointerIndex < 0) {
                    Log.e(
                        LOG_TAG,
                        "Got ACTION_POINTER_DOWN event but have an invalid action index."
                    )
                    return false
                }
                activePointerId = ev.getPointerId(pointerIndex)
            }
        }
        return isBeingDragged
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return (isEnabled
                && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0)
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes)
        startNestedScroll(axes and ViewCompat.SCROLL_AXIS_VERTICAL)
        totalUnconsumed = 0f
        nestedScrollInProgress = true
    }

    override fun onStopNestedScroll(target: View) {
        nestedScrollingParentHelper.onStopNestedScroll(target)
        nestedScrollInProgress = false

        if (totalUnconsumed > 0) {
            totalUnconsumed = 0f
        }
        onRelease()
        stopNestedScroll()
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        val parentConsumed = parentScrollConsumed

        if (dy > 0 && totalUnconsumed > 0) {
            if (dy > totalUnconsumed) {
                consumed[1] = dy - totalUnconsumed.toInt()
                totalUnconsumed = 0f
            } else {
                totalUnconsumed -= dy.toFloat()
                consumed[1] = dy
            }
            onPull(totalUnconsumed)
        }

        if (dy < 0 && totalUnconsumed < 0) {
            if (dy < totalUnconsumed) {
                consumed[1] = dy + totalUnconsumed.toInt()
                totalUnconsumed = 0f
            } else {
                totalUnconsumed -= dy.toFloat()
                consumed[1] = dy
            }
            onPull(totalUnconsumed)
        }

        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0]
            consumed[1] += parentConsumed[1]
        }
    }

    override fun onNestedScroll(
        target: View, dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int
    ) {
        dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            parentOffsetInWindow
        )

        val dy = dyUnconsumed + parentOffsetInWindow[1]
        if (dy < 0 && !canChildScrollUp()) {
            totalUnconsumed += Math.abs(dy).toFloat()
        }

        if (dy > 0 && !canChildScrollDown()) {
            totalUnconsumed -= Math.abs(dy).toFloat()
        }

        onPull(totalUnconsumed)
    }

    // NestedScrollingChild

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        nestedScrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return nestedScrollingChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return nestedScrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        nestedScrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return nestedScrollingChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
        dyUnconsumed: Int, offsetInWindow: IntArray?
    ): Boolean {
        return nestedScrollingChildHelper.dispatchNestedScroll(
            dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed, offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(
            dx, dy, consumed, offsetInWindow
        )
    }

    override fun onNestedPreFling(
        target: View, velocityX: Float,
        velocityY: Float
    ): Boolean {
        return dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onNestedFling(
        target: View, velocityX: Float, velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    private fun canChildScrollUp(): Boolean {
        return if (target is ListView) {
            ListViewCompat.canScrollList(target as ListView, -1)
        } else target?.canScrollVertically(-1) == true
    }

    private fun canChildScrollDown(): Boolean {
        return if (target is ListView) {
            ListViewCompat.canScrollList(target as ListView, 1)
        } else target?.canScrollVertically(1) == true
    }

}