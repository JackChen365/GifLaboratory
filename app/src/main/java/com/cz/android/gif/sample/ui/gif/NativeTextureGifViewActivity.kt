package com.cz.android.gif.sample.ui.gif

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.gif.sample.R
import kotlinx.android.synthetic.main.activity_native_gif.*
import kotlinx.android.synthetic.main.activity_native_texture_gif_view.*
import kotlinx.android.synthetic.main.activity_native_texture_gif_view.imageView
import kotlinx.android.synthetic.main.activity_native_texture_gif_view.startButton
import kotlinx.android.synthetic.main.activity_native_texture_gif_view.stopButton
import java.io.File
import kotlin.concurrent.thread

class NativeTextureGifViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_texture_gif_view)
        thread {
            val imageFile = getTempImageFile("image/image3.gif")
            runOnUiThread {
                imageView.loadImage(imageFile)
                startButton.setOnClickListener {
                    imageView.start()
                }
                stopButton.setOnClickListener {
                    imageView.stop()
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