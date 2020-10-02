package com.cz.android.gif.sample.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cz.android.gif.sample.R
import com.cz.android.sample.library.data.DataManager
import com.cz.android.sample.library.data.DataProvider
import kotlinx.android.synthetic.main.activity_grid_layout.*

class GridLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid_layout)
        val dataProvider = DataManager.getDataProvider(this)
        val layoutInflater = LayoutInflater.from(this)
        val redColorArray = dataProvider.getColorArray(DataProvider.COLOR_RED)
        for (color in redColorArray) {
            val layout = layoutInflater.inflate(R.layout.gif_color_layout, colorTable, false)
            layout.setBackgroundColor(color)
            val colorText1 = layout.findViewById<TextView>(R.id.colorText1)
            val colorText2 = layout.findViewById<TextView>(R.id.colorText2)
            colorText1.text = color.toString()
            colorText2.text = Integer.toHexString(color).padStart(6,'F').toUpperCase()
            colorTable.addView(layout)
        }
        val orangeColorArray = dataProvider.getColorArray(DataProvider.COLOR_ORANGE)
        for (color in orangeColorArray) {
            val layout = layoutInflater.inflate(R.layout.gif_color_layout, colorTable, false)
            layout.setBackgroundColor(color)
            val colorText1 = layout.findViewById<TextView>(R.id.colorText1)
            val colorText2 = layout.findViewById<TextView>(R.id.colorText2)
            colorText1.text = color.toString()
            colorText2.text = Integer.toHexString(color).padStart(6,'F').toUpperCase()
            colorTable.addView(layout)
        }
    }
}