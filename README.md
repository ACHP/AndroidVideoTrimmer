# AndroidVideoTrimmer
AndroidVideoTrimmer is a customizable UI component you can use in your application to trim a video.
For now, this project do not contains the video trimmer itself (like ffmpeg).
You are free to use whatever solution you want to cut/trim your video

### Demo
<img src="https://i.imgur.com/HGbk5Qb.gif" alt="demo" width="200px"/>

### How to install

Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

Step 2. Add the dependency
```
dependencies {
  implementation 'com.github.ACHP:AndroidVideoTrimmer:v0.1-alpha'
}
```


### How to use
_DISCLAIMER_ :  This is an early stage documentation, it should be improved soon.

```kotlin
        var rangeSeekBar:RangeSeekBarView = findViewById(R.id.range_seek_bar)
        rangeSeekBar.setVideo(Uri.parse(path)) //   SET THE VIDEO SOURCE
        
        
        //You can change the bitmap of every thumbs and use yours
        rangeSeekBar.thumbLeft.setDrawableResources(resources, R.drawable.left_thumb)
        rangeSeekBar.thumbRight.setDrawableResources(resources, R.drawable.right_thumb)
        rangeSeekBar.thumbCursor.setDrawableResources(resources, R.drawable.progress_thumb)
        
        rangeSeekBar.timelinePaddingLeft = dip(16).toLong() // Usually, you use the width of your bitmaps as padding
        rangeSeekBar.timelinePaddingRight = dip(16).toLong()// Because you don't want the thumb to overlap the timeline at the starting/ending point
        
         //You can draw lines to wrap the selected area
        rangeSeekBar.selectedBorderColor = ContextCompat.getColor(this, R.color.pink)
        rangeSeekBar.selectedBorderWidth = dip(4)
        
        // The color and the opacity of the trimmed video part
        rangeSeekBar.shadowColor = ContextCompat.getColor(this, R.color.darkshadow)
        rangeSeekBar.shadowAlpha = 212
        
        //You can also draw splitter between each frame of the timeline
        rangeSeekBar.timelineView.enableSplitter = true
        rangeSeekBar.timelineView.imageSplitterColor = Color.BLACK
        rangeSeekBar.timelineView.imageSplitterAlpha = 128
        rangeSeekBar.timelineView.imageSplitterWidth = dip(1)
        
        
        
        //Finally you can listen for changes
        rangeSeekBar.addOnRangeSeekBarListener(object:OnRangeSeekBarListener{
                override fun startChanged(startTime: Long) {} //Called when the start position has changed
    
                override fun stopChanged(endTime: Long) {} //Called when the stop position has changed
    
                override fun onSeek(time: Long) {} //Called when one cursor has moved ( maybe you need to update a preview somewhere ... )
   
                override fun onSeekStart() {} //Called when the user start moving a thumb
    
                override fun onSeekStop() {} //Called when the user stop moving a thumb
    
        })
```
