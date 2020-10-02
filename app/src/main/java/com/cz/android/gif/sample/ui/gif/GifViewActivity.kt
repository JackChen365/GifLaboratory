package com.cz.android.gif.sample.ui.gif

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.gif.sample.R
import com.cz.android.sample.library.function.permission.SamplePermission
import kotlinx.android.synthetic.main.activity_gif_view.*
import kotlinx.android.synthetic.main.activity_gif_view.imageView1
import kotlinx.android.synthetic.main.activity_gif_view.imageView2
import kotlinx.android.synthetic.main.activity_gif_view.imageView3
import kotlinx.android.synthetic.main.activity_gif_view.imageView4
import kotlinx.android.synthetic.main.activity_gif_view.imageView5
import kotlinx.android.synthetic.main.activity_gif_view.imageView6
import kotlinx.android.synthetic.main.activity_gif_view.startButton
import kotlinx.android.synthetic.main.activity_gif_view.stopButton

import java.io.File
import kotlin.concurrent.thread

@SamplePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
class GifViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_view)
        thread {
            val imageFile1 = getTempImageFile("image/image_disposal_none.gif")
            val imageFile2 = getTempImageFile("image/image_disposal_background.gif")
            val imageFile3 = getTempImageFile("image/image_previous.gif")

            runOnUiThread {
                imageView1.loadImage(imageFile1)
                imageView2.loadImage(imageFile2)
                imageView3.loadImage(imageFile3)
                imageView4.loadImage(imageFile1)
                imageView5.loadImage(imageFile2)
                imageView6.loadImage(imageFile3)

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
                imageView5.setOnClickListener {
                    if(!imageView5.isRunning){
                        imageView5.start()
                    } else {
                        imageView5.stop()
                    }
                }
                imageView6.setOnClickListener {
                    if(!imageView6.isRunning){
                        imageView6.start()
                    } else {
                        imageView6.stop()
                    }
                }
                startButton.setOnClickListener {
                    imageView1.start()
                    imageView2.start()
                    imageView3.start()
                    imageView4.start()
                    imageView5.start()
                    imageView6.start()
                }
                stopButton.setOnClickListener {
                    imageView1.stop()
                    imageView2.stop()
                    imageView3.stop()
                    imageView4.stop()
                    imageView5.stop()
                    imageView6.stop()
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