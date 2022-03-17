package com.fastaccess.helper

import android.content.Context
import android.graphics.Typeface
import com.fastaccess.helper.TypeFaceHelper
import android.widget.TextView

/**
 * Created by Kosh on 17/12/15 10:25 PM
 */
object TypeFaceHelper {
    lateinit var typeface: Typeface

    @JvmStatic
    fun generateTypeface(context: Context) {
        typeface = Typeface.createFromAsset(context.assets, "fonts/app_font.ttf")
    }

    @JvmStatic
    fun applyTypeface(textView: TextView) {
        textView.typeface = typeface
    }
}