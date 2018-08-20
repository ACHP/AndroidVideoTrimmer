package com.example.achp.videotrimmersample.components

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView

class AspectSurfaceView(context: Context, attrs: AttributeSet): SurfaceView(context, attrs) {

    companion object {
        private var DEFAULT_XRATIO = 1
        private var DEFAULT_YRATIO = 1
    }

    var xRatio = DEFAULT_XRATIO

    var yRatio = DEFAULT_YRATIO

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)


        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)


        if(widthMode == MeasureSpec.EXACTLY && (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED)) {
            setMeasuredDimension(widthSize, (widthSize.toDouble() / xRatio * yRatio).toInt())
        } else if(heightMode == MeasureSpec.EXACTLY && (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED)) {
            setMeasuredDimension((heightSize.toDouble() / yRatio * xRatio).toInt(), heightSize)
        } else {
            super.setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        }
    }

}