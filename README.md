# AndroidVideoTrimmer
AndroidVideoTrimmer is a customizable UI component you can use in your application to trim a video.
For now, this project do not contains the video trimmer itself (like ffmpeg).
You are free to use whatever solution you want to cut/trim your video
⚠️ Keep in mind that this project is still in progress

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
  implementation 'com.github.ACHP:AndroidVideoTrimmer:v0.2-alpha'
}
```
### How to use
You just have to specify a valid Uri to the component and it should works
```kotlin
  val videoTrimmer:VideoTrimmer = findViewById(R.id.video_trimmer)
  videoTrimmer.videoSource = Uri.parse(path)
```

Of course you may want to listen for changes, to do this you can add a **VideoTrimmerListener**
```kotlin
videoTrimmer.addVideoTrimmerListener(object:VideoTrimmerListener{
            override fun startChanged(startTime: Long) {}  //Called when the start position has changed

            override fun stopChanged(endTime: Long) { //Called when the stop position has changed
                runOnUiThread {
                  // ⚠️ I still have a little problem here, the event is triggered from a background thread
                }
            }
            override fun onSeek(time: Long) {}//Called when the progress cursor has moved
            override fun onSeekStart() {} //Called when the user start moving a thumb
            override fun onSeekStop() {} //Called when the user stop moving a thumb
        })
```

### Properties

| Name |type| Description | default value |
|------|----|-------------|---------------|
|timelinePaddingLeft|Dimension| **[Important]** Used to avoid right thumb clip/overflow. Usually the size of the left thumb | *0dp* |
|timelinePaddingRight|Dimension| **[Important]** Used to avoid right thumb clip/overflow. Usually the size of the right thumb | *0dp* |
|timelineMarginLeft|Dimension|Set the left margin between the timeline and the left of the component | *0dp* |
|timelineMarginRight|Dimension|Set the right margin between the timeline and the left of the component | *0dp* |
|borderRadius|Dimension|Set the border radius of the component (the timeline AND shadow )  | *0dp* |
|enableSplitter|Boolean|If set to true,splitter will be displayed between each frame of the timeline  | *true* |
|splitterColor|Color|Set the color of the splitter displayed in the timeline  | *Color.BLACK* |
|splitterAlpha|Integer|Set the alpha channel of the splitter displayed in the timeline  | *128* |
|splitterWidth|Dimension|Set the width of the splitter displayed in the timeline   | *1dp* |
|selectedBorderColor|Color|Set the color of the borders (top & bottom) of the selected area | *Color.BLACK* |
|selectedBorderWidth|Dimension|Set the width of the borders (top & bottom) of the selected area | *1dp* |
|shadowColor|Color|Set the color of the overlay/shadow on the non-selected area | *Color.BLACK* |
|shadowAlpha|Integer|Set the color alpha of the overlay/shadow on the non-selected area | *128* |
|thumbLeftDrawable|Integer|Set the drawable of the left thumb | provided image |
|thumbRightDrawable|Integer| Set the drawable of the right thumb | provided image |
|thumbCursorDrawable|Integer|Set the drawable of the cursor| provided image |

### Examples

In the layout file (*.xml*) of your activity
```xml
<com.example.mediatrimmer.VideoTrimmer
        <...>
        app:timelinePaddingRight="20dp"
        app:timelinePaddingLeft="20dp"
        app:timelineMarginLeft="20dp"
        app:timelineMarginRight="20dp"
        app:shadowColor="@color/darkshadow"
        app:shadowAlpha="212"
        app:borderRadius="8dp"
        app:enableSplitter="true"
        app:splitterWidth="2dp"
        app:splitterColor="@android:color/white"
        app:splitterAlpha="255"
        app:selectedBorderColor="@android:color/holo_blue_bright"
        app:selectedBorderWidth="4dp"
        />
```

Or programmatically ...
```kotlin
        val videoTrimmer:VideoTrimmer = findViewById(R.id.custom_viewgroup)
        videoTrimmer.videoSource = Uri.parse(path)
        
        videoTrimmer.timelineMarginLeft = dip(50)
        videoTrimmer.timelineMarginRight = dip(50)
        videoTrimmer.timelinePaddingLeft = dip(20)
        videoTrimmer.timelinePaddingRight = dip(20)
        videoTrimmer.enableSplitter = true
        videoTrimmer.splitterColor = Color.RED
        videoTrimmer.splitterAlpha = 0
        videoTrimmer.splitterWidth= dip(5)
        videoTrimmer.selectedBorderColor = ContextCompat.getColor(this, R.color.pink)
        videoTrimmer.selectedBorderWidth = dip(2)
        videoTrimmer.shadowColor = ContextCompat.getColor(this, R.color.darkshadow)
        videoTrimmer.shadowAlpha = 212
        videoTrimmer.borderRadius = dip(8) 
    
```
