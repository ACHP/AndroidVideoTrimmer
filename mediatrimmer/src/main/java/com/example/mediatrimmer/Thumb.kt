package com.example.mediatrimmer

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import java.util.*

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable


enum class ThumbType{
    LEFT,
    RIGHT,
    CURSOR
}

class Thumb {

    //Bitmap used to dra the cursor
    var bitmap: Bitmap

    //Type of the thumb ( Left, Right or Cursor )
    internal var type: ThumbType
        private set

    //Width of the bitmap
    internal var widthBitmap: Int = 0
        private set

    //height of the bitmap
    internal var heightBitmap: Int = 0
        private set

    //TODO : useless
    internal var index: Int = 0

    //Real value associated with the thumb, usually, it's the position (in the video, so in ms)
    internal var value: Float = 4.toFloat()
        set(value){
            field = value
            valueInitialized = true
        }

    //Level of the thumb.It's used to know which thumb is the most important when two thumbs are too closes
    //And the user want to pick one of them
    internal var level : Int = 0

    internal var valueInitialized = false
        private set

    //Position of the thumb relative to the parent ( in px)
    internal var pos: Float = 0.toFloat()
        set(value) {
            field = if(value<0) 0f else value
            positionChangedListener?.invoke(this, field)
        }

    internal var lastTouchX: Float = 0.toFloat()
        set(value) {
            field = if(value<0) 0f else value
        }


    constructor(type:ThumbType, bitmap:Bitmap){
        this.type = type
        this.bitmap = bitmap

        widthBitmap = bitmap.width
        heightBitmap= bitmap.height

        index = when(type){
            ThumbType.LEFT -> 0
            ThumbType.RIGHT -> 1
            ThumbType.CURSOR -> 2
        }

        level = when(type){
            ThumbType.LEFT -> 2
            ThumbType.RIGHT -> 2
            ThumbType.CURSOR -> 1
        }
    }

    private var positionChangedListener: ((Thumb, Float)->Unit)?=null

    internal fun onPositionChanged(fn: (Thumb, Float)->Unit) {
        positionChangedListener = fn
    }

    fun setDrawableResources(resources:Resources, drawable:Int){
        this.bitmap = drawableToBitmap(resources.getDrawable(drawable, null))
    }


    companion object {

        fun getWidthBitmap(thumbs: List<Thumb>): Int {
            return thumbs[0].widthBitmap
        }

        fun getHeightBitmap(thumbs: List<Thumb>): Int {
            return thumbs[0].heightBitmap
        }
    }
}
