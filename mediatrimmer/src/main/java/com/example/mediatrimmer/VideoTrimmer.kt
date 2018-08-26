package com.example.mediatrimmer

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
class VideoTrimmer(context: Context, attrs:AttributeSet):FrameLayout(context, attrs){

    var timeLineView:TimeLineView
    var rangeSeekBarView:RangeSeekBarView

    init{
        LayoutInflater.from(context).inflate(R.layout.video_trimmer_layout, this);
        timeLineView= findViewById(R.id.timelineview)
        rangeSeekBarView= findViewById(R.id.range_seek_bar)
    }


    /**
     * This setter is used to set the space between the left of the video trimmer itself, and the left of the timeline view
     * From the "videoTrimmer component" point of vue, you want to change the padding
     * But actually from a framelayout point of vue, you change the margin
     */
    var timelinePaddingLeft:Int =0
        set(value) {
            field = value
            (timeLineView.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = value
            rangeSeekBarView.timelinePaddingLeft = value.toLong() //TODO : toLong sux
        }

    /**
     * This setter is used to set the space between the right of the video trimmer itself, and the tight of the timeline view
     * From the "videoTrimmer component" point of vue, you want to change the padding
     * But actually from a framelayout point of vue, you change the margin
     */
    var timelinePaddingRight:Int =0
        set(value) {
            field = value
            (timeLineView.layoutParams as ViewGroup.MarginLayoutParams).rightMargin = value
            rangeSeekBarView.timelinePaddingRight = value.toLong()
        }


    /**
     * Set the video source used to create the timeline
     */
    var videoSource: Uri?=null
        set(value) {
            if(value !=null){
                field = value
                timeLineView.videoSource = value
                rangeSeekBarView.setVideo(value)
            }

        }

    /**
     * Set this to true if you want a splitter between each frame of the timeline
     */
    var enableSplitter:Boolean=true
        set(value) {
            field=value
            timeLineView.enableSplitter=value
        }

    /**
     * Set the color of the splitter between each frame of the timeline
     * enableSplitter must be set to True
     */
    var splitterColor: Int = Color.BLACK
        set(value) {
            field=value
            timeLineView.imageSplitterColor = value
        }

    /**
     * Set the width of the splitter between each frame of the timeline
     * enableSplitter must be set to True
     */
    var splitterWidth: Int = dip(1)
        set(value) {
            field=value
            timeLineView.imageSplitterWidth = value
        }

    /**
     * Set the alpha channel of the splitter between each frame of the timeline
     * enableSplitter must be set to True
     */
    var splitterAlpha: Int = 128
        set(value) {
            field=value
            timeLineView.imageSplitterAlpha = value
        }


    /**
     * Set the border color of the area selected by the user
     */
    var selectedBorderColor: Int = Color.BLACK
        set(value) {
            field=value
            rangeSeekBarView.selectedBorderColor = value
        }

    /**
     * Set the border width of the area selected by the user
     */
    var selectedBorderWidth: Int = dip(1)
        set(value) {
            field=value
            rangeSeekBarView.selectedBorderWidth = value
        }


    /**
     * Set the shadowColor for the non-selected area
     */
    var shadowColor: Int = dip(1)
        set(value) {
            field=value
            rangeSeekBarView.shadowColor = value
        }

    /**
     * Set the shadowAlpha for the non-selected area
     */
    var shadowAlpha: Int = dip(1)
        set(value) {
            field=value
            rangeSeekBarView.shadowAlpha = value
        }

    fun addVideoTrimmerListener(videoTrimmerListener:VideoTrimmerListener){
        rangeSeekBarView.addOnRangeSeekBarListener(videoTrimmerListener)
    }





}