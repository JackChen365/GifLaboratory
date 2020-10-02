package com.cz.android.gif.sample.ui.test

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.gif.sample.R
import com.cz.android.sample.library.component.message.SampleMessage
import kotlinx.android.synthetic.main.activity_bitmap_fill_test.*
import java.io.File
import java.util.*
import kotlin.concurrent.thread


@SampleMessage
class BitmapFillTestActivity : AppCompatActivity() {
    companion object {
        private const val TAG="BitmapFillTestActivity"
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("bitmap-native-lib")
        }
    }

    private external fun fillBitmap(bitmap: Bitmap,width:Int,height:Int,color:Int)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bitmap_fill_test)

        val width = 640
        val height = 640
        fillButton1.setOnClickListener {
            thread {
                val drawingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                for(times in 0 until 5){
                    val st = SystemClock.elapsedRealtime()
                    for (i in 0 until height) {
                        for (j in 0 until width) {
                            drawingBitmap.setPixel(j, i, Color.BLUE)
                        }
                    }
                    println("Fill Bitmap from Java1:"+times +" time:"+ (SystemClock.elapsedRealtime() - st))
                }
                runOnUiThread {
                    view.background=BitmapDrawable(resources,drawingBitmap)
                }
            }
        }

        fillButton2.setOnClickListener {
            val drawingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for(times in 0 until 5){
                val st = SystemClock.elapsedRealtime()
                val colorTable=IntArray(width*height)
                Arrays.fill(colorTable,Color.RED)
                drawingBitmap.setPixels(colorTable,0,width,0,0,width,height);
                println("Fill Bitmap from Java2:"+times +" time:"+ (SystemClock.elapsedRealtime() - st))
            }
            runOnUiThread {
                view.background=BitmapDrawable(resources,drawingBitmap)
            }
        }
//        val file = getTempImageFile("image/image4.gif")
        fillButton3.setOnClickListener {
//            testImage(file)
//            testColorTable(file)
            thread {
                val drawingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                for(times in 0 until 5){
                    val st = SystemClock.elapsedRealtime()
                    fillBitmap(drawingBitmap,width,height,-36470)
                    println("Fill Bitmap from NDK:"+times+" time:" + (SystemClock.elapsedRealtime() - st))
                }
                runOnUiThread {
                    view.background=BitmapDrawable(resources,drawingBitmap)
                }
            }
        }
    }
}