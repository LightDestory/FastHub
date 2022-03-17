package com.fastaccess.ui.widgets.contributions

import android.content.Context
import com.fastaccess.ui.widgets.contributions.utils.DatesUtils.getWeekDayFromDate
import com.fastaccess.ui.widgets.contributions.utils.ColorsUtils.calculateLevelColor
import com.fastaccess.ui.widgets.contributions.utils.DatesUtils.isFirstDayOfWeek
import com.fastaccess.ui.widgets.contributions.utils.DatesUtils.isFirstWeekOfMount
import com.fastaccess.ui.widgets.contributions.utils.DatesUtils.getShortMonthName
import android.view.WindowManager
import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.Display
import android.view.View
import android.view.WindowInsets
import com.fastaccess.R
import java.lang.IllegalArgumentException
import java.lang.RuntimeException

/**
 * Copyright 2016 Javier González
 * All right reserved.
 */
class GitHubContributionsView : View {
    private var baseColor = Color.parseColor(BASE_COLOR)
    private var baseEmptyColor = Color.rgb(238, 238, 238)
    private var backgroundBaseColor = Color.TRANSPARENT
    private var textColor = Color.BLACK
    private var displayMonth = false
    private var lastWeeks = 53
    private var username: String? = null
    private var rect: Rect? = null
    private var monthTextPaint: Paint? = null
    private val mMatrix = Matrix()
    private val paint = Paint()
    private var blockPaint: Paint? = null
    private var bitmap: Bitmap? = null
    private var mHeight = 0
    private val point = Point()

    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr, 0)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val windowManage = getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // https://stackoverflow.com/questions/63719160/getsize-deprecated-in-api-level-30
            val windowMetrics = windowManage.currentWindowMetrics
            val windowInsets: WindowInsets = windowManage.currentWindowMetrics.windowInsets
            val insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout()
            )
            val insetsWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom
            val b = windowMetrics.bounds
            point.x = b.width() - insetsWidth
            point.y = b.height() - insetsHeight
        } else {
            windowManage.defaultDisplay.getSize(
                point
            )
        }

        val attributes = context.theme.obtainStyledAttributes(
            attrs, R.styleable.GitHubContributionsView, defStyleAttr, defStyleRes
        )
        initAttributes(attributes)
        rect = Rect()
        monthTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        blockPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        blockPaint!!.style = Paint.Style.FILL
    }

    private fun initAttributes(attributes: TypedArray) {
        baseColor = attributes.getColor(R.styleable.GitHubContributionsView_baseColor, baseColor)
        baseEmptyColor =
            attributes.getColor(R.styleable.GitHubContributionsView_baseEmptyColor, baseEmptyColor)
        backgroundBaseColor = attributes.getColor(
            R.styleable.GitHubContributionsView_backgroundBaseColor,
            backgroundBaseColor
        )
        textColor = attributes.getColor(R.styleable.GitHubContributionsView_textColor, textColor)
        displayMonth =
            attributes.getBoolean(R.styleable.GitHubContributionsView_displayMonth, displayMonth)
        lastWeeks = attributes.getInt(R.styleable.GitHubContributionsView_lastWeeks, lastWeeks)
        if (attributes.getString(R.styleable.GitHubContributionsView_username) != null) {
            username = attributes.getString(R.styleable.GitHubContributionsView_username)
            if (!isInEditMode) {
                loadUserName(username)
            }
        }
    }

    /**
     * Set a base color for blocks.
     * The tone depends on the number of contributions for a day.
     * Supported formats See [Color.parseColor]
     *
     * @param baseColor base color supported formats
     */
    fun setBaseColor(baseColor: String?) {
        try {
            this.baseColor = Color.parseColor(baseColor)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        invalidate()
    }

    /**
     * Set a base color for blocks.
     * The tone depends on the number of contributions for a day.
     *
     * @param color resource color
     */
    fun setBaseColor(color: Int) {
        baseColor = color
        invalidate()
    }

    /**
     * Set a base empty color for blocks without contributions.
     * Supported formats See [Color.parseColor]
     *
     * @param baseColor base color supported formats
     */
    fun setBaseEmptyColor(baseColor: String?) {
        try {
            baseEmptyColor = Color.parseColor(baseColor)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        invalidate()
    }

    /**
     * Set a base empty color for blocks without contributions.
     *
     * @param color resource color
     */
    fun setBaseEmptyColor(color: Int) {
        baseEmptyColor = color
        invalidate()
    }

    /**
     * Sets the background color for this contributions view.
     *
     * @param backgroundBaseColor the color of the background
     */
    fun setBackgroundBaseColor(backgroundBaseColor: String?) {
        try {
            this.backgroundBaseColor = Color.parseColor(backgroundBaseColor)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        invalidate()
    }

    /**
     * Sets the background color for this contributions view.
     *
     * @param backgroundBaseColor the color of the background
     */
    fun setBackgroundBaseColor(backgroundBaseColor: Int) {
        this.backgroundBaseColor = backgroundBaseColor
        invalidate()
    }

    /**
     * Set a text color for month names.
     * Supported formats See [Color.parseColor]
     *
     * @param textColor text color supported formats
     */
    fun setTextColor(textColor: String?) {
        try {
            this.textColor = Color.parseColor(textColor)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        invalidate()
    }

    /**
     * Set a text color for month names.
     *
     * @param textColor resource color
     */
    fun setTextColor(textColor: Int) {
        this.textColor = textColor
        invalidate()
    }

    /**
     * Set the number of weeks that you want to display.
     * You can set minimum 2 weeks but is not recommended. 1 week is impossible.
     * You can set maximum 53 weeks (1 year = 52.14 weeks).
     * By default is 53 (52 weeks and the current week).
     *
     * @param lastWeeks number of week (2..53)
     */
    fun setLastWeeks(lastWeeks: Int) {
        if (lastWeeks in 2..53) {
            this.lastWeeks = lastWeeks
            invalidate()
        } else {
            throw RuntimeException("The last weeks should be a number between 2 and 53")
        }
    }

    /**
     * Set if you want to see the name of the months
     * If you send true, the component height increase
     *
     * @param displayMonth true or false
     */
    fun displayMonth(displayMonth: Boolean) {
        this.displayMonth = displayMonth
        invalidate()
    }

    /**
     * Load and show contributions information for a user / organization
     *
     * @param username also, can be an organization
     */
    private fun loadUserName(username: String?) {
        this.username = username
        clearContribution()
    }

    /**
     * Clean de component.
     */
    private fun clearContribution() {
        bitmap = null
        invalidate()
    }

    fun onResponse() {
        adjustHeight(mHeight)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bitmap != null) {
            canvas.drawBitmap(bitmap!!, mMatrix, paint)
        } else {
            drawPlaceholder(canvas)
        }
    }

    fun drawOnCanvas(
        contributionsFilter: List<ContributionsDay>?,
        contributions: List<ContributionsDay>?
    ): Bitmap? {
        if (contributionsFilter == null || contributions == null || contributionsFilter.isEmpty() || contributions.isEmpty()) {
            return null
        }
        if (bitmap == null) {
            val padding = resources.getDimensionPixelSize(R.dimen.spacing_large)
            val width = point.x - padding
            val verticalBlockNumber = 7
            val horizontalBlockNumber =
                getHorizontalBlockNumber(contributionsFilter.size, verticalBlockNumber)
            val marginBlock = 1.0f - 0.1f
            val blockWidth = width / horizontalBlockNumber.toFloat() * marginBlock
            val spaceWidth = width / horizontalBlockNumber.toFloat() - blockWidth
            val topMargin: Float = if (displayMonth) 7f else 0F
            val monthTextHeight: Float = if (displayMonth) blockWidth * 1.5f else 0F
            val height = ((blockWidth + spaceWidth) * 7 + topMargin + monthTextHeight).toInt()
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas1 = Canvas(bitmap!!)
            // Background
            blockPaint!!.color = backgroundBaseColor
            canvas1.drawRect(
                0f,
                topMargin + monthTextHeight,
                width.toFloat(),
                height + monthTextHeight,
                blockPaint!!
            )
            monthTextPaint!!.color = textColor
            monthTextPaint!!.textSize = monthTextHeight
            // draw the blocks
            val currentWeekDay = getWeekDayFromDate(
                contributions[0].year,
                contributions[0].month,
                contributions[0].day
            )
            var x = 0f
            var y =
                (currentWeekDay - 7) % 7 * (blockWidth + spaceWidth) + (topMargin + monthTextHeight)
            for (day in contributionsFilter) {
                blockPaint!!.color = calculateLevelColor(baseColor, baseEmptyColor, day.level)
                canvas1.drawRect(x, y, x + blockWidth, y + blockWidth, blockPaint!!)
                if (isFirstDayOfWeek(day.year, day.month, day.day + 1)) {
                    // another column
                    x += blockWidth + spaceWidth
                    y = topMargin + monthTextHeight
                    if (isFirstWeekOfMount(day.year, day.month, day.day + 1)) {
                        canvas1.drawText(
                            getShortMonthName(day.year, day.month, day.day + 1), x, monthTextHeight,
                            monthTextPaint!!
                        )
                    }
                } else {
                    y += blockWidth + spaceWidth
                }
            }
            this.mHeight = height
        }
        return bitmap
    }

    private fun adjustHeight(height: Int) {
        val ll = layoutParams
        if (height != ll.height) {
            ll.height = height
            layoutParams = ll
        }
    }

    private fun drawPlaceholder(canvas: Canvas) {
        if (!isInEditMode) return
        canvas.getClipBounds(rect)
        val width = rect!!.width()
        val verticalBlockNumber = 7
        val horizontalBlockNumber = getHorizontalBlockNumber(lastWeeks * 7, verticalBlockNumber)
        val marginBlock = 1.0f - 0.1f
        val blockWidth = width / horizontalBlockNumber.toFloat() * marginBlock
        val spaceWidth = width / horizontalBlockNumber.toFloat() - blockWidth
        val monthTextHeight: Float = if (displayMonth) blockWidth * 1.5f else 0F
        val topMargin: Float = if (displayMonth) 7f else 0F
        monthTextPaint!!.textSize = monthTextHeight
        val height = ((blockWidth + spaceWidth) * 7 + topMargin + monthTextHeight).toInt()

        // Background
        blockPaint!!.color = backgroundBaseColor
        canvas.drawRect(
            0f,
            topMargin + monthTextHeight,
            width.toFloat(),
            height + monthTextHeight,
            blockPaint!!
        )
        var x = 0f
        var y = (0
                * (blockWidth + spaceWidth)
                + (topMargin + monthTextHeight))
        for (i in 1 until lastWeeks * 7 + 1) {
            blockPaint!!.color = calculateLevelColor(baseColor, baseEmptyColor, 0)
            canvas.drawRect(x, y, x + blockWidth, y + blockWidth, blockPaint!!)
            if (i % 7 == 0) {
                // another column
                x += blockWidth + spaceWidth
                y = topMargin + monthTextHeight
            } else {
                y += blockWidth + spaceWidth
            }
        }

        // Resize component
        val ll = layoutParams
        ll.height = height
        layoutParams = ll
    }

    private fun getHorizontalBlockNumber(total: Int, divider: Int): Int {
        val isInteger = total % divider == 0
        val result = total / divider
        return if (isInteger) result else result + 1
    }

    fun getLastContributions(contributions: List<ContributionsDay>): List<ContributionsDay> {
        val size = contributions.size
        val lastWeekDays = size % 7
        val lastDays = if (lastWeekDays > 0) lastWeekDays + (lastWeeks - 1) * 7 else lastWeeks * 7
        return if (size < lastDays) {
            // 不需要过滤
            contributions
        } else contributions.subList(size - lastDays, size)
    }

    companion object {
        private const val BASE_COLOR = "#D6E685" // default of Github
    }
}