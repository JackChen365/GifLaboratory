package com.cz.android.gif.sample.ui.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.gif.sample.R
import com.cz.android.sample.library.component.message.SampleMessage
import kotlinx.android.synthetic.main.activity_gif_performance_test.*
import java.io.File
import kotlin.concurrent.thread

class GifPerformanceTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_performance_test)
        thread {
            val imageFile1 = getTempImageFile("image/image3.gif")

            runOnUiThread {
                imageView1.loadImage(imageFile1)
                imageView2.loadImage(imageFile1)
                imageView3.loadImage(imageFile1)
                imageView4.loadImage(imageFile1)

                imageView1.setOnClickListener {
                    if(!imageView1.isRunning){
                        imageView1.start()
                    } else {
                        imageView1.stop()
                    }
                }
                imageView2.setOnClickListener {
                    if(!imageView2.isRunning){
                        imageView2.start()
                    } else {
                        imageView2.stop()
                    }
                }
                imageView3.setOnClickListener {
                    if(!imageView3.isRunning){
                        imageView3.start()
                    } else {
                        imageView3.stop()
                    }
                }
                imageView4.setOnClickListener {
                    if(!imageView4.isRunning){
                        imageView4.start()
                    } else {
                        imageView4.stop()
                    }
                }
                startButton.setOnClickListener {
                    imageView1.start()
                    imageView2.start()
                    imageView3.start()
                    imageView4.start()
                }
                stopButton.setOnClickListener {
                    imageView1.stop()
                    imageView2.stop()
                    imageView3.stop()
                    imageView4.stop()
                }
                startButton.performClick()
            }
        }
    }

    private fun getTempImageFile(assetFileName:String): File {
        val inputStream = assets.open(assetFileName)
        val readBytes = inputStream.readBytes()
        val file = File.createTempFile("tmp", ".gif")
        file.writeBytes(readBytes)
        file.deleteOnExit()
        return file
    }
}