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

