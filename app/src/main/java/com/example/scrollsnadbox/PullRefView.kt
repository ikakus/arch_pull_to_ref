package com.example.scrollsnadbox

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

class PullRefView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PullToRefBase(context, attrs, defStyleAttr), PullListener {

    private lateinit var mCircleView: ImageView

    init {
        createProgressView()

        super.setListener(this)
    }

    private fun createProgressView() {
        mCircleView = ImageView(context)
        mCircleView.setImageResource(R.drawable.ic_launcher_foreground)
        mCircleView.setBackgroundColor(R.color.colorAccent)
        addView(mCircleView)
    }

    override fun onPull(distance: Float) {
        if (distance >= 0) {
            mCircleView.top = distance.toInt()
        } else {
            mCircleView.top = 0
        }

    }

    override fun onRelease() {
        mCircleView.top = 0
    }

}