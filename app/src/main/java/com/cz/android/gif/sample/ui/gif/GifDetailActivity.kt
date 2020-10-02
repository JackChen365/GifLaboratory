package com.cz.android.gif.sample.ui.gif

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.gif.GifHeaderDecoder
import com.cz.android.gif.sample.R
import com.cz.android.sample.library.function.permission.PermissionViewModelProviders
import com.cz.android.sample.library.function.permission.SamplePermission
import com.cz.android.sample.library.provider.SampleContentProvider
import kotlinx.android.synthetic.main.activity_gif_detail.*
import java.io.File
import kotlin.concurrent.thread

@SamplePermission(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
class GifDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_detail)

        thread {
            val inputStream = assets.open("image/image3.gif")
            val readBytes = inputStream.readBytes()
            val file = File.createTempFile("tmp",".gif")
            file.writeBytes(readBytes)
            file.deleteOnExit()
            runOnUiThread {
                progressBar.animate().alpha(0f)
                //Just load the file. Don't worry about the performance. Cause we are faster enough.
                visualizeView.loadImage(file)
            }
        }

    }
}