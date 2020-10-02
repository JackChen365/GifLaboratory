## PerformanceTest

```
int width=640;
int height=640;
Bitmap drawingBitmap=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
long st = System.currentTimeMillis();
for(int i = 0; i < imageHeight; i++){
    for(int j=0;j<imageWidth;j++){
        drawingBitmap.setPixel(j,i, Color.WHITE);
    }
}
Log.i(TAG,"initialize time1:"+(System.currentTimeMillis()-st)+" frameIndex:"+frameIndex+" width:"+drawingBitmap.getWidth()+" height:"+drawingBitmap.getHeight()+" drawingBitmap:"+(drawingBitmap.hashCode()));
```

I just curious about why first time fill the Bitmap usually faster then the next time.

```
2020-09-21 21:08:29.669 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:155 frameIndex:0 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:29.711 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:10 frameIndex:1 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:29.756 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:13 frameIndex:2 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:29.803 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:16 frameIndex:3 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:29.849 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:15 frameIndex:4 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:29.896 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:15 frameIndex:5 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:29.945 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:17 frameIndex:6 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:30.003 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:26 frameIndex:7 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:30.064 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:30 frameIndex:8 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:30.133 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:36 frameIndex:9 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:30.199 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:33 frameIndex:10 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:30.272 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:40 frameIndex:11 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:30.345 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:40 frameIndex:12 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:30.423 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:45 frameIndex:13 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:30.506 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:50 frameIndex:14 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:30.589 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:50 frameIndex:15 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:30.676 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:54 frameIndex:16 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:30.767 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:58 frameIndex:17 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:30.863 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:63 frameIndex:18 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:30.965 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:68 frameIndex:19 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:31.073 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:74 frameIndex:20 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:31.185 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:78 frameIndex:21 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:31.304 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:85 frameIndex:22 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:31.431 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:92 frameIndex:23 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:31.564 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:99 frameIndex:24 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:31.704 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:105 frameIndex:25 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:31.852 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:113 frameIndex:26 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:32.006 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:120 frameIndex:27 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:32.174 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:132 frameIndex:28 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:32.353 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:144 frameIndex:29 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:32.534 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:146 frameIndex:30 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:32.725 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:155 frameIndex:31 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:32.916 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:156 frameIndex:32 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:33.110 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:158 frameIndex:33 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:33.302 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:156 frameIndex:34 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:33.497 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:158 frameIndex:35 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:33.661 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:36 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:33.827 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:37 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:33.993 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:38 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:34.158 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:39 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:34.323 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:40 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:34.488 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:41 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:34.653 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:42 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:34.818 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:43 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:34.983 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:44 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:35.148 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:45 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:35.312 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:46 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:35.477 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:47 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:35.643 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:48 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:35.808 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:49 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:35.973 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:50 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:36.139 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:51 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:36.304 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:52 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:36.469 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:53 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:36.633 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:54 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:36.798 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:55 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:36.963 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:56 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:37.127 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:57 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:37.291 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:58 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:37.456 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:59 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:37.622 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:131 frameIndex:60 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:37.786 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:61 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:37.950 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:62 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:38.114 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:63 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:38.279 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:64 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:38.443 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:65 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:38.609 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:131 frameIndex:66 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:38.773 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:67 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:38.939 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:131 frameIndex:68 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:39.103 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:69 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:39.268 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:70 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:39.432 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:71 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:39.596 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:72 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:39.761 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:73 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:39.926 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:131 frameIndex:74 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:40.089 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:128 frameIndex:75 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:40.253 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:76 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:40.418 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:131 frameIndex:77 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:40.583 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:131 frameIndex:78 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:40.746 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:79 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:40.910 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:80 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:41.074 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:130 frameIndex:81 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:41.236 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:128 frameIndex:82 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:41.399 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:128 frameIndex:83 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:41.564 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:131 frameIndex:84 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:41.728 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:128 frameIndex:85 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:41.890 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:86 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:42.053 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:87 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:42.216 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:88 width:640 height:640 drawingBitmap:183665066
2020-09-21 21:08:42.379 30650-30650/com.cz.android.gif.sample I/GifDecoder: initialize time1:129 frameIndex:89 width:640 height:640 drawingBitmap:183665066
```

