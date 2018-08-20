package com.example.mediatrimmer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.util.LongSparseArray
import android.view.View
import kotlinx.coroutines.experimental.launch
import kotlin.properties.Delegates


class TimeLineView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
        ) : View(context, attrs, defStyleAttr) {

    //Default Height for the timeline
    private var mHeightView: Int = dip(40)

    //List containing all generated images
    private var mBitmapList: LongSparseArray<Bitmap> = LongSparseArray()

    //Listener to call when all the bitmaps has been generated
    private var onReadyFunction:(()->Unit)?=null

    //Paint for imageSplitter
    private var imageSplitterPaint:Paint = Paint()

    var enableSplitter = true

    //Image Splitter color value
    var imageSplitterColor:Int by Delegates.observable(Color.WHITE){
        _,_,_-> refreshImageSplitterPaint()
    }
    //Image Splitter width value
    var imageSplitterWidth:Int by Delegates.observable(dip(1)){
        _,_,_-> refreshImageSplitterPaint()
    }
    //Image Splitter alpha value
    var imageSplitterAlpha:Int by Delegates.observable(128){
        _,_,_-> refreshImageSplitterPaint()
    }

    //URL of the video
    var videoSource: Uri? = null

    //Length of the video
    var videoLengthInMs:Long?=null
        private set


    init{
        //We init the imageSplitter paint
        imageSplitterPaint.color  = imageSplitterColor
        imageSplitterPaint.alpha  = imageSplitterAlpha
        imageSplitterPaint.strokeWidth  = imageSplitterWidth.toFloat()
    }

    /**
     * Methods to set the onReady callback
     */
    fun setOnReady(fn:()->Unit){ onReadyFunction = fn }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW = paddingLeft + paddingRight + suggestedMinimumWidth
        val w = View.resolveSizeAndState(minW, widthMeasureSpec, 1)

        val minH = paddingBottom + paddingTop + mHeightView
        val h = View.resolveSizeAndState(minH, heightMeasureSpec, 1)
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        mHeightView = h

        //If height has changed, re-generate bitmap list asynchronously
        if (w != oldW) {
            launch {
                mBitmapList =  getBitmap(w)
            }
        }
    }

    /**
     * This function is used to process the video and return a number of key frames of this video
     */
    private fun getBitmap(viewWidth: Int): LongSparseArray<Bitmap> {

        val thumbnailList = LongSparseArray<Bitmap>()

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, videoSource)

        // Retrieve media data
        val videoLengthInMsImmutable = (Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000).toLong()
        videoLengthInMs = videoLengthInMsImmutable

        val firstBitmap = mediaMetadataRetriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC)

        // Set thumbnail properties (Thumbs are squares)
        val thumbWidth = (firstBitmap.width.toFloat() / firstBitmap.height.toFloat() * mHeightView.toFloat() ).toInt()
        val thumbHeight = mHeightView
        val finalThumbWidth = mHeightView
        val numThumbs = Math.ceil((viewWidth.toFloat() / finalThumbWidth).toDouble()).toInt()
        val interval = videoLengthInMsImmutable / numThumbs
        val thumbsTotalWidth = numThumbs*finalThumbWidth
        val overflowWidth = thumbsTotalWidth - viewWidth

        for (i in 0 until numThumbs) {

            //On récupere une frame de la video
            var bitmap = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC)
            //On la transforme en miniature
            bitmap = Bitmap.createScaledBitmap(bitmap, thumbWidth, thumbHeight, false)
            bitmap = if(i==numThumbs-1) {
                //La derniere frame on la coupe si elle dépasse
                Bitmap.createBitmap(bitmap, 0,0,finalThumbWidth - overflowWidth, thumbHeight)
            }else{
                Bitmap.createBitmap(bitmap, (thumbWidth/2) - (finalThumbWidth/2),0,thumbHeight, thumbHeight)
            }

            thumbnailList.put(i.toLong(), bitmap)
        }

        postInvalidate()
        onReadyFunction?.invoke()

        mediaMetadataRetriever.release()

        return thumbnailList
    }


    //Methods to
    private fun refreshImageSplitterPaint(){
        imageSplitterPaint.color = imageSplitterColor
        imageSplitterPaint.strokeWidth = imageSplitterWidth.toFloat()
        imageSplitterPaint.alpha = imageSplitterAlpha
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        var x = 0

        for (i in 0 until mBitmapList.size()) {
            val bitmap = mBitmapList.get(i.toLong())

            if (bitmap != null) {
                canvas.drawBitmap(bitmap, left+x.toFloat(), 0f, null)

                //If image splitter is enabled we draw it
                if(enableSplitter && x > 0){
                    canvas.drawLine(
                            left+x.toFloat(),
                            0f,
                            left+x.toFloat(),
                            bitmap.height.toFloat(),
                            imageSplitterPaint
                    )
                }
                x += bitmap.width
            }

        }

    }
}