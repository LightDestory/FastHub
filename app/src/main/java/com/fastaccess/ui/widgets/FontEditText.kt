package com.fastaccess.ui.widgets

import androidx.appcompat.widget.AppCompatEditText
import android.view.inputmethod.EditorInfo
import com.fastaccess.helper.TypeFaceHelper
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import java.lang.Exception

/**
 * Created by Kosh on 8/18/2015. copyrights are reserved
 */
open class FontEditText : AppCompatEditText {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        if (isInEditMode) return
        inputType =
            inputType or EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_FLAG_NO_FULLSCREEN
        imeOptions = imeOptions or EditorInfo.IME_FLAG_NO_FULLSCREEN
        TypeFaceHelper.applyTypeface(this)
    }

    @SuppressLint("SetTextI18n")
    override fun setText(text: CharSequence, type: BufferType) {
        try {
            super.setText(text, type)
        } catch (e: Exception) {
            setText(
                """
    I tried, but your OEM just sucks because they modify the framework components and therefore causing the app to crash!.
    FastHub
    """.trimIndent()
            )
        }
    }
}