* Glide and my decoder, The first version.

```
2020-09-25 12:59:51.903 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:1 time:20 delay:30
2020-09-25 12:59:51.936 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:2 time:2 delay:30
2020-09-25 12:59:51.969 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:3 time:3 delay:30
2020-09-25 12:59:52.002 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:4 time:3 delay:30
2020-09-25 12:59:52.035 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:5 time:3 delay:30
2020-09-25 12:59:52.068 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:6 time:3 delay:30
2020-09-25 12:59:52.101 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:7 time:3 delay:30
2020-09-25 12:59:52.135 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:8 time:3 delay:30
2020-09-25 12:59:52.170 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:9 time:4 delay:30
2020-09-25 12:59:52.204 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:10 time:4 delay:30
2020-09-25 12:59:52.238 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:11 time:5 delay:30
2020-09-25 12:59:52.273 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:12 time:4 delay:30
2020-09-25 12:59:52.308 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:13 time:5 delay:30
2020-09-25 12:59:52.343 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:14 time:5 delay:30
2020-09-25 12:59:52.378 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:15 time:5 delay:30
2020-09-25 12:59:52.414 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:16 time:6 delay:30
2020-09-25 12:59:52.449 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:17 time:5 delay:30
2020-09-25 12:59:52.484 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:18 time:6 delay:30
2020-09-25 12:59:52.521 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:19 time:6 delay:30
2020-09-25 12:59:52.557 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:20 time:6 delay:30
2020-09-25 12:59:52.592 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:21 time:5 delay:30
2020-09-25 12:59:52.628 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:22 time:6 delay:30
2020-09-25 12:59:52.665 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:23 time:7 delay:30
2020-09-25 12:59:52.706 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:24 time:9 delay:30
2020-09-25 12:59:52.744 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:25 time:8 delay:30
2020-09-25 12:59:52.784 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:26 time:10 delay:30
2020-09-25 12:59:52.824 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:27 time:10 delay:30
2020-09-25 12:59:52.865 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:28 time:11 delay:30
2020-09-25 12:59:52.909 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:29 time:13 delay:30
2020-09-25 12:59:52.955 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:30 time:15 delay:30
2020-09-25 12:59:52.999 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:31 time:14 delay:30
2020-09-25 12:59:53.046 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:32 time:16 delay:30
2020-09-25 12:59:53.090 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:33 time:14 delay:30
2020-09-25 12:59:53.131 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:34 time:11 delay:30
2020-09-25 12:59:53.175 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:35 time:13 delay:30
2020-09-25 12:59:53.219 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:36 time:14 delay:30
2020-09-25 12:59:53.262 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:37 time:14 delay:30
2020-09-25 12:59:53.305 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:38 time:12 delay:30
2020-09-25 12:59:53.347 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:39 time:11 delay:30
2020-09-25 12:59:53.388 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:40 time:10 delay:30
2020-09-25 12:59:53.429 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:41 time:10 delay:30
2020-09-25 12:59:53.469 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:42 time:11 delay:30
2020-09-25 12:59:53.511 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:43 time:10 delay:30
2020-09-25 12:59:53.554 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:44 time:12 delay:30
2020-09-25 12:59:53.596 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:45 time:11 delay:30
2020-09-25 12:59:53.639 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:46 time:13 delay:30
2020-09-25 12:59:53.683 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:47 time:13 delay:30
2020-09-25 12:59:53.727 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:48 time:13 delay:30
2020-09-25 12:59:53.769 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:49 time:11 delay:30
2020-09-25 12:59:53.811 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:50 time:10 delay:30
2020-09-25 12:59:53.852 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:51 time:11 delay:30
2020-09-25 12:59:53.897 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:52 time:13 delay:30
2020-09-25 12:59:53.944 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:53 time:16 delay:30
2020-09-25 12:59:53.988 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:54 time:14 delay:30
2020-09-25 12:59:54.030 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:55 time:12 delay:30
2020-09-25 12:59:54.072 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:56 time:12 delay:30
2020-09-25 12:59:54.115 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:57 time:13 delay:30
2020-09-25 12:59:54.161 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:58 time:15 delay:30
2020-09-25 12:59:54.204 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:59 time:13 delay:30
2020-09-25 12:59:54.245 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:60 time:12 delay:30
2020-09-25 12:59:54.288 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:61 time:12 delay:30
2020-09-25 12:59:54.325 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:62 time:6 delay:30
2020-09-25 12:59:54.366 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:63 time:10 delay:30
2020-09-25 12:59:54.408 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:64 time:12 delay:30
2020-09-25 12:59:54.448 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:65 time:10 delay:30
2020-09-25 12:59:54.492 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:66 time:14 delay:30
2020-09-25 12:59:54.537 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:67 time:15 delay:30
2020-09-25 12:59:54.583 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:68 time:16 delay:30
2020-09-25 12:59:54.626 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:69 time:12 delay:30
2020-09-25 12:59:54.668 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:70 time:11 delay:30
2020-09-25 12:59:54.708 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:71 time:9 delay:30
2020-09-25 12:59:54.749 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:72 time:10 delay:30
2020-09-25 12:59:54.789 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:73 time:10 delay:30
2020-09-25 12:59:54.830 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:74 time:11 delay:30
2020-09-25 12:59:54.871 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:75 time:11 delay:30
2020-09-25 12:59:54.913 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:76 time:11 delay:30
2020-09-25 12:59:54.956 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:77 time:12 delay:30
2020-09-25 12:59:55.003 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:78 time:16 delay:30
2020-09-25 12:59:55.047 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:79 time:14 delay:30
2020-09-25 12:59:55.091 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:80 time:12 delay:30
2020-09-25 12:59:55.133 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:81 time:11 delay:30
2020-09-25 12:59:55.175 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:82 time:11 delay:30
2020-09-25 12:59:55.219 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:83 time:14 delay:30
2020-09-25 12:59:55.265 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:84 time:15 delay:30
2020-09-25 12:59:55.308 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:85 time:12 delay:30
2020-09-25 12:59:55.348 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:86 time:10 delay:30
2020-09-25 12:59:55.388 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:87 time:10 delay:30
2020-09-25 12:59:55.429 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:88 time:10 delay:30
2020-09-25 12:59:55.468 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:89 time:9 delay:30
2020-09-25 12:59:55.509 2367-2367/com.cz.android.gif.sample I/GifView1: id:170045054 frameIndex:90 time:10 delay:30
```

