package com.example.mediatrimmer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.properties.Delegates

interface VideoTrimmerListener {

    fun startChanged(startTime:Long)
    fun stopChanged(endTime:Long)
    fun onSeek(time:Long)
    fun onSeekStart()
    fun onSeekStop()

}


class RangeSeekBarView @JvmOverloads constructor(context: Context, attrs: AttributeSet,  defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    //hauteur de la timeline ( calculé plus tard )
    private var mHeightTimeLine: Int = 0


    //listener permettant d'écouter les event onSeek, onSeekStart, on seekStop, etc...
    private var mListeners: MutableList<VideoTrimmerListener>? = null

    //taille du grip/thumb gauche
    private var mThumbWidth: Float = 0.toFloat()
    private var mThumbHeight: Float = 0.toFloat()

    //Taille de la vue ( le container )
    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0

    private var mPixelRangeMin: Long = 0
    private var mPixelRangeMax: Long = 0
    private var mScaleRangeMax: Float = 0.toFloat()

    //Liste des grip/curseur/pointeurs

    val thumbLeft = Thumb(ThumbType.LEFT, drawableToBitmap(resources.getDrawable(R.drawable.vector_drawable_video_trim_thumb_left, null)))
    val thumbRight = Thumb(ThumbType.RIGHT, drawableToBitmap(resources.getDrawable(R.drawable.vector_drawable_video_trim_thumb_right, null)))
    val thumbCursor = Thumb(ThumbType.CURSOR, drawableToBitmap(resources.getDrawable(R.drawable.vector_drawable_video_trim_progress, null)))
    private var thumbs: List<Thumb> = listOf(thumbLeft, thumbRight, thumbCursor)

    private var mFirstRun: Boolean = false

    //_____________________Padding
    var timelinePaddingLeft:Long = 0
    var timelinePaddingRight:Long = 0

    //_____________________SHADOW
    //La couleur de l'overlay a gauche et a droite de la video quand on trim
    private var mShadow = Paint().apply { isAntiAlias = true }

    var shadowColor:Int by Delegates.observable(Color.WHITE){ _,_,_ -> refreshShadowPaint() }
    var shadowAlpha:Int by Delegates.observable(200){ _,_,_-> refreshShadowPaint() }
    var borderRadius:Int = 0
    var shadowPaddingLeft:Long = 0
    var shadowPaddingRight:Long = 0

    private fun refreshShadowPaint(){
        mShadow.color = shadowColor
        mShadow.alpha = shadowAlpha
    }


    //____________________BORDERS ( top and bottom lines )
    private var borderPaint = Paint().apply { isAntiAlias = true }
    var selectedBorderColor:Int by Delegates.observable(Color.MAGENTA){_,_,_-> refreshBorderPaint()}
    var selectedBorderWidth:Int by Delegates.observable(dip(4)){_,_,_->refreshBorderPaint()}

    private fun refreshBorderPaint(){
        borderPaint.color = selectedBorderColor
        borderPaint.strokeWidth = selectedBorderWidth.toFloat()
    }



    //infos sur le curseur
    private object cursorInfos{
        var minCursorPos:Long = 0
        var maxCursorPos:Long = Long.MAX_VALUE
    }
    //info sur la timeline
    private object timelineInfos{
        var width:Long = 0
        var left:Long=0
    }

    //infos sur la video
    private object videoInfos{
        lateinit var path:String
        var duration:Long = 0
    }

    //Uri de la video
    private var mVideoUri:Uri?=null

    //Méthode permettant de set l'uri
    fun setVideo(data: Uri) {
        mVideoUri = data

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, data)

        // Retrieve media data
        videoInfos.duration = (Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000).toLong()
        videoInfos.path = data.toString()

    }

    //progression de la video en ms
    var progress: Long= 0
        set(currentTime) {
            field = currentTime
            updateCursorProgress(currentTime)
        }

    init {

        mThumbWidth = Thumb.getWidthBitmap(thumbs).toFloat()
        mThumbHeight = Thumb.getHeightBitmap(thumbs).toFloat()

        timelineInfos.left = mThumbWidth.toLong()
        mScaleRangeMax = 100f

        isFocusable = true
        isFocusableInTouchMode = true
        mFirstRun = true


    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val minW = paddingLeft + paddingRight + suggestedMinimumWidth
        mViewWidth = View.resolveSizeAndState(minW, widthMeasureSpec, 1)
        val minH = paddingBottom + paddingTop + mThumbHeight.toInt() + mHeightTimeLine
        mViewHeight = View.resolveSizeAndState(minH, heightMeasureSpec, 1)

        setMeasuredDimension(mViewWidth, mViewHeight)
        if(mViewWidth==0){ return }


        mPixelRangeMin = 0 //leftmost pixel where we can draw
        mPixelRangeMax = mViewWidth - timelinePaddingRight //rightmost pixel where we candraw
        mHeightTimeLine = mViewHeight
        timelineInfos.width = mViewWidth - (timelinePaddingLeft + timelinePaddingRight)

        cursorInfos.minCursorPos = mThumbWidth.toLong()
        cursorInfos.maxCursorPos = mViewWidth + mThumbWidth.toLong()

        val requiredEndPosition  = if(thumbRight!=null && thumbRight.valueInitialized){
            (thumbRight.value / (videoInfos.duration / 1000) * timelineInfos.width)+cursorInfos.minCursorPos
        }else{
            mPixelRangeMax.toFloat()
        }


        val requiredStartPosition  = if(thumbLeft!=null && thumbLeft.valueInitialized){
            thumbLeft.value / (videoInfos.duration / 1000) * timelineInfos.width
        }else{
            0f
        }


        val requiredCursorPosition  = if(thumbCursor!=null && thumbCursor.valueInitialized){
            (thumbCursor.value / (videoInfos.duration / 1000) * timelineInfos.width)+cursorInfos.minCursorPos
        }else{
            cursorInfos.minCursorPos.toFloat()
        }

        thumbs.forEach {thumb -> when(thumb.type){
            ThumbType.LEFT-> {
                thumb.pos = requiredStartPosition
            }
            ThumbType.RIGHT -> {
                thumb.pos = requiredEndPosition
            }
            ThumbType.CURSOR-> {
                thumb.pos = requiredCursorPosition
            }
        }}

    }

    /**
     * Méthode permettant de dessiner l'ensemble du composant
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawShadow(canvas)
        drawThumbs(canvas)
        drawBorder(canvas)

    }


    //Correspond au grip/pointeur en cours d'utilisation
    private var currentThumb: Thumb?=null

    //Dernier geste effectué par l'utilisateur, utile pour différencier un click d'un drag&drop
    private var lastMotion:Int?=null


    /**
     * Méthode qui va être appelée quand on va toucher le composant
     * Attention si on renvoie false, on bloque les autres events
     */
    override fun onTouchEvent(ev: MotionEvent): Boolean {

        val coordinate = ev.x
        val action = ev.action

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastMotion = MotionEvent.ACTION_DOWN
                val currentThumbIndex = getClosestThumb(coordinate)

                if (currentThumbIndex == -1) {
                    return true
                }
                val immutableCurrentThumb = thumbs.getOrNull(currentThumbIndex)?: return false
                immutableCurrentThumb.lastTouchX = coordinate
                currentThumb = immutableCurrentThumb

                return true
            }
            MotionEvent.ACTION_UP -> {

                when (lastMotion) {
                    MotionEvent.ACTION_MOVE -> {
                        //On a fait MOVE + UP, ça veut dire qu'on a fini de bouger le curseur, on tric le seekStop
                        notifySeekStop()

                        if(thumbLeft==null || thumbCursor==null){ return false }
                        //On vient de faire un drag&drop sur le curseur droit, on remet le curseur de lecture au début.
                        if(currentThumb==thumbRight){
                            moveCursor(thumbLeft.pos,thumbCursor, thumbLeft.lastTouchX)
//                            setThumbPos(thumbCursor, thumbCursor.pos)
                            invalidate()
                        }
                    }
                    MotionEvent.ACTION_DOWN -> {
                        //On vient de faire un click, on va essayer de bouger le curseur de lecture a l'endroit du click
                        if(thumbCursor==null){ return false }

                        notifySeekStart()
                        val dx = coordinate - thumbCursor.lastTouchX
                        val newX = thumbCursor.pos + dx
                        moveCursor(newX, thumbCursor,coordinate)
//                        setThumbPos(thumbCursor, thumbCursor.pos)
                        notifySeekStop()

                        invalidate()
                    }
                }
                //On indique que le dernier mouvement était un up, et on supprime la reference au grip utilisé.
                lastMotion = MotionEvent.ACTION_UP
                currentThumb=null
                return true
            }

            MotionEvent.ACTION_MOVE -> {

                //Si c'est le premier move, on trig le seekStart
                if(lastMotion != MotionEvent.ACTION_MOVE){
                    notifySeekStart()
                }

                lastMotion = MotionEvent.ACTION_MOVE

                val immutableCurrentThumb = currentThumb ?: return false
                val dx = coordinate - immutableCurrentThumb.lastTouchX
                val newX = immutableCurrentThumb.pos + dx

                //En fonction du curseur sélectionné, on va le déplacer en respectant ses contraintes
                when(immutableCurrentThumb.type){
                    ThumbType.LEFT -> moveLeftThumb(newX, immutableCurrentThumb, coordinate)
                    ThumbType.RIGHT-> moveThumbRight(newX, immutableCurrentThumb, coordinate)
                    ThumbType.CURSOR->moveCursor(newX, immutableCurrentThumb, coordinate)
                }

//                setThumbPos(immutableCurrentThumb, immutableCurrentThumb.pos)

                // Invalidate to request a redraw
                invalidate()
                return true
            }
        }
        return true
    }


    /**
     * Fonction permettant de bouger le curseur en respectant les contraintes suivante
     *  - Le curseur ne peut pas être inférieur au grip gauche
     *  - Le curseur ne peut pas être supérieur au grip droit
     */
    private fun moveCursor(newX:Float, thumb:Thumb, coordinate:Float){

        if(thumbLeft==null || thumbRight==null){ return }

        when {
            newX <= thumbLeft.pos + thumbLeft.widthBitmap -> thumb.pos = ( thumbLeft.pos + thumbLeft.widthBitmap)
            newX >= thumbRight.pos -> thumb.pos = thumbRight.pos
            else -> {
                thumb.pos = newX
                thumb.lastTouchX =coordinate
            }
        }

        val maxSize = timelineInfos.width
        val videoDurationMS:Long = videoInfos.duration/1000
        val offset = thumbLeft.widthBitmap?:0
        thumb.value = ((thumb.pos-offset) * videoDurationMS / maxSize )

        notifySeekTo()
    }

    /**
     * Fonction permettant de bouger le grip droit  en respectant les contraintes suivantes :
     *  -   Le grip droit ne peut pas être inférieur au grip gauche
     *  -   Le grip droit ne peut pas dépasser de la timeline
     *  -   Le curseur doit se trouver au même point que le grip pendant son déplacement
     */
    private fun moveThumbRight(newX:Float, thumb:Thumb, coordinate:Float){

        if(thumbCursor==null || thumbLeft==null){ return }

        when {
            ( newX <= thumbLeft.pos + thumbLeft.widthBitmap ) -> thumb.pos =( thumbLeft.pos + thumb.widthBitmap)
            ( newX >= mPixelRangeMax ) -> thumb.pos =( mPixelRangeMax.toFloat())
            else -> {
                thumb.pos =newX
                thumb.lastTouchX = coordinate
            }
        }

        val maxSize = timelineInfos.width
        val videoDurationMS:Long = videoInfos.duration/1000
        val offset = thumbLeft.widthBitmap?:0
        thumb.value = ((thumb.pos-offset) * videoDurationMS / maxSize )

        //On place le curseur au meme endroit, ce qui va permettre d'avoir une peview
        thumbCursor.pos = thumb.pos
        thumbCursor.value = thumb.value
        thumbCursor.lastTouchX = coordinate
        notifySeekTo()
        notifyStopChanged()
    }

    /**
     * Fonction permettant de bouger le grip gauche en respectant les contraintes suivantes :
     *  -   Le grip gauche ne peut pas être supérieur au grip droit
     *  -   Le grip gauche ne peut pas dépasser de la timeline
     *  -   Le curseur doit se trouver au même point que le grip pendant son déplacement
     */
    private fun moveLeftThumb(newX:Float, thumb:Thumb, coordinate:Float){

        if(thumbCursor==null || thumbRight==null){ return }

        when {
            newX + thumb.widthBitmap >= thumbRight.pos -> thumb.pos = (thumbRight.pos - thumb.widthBitmap)
            newX <= mPixelRangeMin -> thumb.pos =( mPixelRangeMin.toFloat())
            else -> {
                thumb.pos =newX
                thumb.lastTouchX =coordinate
            }
        }
        val maxSize = timelineInfos.width
        val videoDurationMS:Long = videoInfos.duration/1000
        thumb.value = (thumb.pos * videoDurationMS / maxSize )

        //On place le curseur au meme endroit, ce qui va permettre d'avoir une peview
        thumbCursor.pos = thumb.pos + thumb.widthBitmap
        thumbCursor.lastTouchX = coordinate
        thumbCursor.value = thumb.value
        notifySeekTo()
        notifyStartChanged()
    }


    /**
     * Méthode permettant de calculer l'emplacement du curseur de lecture en fonction du temps passé dans la vidéo
     */
    private fun updateCursorProgress(currentTime:Long){
        if(thumbCursor==null || thumbRight==null) return

        val durationMs = videoInfos.duration / 1000

        //Si le currentTime vaut 0, il faut déplacer le curseur au minimum ( = 0 + leftThumb.bitmap.width)
        //Sinon...on fait le calcul
        val newX:Float = when(currentTime){
            0.toLong() -> cursorInfos.minCursorPos.toFloat()
            else -> ((currentTime * timelineInfos.width / durationMs) + (thumbRight.widthBitmap * 2)).toFloat()
        }

        //Si jamais le progress dépasse le grip/curseur droit, on le stop
        if(newX<=thumbRight.pos){
            thumbCursor.pos = newX
        }else{
            thumbCursor.pos = thumbRight.pos
        }
        //On demande le redraw
        this.postInvalidate()
    }

    /**
     * Fonction permettant de récupérer l'index du grip/curseur le plus proche d'un point
     */
    private fun getClosestThumb(coordinate: Float): Int {
        var closest = -1

        if (thumbs.isEmpty()) { return closest}
        val threeshold = 40

        thumbs.forEach { thumb ->
            val tcoordinate = thumb.pos + mThumbWidth

            //Si on a deja trouvé un closest, qui se trouve plus haut que l'élement actuel
            //on ne vérifie pas si l'element actuel correspond aux coordonnées
            if(closest !=-1){
                thumbs[closest].level > thumb.level
                return@forEach
            }

            if (coordinate >= (thumb.pos-threeshold) && coordinate <= (tcoordinate+threeshold)) {
                closest = thumb.index
            }
        }

        if(closest==-1 && coordinate> thumbLeft.pos && coordinate < thumbRight.pos){
            closest = thumbCursor.index
        }

        return closest
    }


    /**
     * Methode permettant de dessiner l'overlay sur les parties trimées de la video
     */
    private fun drawShadow(canvas: Canvas) {
        if (!thumbs.isEmpty()) {

            thumbs.forEach { thumb -> when(thumb.type){
                ThumbType.LEFT -> {
                    val x = thumb.pos + paddingLeft
                    if (x > mPixelRangeMin && shadowPaddingLeft < (x + mThumbWidth)) {
                        val mRect = RoundedRect(
                                shadowPaddingLeft.toFloat(),
                                0f,
                                x + mThumbWidth,
                                mHeightTimeLine.toFloat(),
                                borderRadius.toFloat(),
                                borderRadius.toFloat(),
                                true, false, false, true
                        )
                        canvas.drawPath(mRect, mShadow)
                    }
                }
                ThumbType.RIGHT -> {
                    val x = thumb.pos - paddingRight
                    if (x < mPixelRangeMax && x < (mViewWidth - shadowPaddingRight) ) {

                        val mRect = RoundedRect(
                                x,
                                0f,
                                (mViewWidth - shadowPaddingRight).toFloat(),
                                mHeightTimeLine.toFloat(),
                                borderRadius.toFloat(),
                                borderRadius.toFloat(),
                                false, true, true, false
                        )

                        canvas.drawPath(mRect, mShadow)
                    }
                }
            }}

        }
    }


    /**
     * Méthode permettant de dessiner les bordures autour de la partie de la vidéo non trimé
     */
    private fun drawBorder(canvas: Canvas) {
        if(thumbLeft == null || thumbRight==null) return
        canvas.drawLine(thumbLeft.pos+thumbLeft.widthBitmap, 0f,thumbRight.pos,0f,Paint().also {
            it.color = selectedBorderColor
            it.strokeWidth = selectedBorderWidth.toFloat()
        })
        canvas.drawLine(thumbLeft.pos+thumbLeft.widthBitmap, mViewHeight.toFloat(),thumbRight.pos,mViewHeight.toFloat(),Paint().also {
            it.color = selectedBorderColor
            it.strokeWidth = selectedBorderWidth.toFloat()
        })
    }

    /**
     * méthode permettant de dessiner les différents curseurs
     */
    private fun drawThumbs(canvas: Canvas) {

        if (thumbs.isEmpty()) { return }

        thumbs.forEach { thumb-> when(thumb.type){
            ThumbType.LEFT,ThumbType.CURSOR -> canvas.drawBitmap(thumb.bitmap, thumb.pos + paddingLeft, paddingTop.toFloat(), null)
            ThumbType.RIGHT -> canvas.drawBitmap(thumb.bitmap, thumb.pos - paddingRight, paddingTop.toFloat(),null)
        }}

    }

    /**
     * Méthode permettant d'ajoute un listener
     */
    fun addOnRangeSeekBarListener(listener: VideoTrimmerListener) {

        if (mListeners == null) {
            mListeners = ArrayList()
        }

        mListeners?.add(listener)
    }


    /**
     * Méthode permettant de notifier le listener --------
     */
    private fun notifySeekStart():Unit? = mListeners?.forEach { it.onSeekStart() }

    private fun notifySeekStop():Unit? = mListeners?.forEach { it.onSeekStop() }

    private fun notifySeekTo(){
        mListeners?.forEach {
            it.onSeek(thumbCursor.value.toLong())
        }
    }

    private fun notifyStartChanged(){
        mListeners?.forEach {
            it.startChanged(thumbLeft.value.toLong())
        }
    }

    private fun notifyStopChanged(){
        Looper.getMainLooper().thread.run {
            mListeners?.forEach {listener->
                listener.stopChanged(
                        if(thumbRight != null && thumbRight.valueInitialized){
                            thumbRight.value.toLong()
                        }else{
                            videoInfos.duration/1000
                        })

            }
        }
    }

    fun setStartTime(startTimeVideo: Long) {
        thumbLeft?:return
        thumbLeft.value = startTimeVideo.toFloat()
    }

    fun setStopTime(endTimeVideo: Long) {
        thumbRight?:return
        thumbRight.value = endTimeVideo.toFloat()
    }
    //--------------------------------------------------------

    companion object {

        private val TAG = RangeSeekBarView::class.java.simpleName
    }
}