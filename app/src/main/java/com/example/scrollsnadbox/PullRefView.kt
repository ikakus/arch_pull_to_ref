package com.example.scrollsnadbox

import android.content.Context
import android.util.AttributeSet

class PullRefView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : PullToRefBase(context, attrs, defStyleAttr) {

    private val container by lazy { mTarget!! }

    override fun onPull(distance: Float) {
        if (distance >= 0) {
            container.top = distance.toInt()
        } else {
            container.top = 0
        }
    }

    override fun onRelease() {
        container.top = 0
    }

}