```
2020-09-25 12:59:51.880 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:1 time:19 delay:30
2020-09-25 12:59:51.912 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:2 time:2 delay:30
2020-09-25 12:59:51.944 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:3 time:2 delay:30
2020-09-25 12:59:51.977 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:4 time:2 delay:30
2020-09-25 12:59:52.012 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:5 time:3 delay:30
2020-09-25 12:59:52.045 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:6 time:3 delay:30
2020-09-25 12:59:52.077 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:7 time:2 delay:30
2020-09-25 12:59:52.110 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:8 time:3 delay:30
2020-09-25 12:59:52.142 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:9 time:3 delay:30
2020-09-25 12:59:52.188 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:10 time:3 delay:30
2020-09-25 12:59:52.222 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:11 time:3 delay:30
2020-09-25 12:59:52.256 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:12 time:3 delay:30
2020-09-25 12:59:52.290 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:13 time:4 delay:30
2020-09-25 12:59:52.324 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:14 time:4 delay:30
2020-09-25 12:59:52.358 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:15 time:4 delay:30
2020-09-25 12:59:52.392 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:16 time:4 delay:30
2020-09-25 12:59:52.426 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:17 time:4 delay:30
2020-09-25 12:59:52.467 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:18 time:4 delay:30
2020-09-25 12:59:52.501 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:19 time:4 delay:30
2020-09-25 12:59:52.535 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:20 time:4 delay:30
2020-09-25 12:59:52.570 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:21 time:5 delay:30
2020-09-25 12:59:52.604 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:22 time:4 delay:30
2020-09-25 12:59:52.646 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:23 time:5 delay:30
2020-09-25 12:59:52.683 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:24 time:6 delay:30
2020-09-25 12:59:52.726 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:25 time:6 delay:30
2020-09-25 12:59:52.762 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:26 time:6 delay:30
2020-09-25 12:59:52.802 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:27 time:7 delay:30
2020-09-25 12:59:52.841 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:28 time:9 delay:30
2020-09-25 12:59:52.883 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:29 time:10 delay:30
2020-09-25 12:59:52.928 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:30 time:10 delay:30
2020-09-25 12:59:52.974 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:31 time:11 delay:30
2020-09-25 12:59:53.018 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:32 time:11 delay:30
2020-09-25 12:59:53.061 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:33 time:10 delay:30
2020-09-25 12:59:53.098 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:34 time:7 delay:30
2020-09-25 12:59:53.140 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:35 time:8 delay:30
2020-09-25 12:59:53.182 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:36 time:7 delay:30
2020-09-25 12:59:53.226 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:37 time:7 delay:30
2020-09-25 12:59:53.270 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:38 time:7 delay:30
2020-09-25 12:59:53.313 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:39 time:8 delay:30
2020-09-25 12:59:53.355 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:40 time:8 delay:30
2020-09-25 12:59:53.396 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:41 time:7 delay:30
2020-09-25 12:59:53.436 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:42 time:7 delay:30
2020-09-25 12:59:53.477 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:43 time:7 delay:30
2020-09-25 12:59:53.519 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:44 time:7 delay:30
2020-09-25 12:59:53.562 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:45 time:8 delay:30
2020-09-25 12:59:53.605 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:46 time:8 delay:30
2020-09-25 12:59:53.648 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:47 time:9 delay:30
2020-09-25 12:59:53.692 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:48 time:10 delay:30
2020-09-25 12:59:53.734 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:49 time:7 delay:30
2020-09-25 12:59:53.778 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:50 time:8 delay:30
2020-09-25 12:59:53.819 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:51 time:7 delay:30
2020-09-25 12:59:53.861 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:52 time:8 delay:30
2020-09-25 12:59:53.906 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:53 time:9 delay:30
2020-09-25 12:59:53.957 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:54 time:12 delay:30
2020-09-25 12:59:53.999 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:55 time:10 delay:30
2020-09-25 12:59:54.039 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:56 time:9 delay:30
2020-09-25 12:59:54.081 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:57 time:8 delay:30
2020-09-25 12:59:54.125 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:58 time:10 delay:30
2020-09-25 12:59:54.172 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:59 time:11 delay:30
2020-09-25 12:59:54.212 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:60 time:8 delay:30
2020-09-25 12:59:54.255 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:61 time:9 delay:30
2020-09-25 12:59:54.297 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:62 time:9 delay:30
2020-09-25 12:59:54.339 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:63 time:7 delay:30
2020-09-25 12:59:54.383 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:64 time:8 delay:30
2020-09-25 12:59:54.425 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:65 time:5 delay:30
2020-09-25 12:59:54.464 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:66 time:8 delay:30
2020-09-25 12:59:54.513 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:67 time:11 delay:30
2020-09-25 12:59:54.555 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:68 time:11 delay:30
2020-09-25 12:59:54.595 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:69 time:4 delay:30
2020-09-25 12:59:54.635 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:70 time:9 delay:30
2020-09-25 12:59:54.676 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:71 time:8 delay:30
2020-09-25 12:59:54.716 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:72 time:7 delay:30
2020-09-25 12:59:54.756 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:73 time:7 delay:30
2020-09-25 12:59:54.797 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:74 time:7 delay:30
2020-09-25 12:59:54.837 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:75 time:8 delay:30
2020-09-25 12:59:54.879 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:76 time:8 delay:30
2020-09-25 12:59:54.922 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:77 time:8 delay:30
2020-09-25 12:59:54.967 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:78 time:10 delay:30
2020-09-25 12:59:55.016 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:79 time:12 delay:30
2020-09-25 12:59:55.059 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:80 time:11 delay:30
2020-09-25 12:59:55.100 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:81 time:8 delay:30
2020-09-25 12:59:55.141 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:82 time:8 delay:30
2020-09-25 12:59:55.184 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:83 time:9 delay:30
2020-09-25 12:59:55.232 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:84 time:12 delay:30
2020-09-25 12:59:55.277 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:85 time:11 delay:30
2020-09-25 12:59:55.317 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:86 time:9 delay:30
2020-09-25 12:59:55.356 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:87 time:8 delay:30
2020-09-25 12:59:55.396 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:88 time:7 delay:30
2020-09-25 12:59:55.436 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:89 time:7 delay:30
2020-09-25 12:59:55.476 2367-2367/com.cz.android.gif.sample I/GlideGifView: id:258091065 frameIndex:90 time:7 delay:30
```

