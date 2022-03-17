package com.fastaccess.helper

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import com.annimon.stream.IntStream
import com.fastaccess.R
import com.fastaccess.helper.InputHelper.isEmpty
import com.google.android.material.tabs.TabLayout
import java.util.*

/**
 * Created by kosh20111 on 10/7/2015 10:42 PM
 */
object ViewHelper {
    @ColorInt
    fun getPrimaryDarkColor(context: Context): Int {
        return getColorAttr(context, R.attr.colorPrimaryDark)
    }

    @JvmStatic
    @ColorInt
    fun getPrimaryColor(context: Context): Int {
        return getColorAttr(context, R.attr.colorPrimary)
    }

    @JvmStatic
    @ColorInt
    fun getPrimaryTextColor(context: Context): Int {
        return getColorAttr(context, android.R.attr.textColorPrimary)
    }

    @ColorInt
    fun getSecondaryTextColor(context: Context): Int {
        return getColorAttr(context, android.R.attr.textColorSecondary)
    }

    @ColorInt
    fun getTertiaryTextColor(context: Context): Int {
        return getColorAttr(context, android.R.attr.textColorTertiary)
    }

    @ColorInt
    fun getAccentColor(context: Context): Int {
        return getColorAttr(context, R.attr.colorAccent)
    }

    @ColorInt
    fun getIconColor(context: Context): Int {
        return getColorAttr(context, R.attr.icon_color)
    }

    @JvmStatic
    @ColorInt
    fun getWindowBackground(context: Context): Int {
        return getColorAttr(context, android.R.attr.windowBackground)
    }

    @ColorInt
    fun getListDivider(context: Context): Int {
        return getColorAttr(context, R.attr.dividerColor)
    }

    @JvmStatic
    @ColorInt
    fun getCardBackground(context: Context): Int {
        return getColorAttr(context, R.attr.card_background)
    }

    @JvmStatic
    @ColorInt
    fun getPatchAdditionColor(context: Context): Int {
        return getColorAttr(context, R.attr.patch_addition)
    }

    @JvmStatic
    @ColorInt
    fun getPatchDeletionColor(context: Context): Int {
        return getColorAttr(context, R.attr.patch_deletion)
    }

    @JvmStatic
    @ColorInt
    fun getPatchRefColor(context: Context): Int {
        return getColorAttr(context, R.attr.patch_ref)
    }

    @ColorInt
    private fun getColorAttr(context: Context, attr: Int): Int {
        val theme = context.theme
        val typedArray = theme.obtainStyledAttributes(intArrayOf(attr))
        val color = typedArray.getColor(0, Color.LTGRAY)
        typedArray.recycle()
        return color
    }

    fun toPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PX,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    @JvmStatic
    fun dpToPx(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }

    fun tintDrawable(drawable: Drawable, @ColorInt color: Int) {
        val filter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        drawable.mutate().colorFilter = filter
    }

    fun getDrawableSelector(normalColor: Int, pressedColor: Int): Drawable {
        return RippleDrawable(
            ColorStateList.valueOf(pressedColor),
            getRippleMask(normalColor),
            getRippleMask(normalColor)
        )
    }

    private fun getRippleMask(color: Int): Drawable {
        val outerRadii = FloatArray(8)
        Arrays.fill(outerRadii, 3f)
        val r = RoundRectShape(outerRadii, null, null)
        val shapeDrawable = ShapeDrawable(r)
        shapeDrawable.paint.color = color
        return shapeDrawable
    }

    private fun getStateListDrawable(normalColor: Int, pressedColor: Int): StateListDrawable {
        val states = StateListDrawable()
        states.addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(pressedColor))
        states.addState(intArrayOf(android.R.attr.state_focused), ColorDrawable(pressedColor))
        states.addState(intArrayOf(android.R.attr.state_activated), ColorDrawable(pressedColor))
        states.addState(intArrayOf(android.R.attr.state_selected), ColorDrawable(pressedColor))
        states.addState(intArrayOf(), ColorDrawable(normalColor))
        return states
    }

    fun textSelector(normalColor: Int, pressedColor: Int): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_activated),
                intArrayOf(android.R.attr.state_selected),
                intArrayOf()
            ), intArrayOf(
                pressedColor,
                pressedColor,
                pressedColor,
                pressedColor,
                normalColor
            )
        )
    }

    private fun isTablet(resources: Resources): Boolean {
        return resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    fun isTablet(context: Context): Boolean {
        return isTablet(context.resources)
    }

    fun isLandscape(resources: Resources): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    fun getLayoutPosition(view: View): Rect {
        val myViewRect = Rect()
        view.getGlobalVisibleRect(myViewRect)
        return myViewRect
    }

    @JvmStatic
    fun getTransitionName(view: View): String? {
        return if (!isEmpty(view.transitionName)) view.transitionName else null
    }

    @JvmOverloads
    fun showKeyboard(v: View, activity: Context = v.context) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(v, 0)
    }

    fun hideKeyboard(view: View) {
        val inputManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @JvmStatic
    @ColorInt
    fun generateTextColor(background: Int): Int {
        return getContrastColor(background)
    }

    @ColorInt
    private fun getContrastColor(@ColorInt color: Int): Int {
        val a =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return if (a < 0.5) Color.BLACK else Color.WHITE
    }

    fun isEllipsed(textView: TextView): Boolean {
        val layout = textView.layout
        if (layout != null) {
            val lines = layout.lineCount
            if (lines > 0) {
                return IntStream.range(0, lines)
                    .anyMatch { line: Int -> layout.getEllipsisCount(line) > 0 }
            }
        }
        return false
    }

    @JvmStatic
    fun getTabTextView(tabs: TabLayout, tabIndex: Int): TextView {
        return ((tabs.getChildAt(0) as LinearLayout).getChildAt(tabIndex) as LinearLayout).getChildAt(
            1
        ) as TextView
    }
}