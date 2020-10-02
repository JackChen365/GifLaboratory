package com.cz.android.gif.sample.ui.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.gif.sample.R
import kotlinx.android.synthetic.main.activity_byte_text_view.*
import java.io.File
import java.io.InputStream
import java.lang.StringBuilder

class ByteTextViewActivity : AppCompatActivity() {
    companion object{
        fun startActivity(context: Context,title:String, path: String, start:Long, end:Long){
            val intent = Intent(context, ByteTextViewActivity::class.java)
            intent.putExtra("file", path)
            intent.putExtra("title", title)
            intent.putExtra("start",start)
            intent.putExtra("end",end)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_byte_text_view)
        val filePath = intent.getStringExtra("file")
        val start = intent.getLongExtra("start", 0)
        val end = intent.getLongExtra("end", 0)
        if(null!=filePath){
            val file = File(filePath)
            textReaderLayout.setStartOffset(start.toInt())
            textReaderLayout.setEndOffset(end.toInt())
            textReaderLayout.loadFile(file)
        } else {
            //show demonstrate data.
            val tempImageFile = getTempImageFile("image/interlace.gif")
            textReaderLayout.setStartOffset(start.toInt())
            textReaderLayout.setEndOffset(end.toInt())
            textReaderLayout.loadFile(tempImageFile)
        }

        previousButton.setOnClickListener {
            textReaderLayout.previousPage()
            updateStateBarInformation()
            seekBar.progress=textReaderLayout.currentPage
        }

        nextButton.setOnClickListener {
            textReaderLayout.nextPage()
            updateStateBarInformation()
            seekBar.progress=textReaderLayout.currentPage
        }
        textReaderLayout.post {
            updateSeekBar()
            updateStateBarInformation()
            updateView()
        }
    }

    private fun updateSeekBar() {
        val pageSize = textReaderLayout.pageSize
        val currentPage = textReaderLayout.currentPage
        seekBar.max=pageSize-1
        seekBar.progress=currentPage
        seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    textReaderLayout.currentPage = progress
                    updateStateBarInformation()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun updateStateBarInformation(){
        val startOffset = textReaderLayout.startOffset
        val endOffset = textReaderLayout.endOffset
        val pageByteSize = textReaderLayout.pageByteSize
        val pageSize = textReaderLayout.pageSize
        val currentPage = textReaderLayout.currentPage
        stateBarTextView.setText("TextRange:$startOffset-$endOffset PageByteSize:$pageByteSize Total:$pageSize page:${currentPage+1}")
    }

    private fun updateView() {
        val pageSize = textReaderLayout.pageSize
        if(1 == pageSize){
            seekBar.visibility= View.GONE
            previousButton.visibility=View.GONE
            nextButton.visibility=View.GONE
        } else {
            seekBar.visibility= View.VISIBLE
            previousButton.visibility=View.VISIBLE
            nextButton.visibility=View.VISIBLE
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