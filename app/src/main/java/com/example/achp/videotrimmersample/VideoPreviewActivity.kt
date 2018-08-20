package com.example.achp.videotrimmersample

import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.TextView
import com.example.achp.videotrimmersample.components.AspectSurfaceView
import com.example.mediatrimmer.OnRangeSeekBarListener
import com.example.mediatrimmer.RangeSeekBarView
import com.example.mediatrimmer.dip
import java.io.File

class VideoPreviewActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private var path :String?=null
    private lateinit var surfaceHolder:SurfaceHolder
    private lateinit var surface: Surface
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var rangeSeekBar:RangeSeekBarView

    private lateinit var onSeekTextView:TextView
    private lateinit var startTextView:TextView
    private lateinit var stopTextView:TextView
    private lateinit var progressTextView:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_preview)

        path = intent.getStringExtra("path")

        onSeekTextView = findViewById(R.id.seek_text_view)
        startTextView = findViewById(R.id.start_text_view)
        stopTextView = findViewById(R.id.stop_text_view)
        progressTextView = findViewById(R.id.progress_text_view)

        val surfaceView = findViewById<AspectSurfaceView>(R.id.video_preview_surface_view)
        surfaceView.xRatio = 16
        surfaceView.yRatio = 9
        rangeSeekBar = findViewById(R.id.range_seek_bar)
        rangeSeekBar.setVideo(Uri.parse(path))
        rangeSeekBar.timelinePaddingLeft = dip(16).toLong()
        rangeSeekBar.timelinePaddingRight = dip(16).toLong()
        rangeSeekBar.selectedBorderColor = ContextCompat.getColor(this, R.color.pink)
        rangeSeekBar.selectedBorderWidth = dip(4)
        rangeSeekBar.shadowColor = ContextCompat.getColor(this, R.color.darkshadow)
        rangeSeekBar.shadowAlpha = 212
        rangeSeekBar.timelineView.enableSplitter = true
        rangeSeekBar.timelineView.imageSplitterColor = Color.BLACK
        rangeSeekBar.timelineView.imageSplitterAlpha = 128
        rangeSeekBar.timelineView.imageSplitterWidth = dip(1)
        rangeSeekBar.addOnRangeSeekBarListener(object:OnRangeSeekBarListener{
            override fun startChanged(startTime: Long) {
                startTextView.text = startTime.toString()
            }

            override fun stopChanged(endTime: Long) {
                runOnUiThread {
                    stopTextView.text = endTime.toString()
                }
            }

            override fun onSeek(time: Long) {
                progressTextView.text = time.toString()
            }

            override fun onSeekStart() {
                onSeekTextView.text = "seeking"
            }

            override fun onSeekStop() {
                onSeekTextView.text = "not seeking"
            }

        })


        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)
        surface = surfaceView.holder.surface

        mediaPlayer = MediaPlayer()

    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        /*Do nothing*/
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        mediaPlayer.release()
    }

    override fun surfaceCreated(p0: SurfaceHolder?) {

        val absolutePath : String =File(path).path
        mediaPlayer.setSurface(surface)

        mediaPlayer.setDataSource(absolutePath)
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
        }
        mediaPlayer.prepare()
    }

}
