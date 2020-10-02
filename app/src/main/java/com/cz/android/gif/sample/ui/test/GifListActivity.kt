package com.cz.android.gif.sample.ui.test

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.cz.android.gif.sample.R
import com.cz.android.sample.analysis.AbsAnalyzer
import com.cz.android.sample.library.analysis.HtmlSource
import com.cz.android.sample.library.function.permission.SamplePermission
import kotlinx.android.synthetic.main.activity_gif_list.*
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.thread

@SamplePermission(Manifest.permission.READ_EXTERNAL_STORAGE)
class GifListActivity : AppCompatActivity(){
    companion object{
        private const val TAG="GifListActivity"
    }

    private val httpClient = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_list)

//        downloadImageFiles()
        loadFromAssets()
        clearButton.setOnClickListener {
            for(f in filesDir.listFiles()){
                f.delete()
            }
        }
    }

    /**
     * Load image files from assets and download from internet.
     */
    private fun loadFromAssets() {
        thread {
            val imageList = assets.open("images.txt").reader().readLines()
//            imageList.forEachIndexed { index, url ->
//                val file = File(filesDir, url.hashCode().toString())
//                if(!file.exists()){
//                    downloadFile(url,file)
//                }
//                messageText.post {
//                    messageText.text="Download:${index+1}"
//                }
//            }
            runOnUiThread {
                progressBar.animate().alpha(0f)
                messageText.animate().alpha(0f)
                recyclerView.layoutManager =
                    StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
                //        recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.setHasFixedSize(false)
                recyclerView.adapter =
                    GifListAdapter(
                        this,
                        imageList
                    )
            }
        }
    }

    private fun downloadFile(url: String,file:File) {
        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()
        if (200 == response.code) {
            saveImageFile(response, file)
            Log.i(TAG, "url:$url not found!")
        } else {
            Log.e(TAG, "url:$url not found!")
        }
    }

    private fun saveImageFile(response: Response, file: File) {
        try {
            val body = response.body?.bytes()
            if (null != body && body.isNotEmpty()) {
                val fileInputStream = FileOutputStream(file)
                fileInputStream.write(body)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun downloadImageFiles() {
        thread {
            val imageAnalyzer =
                GifImageAnalyzer()
            imageAnalyzer.setDataSource(HtmlSource())
            val list = listOf(
                "http://www.gaoxiaogif.com/qqbiaoqing/dongtai/",
                "http://www.gaoxiaogif.com/tag/fengjing/",
                "http://www.gaoxiaogif.com/qqbiaoqing/liaotian/wasj/",
                "http://www.gaoxiaogif.com/qqbiaoqing/xinqing/bsws/",
                "http://www.gaoxiaogif.com/qqbiaoqing/liaotian/gzxx/",
                "http://www.gaoxiaogif.com/qqbiaoqing/mogutou/"
            )

            val imageList = mutableListOf<String>()
            list.forEach { url ->
                val list = imageAnalyzer.analysis(this, url)
                imageList.addAll(list)
            }
            runOnUiThread {
    //                recyclerView.layoutManager = StaggeredGridLayoutManager(2,LinearLayoutManager.VERTICAL)
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.setHasFixedSize(false)
                recyclerView.adapter =
                    GifListAdapter(
                        this,
                        imageList
                    )
            }
        }
    }

    class GifImageAnalyzer : AbsAnalyzer<String, String, List<String>>() {
        override fun analysisSource(params: String): List<String> {
            val imageList: MutableList<String> = ArrayList()
            val regex = "\"(https?://[^<>{}]*?\\.(gif))\""
            val compile = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
            val pattern = compile.matcher(params)
            while (pattern.find()) {
                val url = pattern.group(1)
                imageList.add(url)
            }
            return imageList
        }
    }
}