package com.fastaccess.ui.widgets.contributions

import com.fastaccess.helper.InputHelper.isEmpty

/**
 * Copyright 2016 Javier González
 * All right reserved.
 */
@Deprecated("github contributions html structural incompatibility")
class ContributionsProvider {
    fun getContributions(string: String?): List<ContributionsDay> {
        val contributions = ArrayList<ContributionsDay>()
        if (isEmpty(string)) return contributions
        var fillPos = -1
        var dataPos = -1
        var datePos = -1
        while (true) {
            fillPos = string!!.indexOf(FILL_STRING, fillPos + 1)
            dataPos = string.indexOf(DATA_STRING, dataPos + 1)
            datePos = string.indexOf(DATE_STRING, datePos + 1)
            if (fillPos == -1) break
            var level = 0
            when (string.substring(fillPos + FILL_STRING.length, fillPos + FILL_STRING.length + 7)) {
                "#eeeeee", "#ebedf0" -> level = 0
                "#d6e685", "#239a3b" -> level = 1
                "#8cc665", "#c6e48b" -> level = 2
                "#44a340", "#7bc96f" -> level = 3
                "#1e6823", "#196127" -> level = 4
            }
            val dataEndPos = string.indexOf("\"", dataPos + DATA_STRING.length)
            val dataString = string.substring(dataPos + DATA_STRING.length, dataEndPos)
            val data = dataString.toInt()
            val dateString =
                string.substring(datePos + DATE_STRING.length, datePos + DATE_STRING.length + 11)
            contributions.add(
                ContributionsDay(
                    dateString.substring(0, 4).toInt(),
                    dateString.substring(5, 7).toInt(),
                    dateString.substring(8, 10).toInt(),
                    level,
                    data
                )
            )
        }
        return contributions
    }

    companion object {
        private const val FILL_STRING = "fill=\""
        private const val DATA_STRING = "data-count=\""
        private const val DATE_STRING = "data-date=\""
    }
}