More details

```
loadFile time:27074 22959
frame:0 initialize previous. time:28212 15823
frame:0 decode code table. time:11597 10635
frame:0 process pixels. time:52 134
frame:1 initialize previous. time:17722 8156
frame:1 decode code table. time:1455 1142
frame:1 process pixels. time:18 23
frame:2 initialize previous. time:1796 2007
frame:2 decode code table. time:1384 1085
frame:2 process pixels. time:11 11
frame:3 initialize previous. time:12981 12890
frame:3 decode code table. time:968 589
frame:3 process pixels. time:22 12
frame:4 initialize previous. time:917 1456
frame:4 decode code table. time:1152 875
frame:4 process pixels. time:6 93
frame:5 initialize previous. time:1379 1783
frame:5 decode code table. time:1357 964
frame:5 process pixels. time:8 8
frame:6 initialize previous. time:1259 1063
frame:6 decode code table. time:773 1041
frame:6 process pixels. time:8 8
frame:7 initialize previous. time:1335 2035
frame:7 decode code table. time:1721 1162
frame:7 process pixels. time:12 9
frame:8 initialize previous. time:1455 1721
frame:8 decode code table. time:1447 1637
frame:8 process pixels. time:7 13
frame:9 initialize previous. time:2105 2305
frame:9 decode code table. time:1832 1310
frame:9 process pixels. time:11 9
frame:10 initialize previous. time:1611 2046
frame:10 decode code table. time:1751 972
frame:10 process pixels. time:9 14
frame:11 initialize previous. time:1285 2213
frame:11 decode code table. time:1900 1265
frame:11 process pixels. time:13 27
frame:12 initialize previous. time:1700 3384
frame:12 decode code table. time:2949 895
frame:12 process pixels. time:24 13
frame:13 initialize previous. time:1232 2589
frame:13 decode code table. time:2262 1031
frame:13 process pixels. time:9 12
frame:14 initialize previous. time:1325 2664
frame:14 decode code table. time:2367 1034
frame:14 process pixels. time:14 12
frame:15 initialize previous. time:1320 2996
frame:15 decode code table. time:2692 1063
frame:15 process pixels. time:16 16
frame:16 initialize previous. time:1423 3073
frame:16 decode code table. time:2734 1356
frame:16 process pixels. time:10 15
frame:17 initialize previous. time:1672 3251
frame:17 decode code table. time:2925 1363
frame:17 process pixels. time:22 83
frame:18 initialize previous. time:1770 1857
frame:18 decode code table. time:1513 1287
frame:18 process pixels. time:20 11
frame:19 initialize previous. time:1559 1849
frame:19 decode code table. time:1579 1471
frame:19 process pixels. time:10 17
frame:20 initialize previous. time:1747 1881
frame:20 decode code table. time:1611 1352
frame:20 process pixels. time:12 11
frame:21 initialize previous. time:1637 2093
frame:21 decode code table. time:1804 1485
frame:21 process pixels. time:13 14
frame:22 initialize previous. time:1766 2118
frame:22 decode code table. time:1830 1640
frame:22 process pixels. time:19 16
frame:23 initialize previous. time:2094 2444
frame:23 decode code table. time:1990 1720
frame:23 process pixels. time:17 13
frame:24 initialize previous. time:1996 2381
frame:24 decode code table. time:2094 1707
frame:24 process pixels. time:24 13
frame:25 initialize previous. time:2007 2602
frame:25 decode code table. time:2297 1944
frame:25 process pixels. time:17 16
frame:26 initialize previous. time:2229 2578
frame:26 decode code table. time:2288 2003
frame:26 process pixels. time:17 14
frame:27 initialize previous. time:2386 2546
frame:27 decode code table. time:2174 1991
frame:27 process pixels. time:11 12
frame:28 initialize previous. time:2267 2893
frame:28 decode code table. time:2614 2176
frame:28 process pixels. time:17 39
frame:29 initialize previous. time:2461 2900
frame:29 decode code table. time:2637 2222
frame:29 process pixels. time:16 14
frame:30 initialize previous. time:2507 3100
frame:30 decode code table. time:2816 2356
frame:30 process pixels. time:13 23
frame:31 initialize previous. time:2634 3180
frame:31 decode code table. time:2827 2650
frame:31 process pixels. time:21 22
frame:32 initialize previous. time:3082 3656
frame:32 decode code table. time:3305 2385
frame:32 process pixels. time:18 15
frame:33 initialize previous. time:2651 3465
frame:33 decode code table. time:3192 2447
frame:33 process pixels. time:22 17
frame:34 initialize previous. time:2734 3554
frame:34 decode code table. time:3263 2818
frame:34 process pixels. time:22 20
frame:35 initialize previous. time:3101 3465
frame:35 decode code table. time:3187 2666
frame:35 process pixels. time:15 16
frame:36 initialize previous. time:2979 3590
frame:36 decode code table. time:3265 2778
frame:36 process pixels. time:28 17
frame:37 initialize previous. time:3061 3412
frame:37 decode code table. time:3130 2632
frame:37 process pixels. time:17 15
frame:38 initialize previous. time:2902 3406
frame:38 decode code table. time:3130 2680
frame:38 process pixels. time:15 25
frame:39 initialize previous. time:2957 3327
frame:39 decode code table. time:3063 2650
frame:39 process pixels. time:18 19
frame:40 initialize previous. time:2925 3443
frame:40 decode code table. time:3163 2677
frame:40 process pixels. time:20 21
frame:41 initialize previous. time:3114 3690
frame:41 decode code table. time:3248 2805
frame:41 process pixels. time:25 26
frame:42 initialize previous. time:3129 3517
frame:42 decode code table. time:3196 2871
frame:42 process pixels. time:23 34
frame:43 initialize previous. time:3183 3404
frame:43 decode code table. time:3096 2845
frame:43 process pixels. time:16 40
frame:44 initialize previous. time:3158 3341
frame:44 decode code table. time:3065 2763
frame:44 process pixels. time:19 22
frame:45 initialize previous. time:3026 3424
frame:45 decode code table. time:3164 2684
frame:45 process pixels. time:18 17
frame:46 initialize previous. time:2960 3206
frame:46 decode code table. time:2938 2641
frame:46 process pixels. time:13 14
frame:47 initialize previous. time:2890 3200
frame:47 decode code table. time:2938 2733
frame:47 process pixels. time:26 22
frame:48 initialize previous. time:2997 3608
frame:48 decode code table. time:3341 2598
frame:48 process pixels. time:20 17
frame:49 initialize previous. time:2864 3389
frame:49 decode code table. time:3115 2980
frame:49 process pixels. time:20 25
frame:50 initialize previous. time:3278 3663
frame:50 decode code table. time:3373 2719
frame:50 process pixels. time:22 21
frame:51 initialize previous. time:3027 3438
frame:51 decode code table. time:3138 2884
frame:51 process pixels. time:16 18
frame:52 initialize previous. time:3161 3450
frame:52 decode code table. time:3169 2575
frame:52 process pixels. time:20 16
frame:53 initialize previous. time:2832 3553
frame:53 decode code table. time:3293 2576
frame:53 process pixels. time:20 23
frame:54 initialize previous. time:2838 3645
frame:54 decode code table. time:3390 2599
frame:54 process pixels. time:15 56
frame:55 initialize previous. time:2993 3191
frame:55 decode code table. time:2837 2551
frame:55 process pixels. time:17 32
frame:56 initialize previous. time:2951 3185
frame:56 decode code table. time:2805 2474
frame:56 process pixels. time:13 13
frame:57 initialize previous. time:2975 3789
frame:57 decode code table. time:3272 2731
frame:57 process pixels. time:25 25
frame:58 initialize previous. time:3249 4300
frame:58 decode code table. time:3783 2942
frame:58 process pixels. time:23 44
frame:59 initialize previous. time:3244 3221
frame:59 decode code table. time:2954 2591
frame:59 process pixels. time:14 15
frame:60 initialize previous. time:2844 3044
frame:60 decode code table. time:2781 2642
frame:60 process pixels. time:25 18
frame:61 initialize previous. time:2900 3035
frame:61 decode code table. time:2779 2573
frame:61 process pixels. time:16 17
frame:62 initialize previous. time:2823 2996
frame:62 decode code table. time:2750 2476
frame:62 process pixels. time:14 14
frame:63 initialize previous. time:2759 3093
frame:63 decode code table. time:2778 2561
frame:63 process pixels. time:16 17
frame:64 initialize previous. time:2846 3078
frame:64 decode code table. time:2826 2437
frame:64 process pixels. time:14 20
frame:65 initialize previous. time:2674 2858
frame:65 decode code table. time:2625 2378
frame:65 process pixels. time:16 21
frame:66 initialize previous. time:2696 3003
frame:66 decode code table. time:2692 2420
frame:66 process pixels. time:13 18
frame:67 initialize previous. time:2724 3432
frame:67 decode code table. time:3095 2665
frame:67 process pixels. time:34 16
frame:68 initialize previous. time:2933 2896
frame:68 decode code table. time:2642 2368
frame:68 process pixels. time:18 23
frame:69 initialize previous. time:2692 2962
frame:69 decode code table. time:2645 2276
frame:69 process pixels. time:17 14
frame:70 initialize previous. time:2552 3050
frame:70 decode code table. time:2774 2236
frame:70 process pixels. time:14 14
frame:71 initialize previous. time:2472 2770
frame:71 decode code table. time:2525 2209
frame:71 process pixels. time:24 15
frame:72 initialize previous. time:2435 2836
frame:72 decode code table. time:2612 2208
frame:72 process pixels. time:13 13
frame:73 initialize previous. time:2428 2738
frame:73 decode code table. time:2511 2181
frame:73 process pixels. time:14 13
frame:74 initialize previous. time:2395 2541
frame:74 decode code table. time:2335 2318
frame:74 process pixels. time:12 16
frame:75 initialize previous. time:2513 2620
frame:75 decode code table. time:2428 2261
frame:75 process pixels. time:11 13
frame:76 initialize previous. time:2464 2507
frame:76 decode code table. time:2302 2102
frame:76 process pixels. time:15 13
frame:77 initialize previous. time:2279 2671
frame:77 decode code table. time:2494 2282
frame:77 process pixels. time:14 17
frame:78 initialize previous. time:2487 2492
frame:78 decode code table. time:2294 2025
frame:78 process pixels. time:11 11
frame:79 initialize previous. time:2224 2298
frame:79 decode code table. time:2098 2201
frame:79 process pixels. time:12 17
frame:80 initialize previous. time:2408 2499
frame:80 decode code table. time:2295 2127
frame:80 process pixels. time:12 14
frame:81 initialize previous. time:2330 2409
frame:81 decode code table. time:2205 1987
frame:81 process pixels. time:14 13
frame:82 initialize previous. time:2172 2553
frame:82 decode code table. time:2359 1985
frame:82 process pixels. time:20 13
frame:83 initialize previous. time:2189 2618
frame:83 decode code table. time:2414 1986
frame:83 process pixels. time:13 12
frame:84 initialize previous. time:2205 2336
frame:84 decode code table. time:2111 2115
frame:84 process pixels. time:14 16
frame:85 initialize previous. time:2335 2244
frame:85 decode code table. time:2032 2117
frame:85 process pixels. time:13 13
frame:86 initialize previous. time:2321 2435
frame:86 decode code table. time:2231 1947
frame:86 process pixels. time:13 13
frame:87 initialize previous. time:2155 2312
frame:87 decode code table. time:2100 1980
frame:87 process pixels. time:17 15
frame:88 initialize previous. time:2238 2466
frame:88 decode code table. time:2208 2000
frame:88 process pixels. time:13 23
frame:89 initialize previous. time:2255 2145
frame:89 decode code table. time:1900 1804
frame:89 process pixels. time:14 14
```

Seem like decode code table is a little slow then I expected.