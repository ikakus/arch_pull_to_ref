package com.example.scrollsnadbox

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.NestedScrollingChild
import androidx.core.view.NestedScrollingParent


class PullToRef @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr),
    NestedScrollingParent,
    NestedScrollingChild {

    private val LOG_TAG = PullToRef::class.java.getSimpleName()

    private lateinit var mCircleView: ImageView
    private var mActivePointerId: Int = 0
    private var mInitialMotionY: Float = 0.toFloat()
    private var mInitialDownY: Float = 0.toFloat()
    private var mIsBeingDragged: Boolean = false

    internal var mCurrentTargetOffsetTop: Int = 0

    private var mTarget: View? = null // the target of the gesture

    init {
        createProgressView()
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        val width = measuredWidth
        val height = measuredHeight
        if (childCount == 0) {
            return
        }
        if (mTarget == null) {
            ensureTarget()
        }
        if (mTarget == null) {
            return
        }
        val child = mTarget
        val childLeft = paddingLeft
        val childTop = paddingTop
        val childWidth = width - paddingLeft - paddingRight
        val childHeight = height - paddingTop - paddingBottom
        child!!.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mTarget == null) {
            ensureTarget()
        }
        if (mTarget == null) {
            return
        }
        mTarget!!.measure(
            MeasureSpec.makeMeasureSpec(
                measuredWidth - paddingLeft - paddingRight,
                MeasureSpec.EXACTLY
            ), MeasureSpec.makeMeasureSpec(
                measuredHeight - paddingTop - paddingBottom,
                MeasureSpec.EXACTLY
            )
        )

    }

    private fun ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
//                if (child != mCircleView) {
                mTarget = child
                break
//                }
            }
        }
    }

    private fun createProgressView() {
        mCircleView = ImageView(context)
        mCircleView.setImageResource(R.drawable.ic_launcher_foreground)
        mCircleView.setBackgroundColor(R.color.colorAccent)
        addView(mCircleView)
    }


    private fun startDragging(y: Float) {
        val yDiff = y - mInitialDownY
        if (!mIsBeingDragged) {
            mInitialMotionY = mInitialDownY
            mIsBeingDragged = true
        }
    }

    private fun setTargetOffsetTopAndBottom(offset: Int) {
        Log.d(LOG_TAG, " offset ${offset}")
        mCircleView.top = offset
        mCurrentTargetOffsetTop = mCircleView.getTop()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked
        var pointerIndex = -1

        Log.d(LOG_TAG, "${ev.action}")

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mActivePointerId = ev.getPointerId(0)
                mIsBeingDragged = false
                pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                mInitialDownY = ev.getY(pointerIndex)
            }

            MotionEvent.ACTION_UP -> {
                mIsBeingDragged = false
                setTargetOffsetTopAndBottom(0)
            }

            MotionEvent.ACTION_MOVE -> {
                pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.")
                    return false
                }

                val y = ev.getY(pointerIndex)
                startDragging(y)

                if (mIsBeingDragged) {
                    val overscrollTop = (y - mInitialMotionY)
                    if (overscrollTop > 0) {
                        setTargetOffsetTopAndBottom(overscrollTop.toInt())
                    } else {
                        setTargetOffsetTopAndBottom(0)
                        return false
                    }
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
                mActivePointerId = ev.getPointerId(pointerIndex)
            }
        }
        return true
    }

}