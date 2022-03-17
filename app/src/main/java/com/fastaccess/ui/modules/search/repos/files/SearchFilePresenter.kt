package com.fastaccess.ui.modules.search.repos.files

import android.os.Bundle
import com.fastaccess.R
import com.fastaccess.helper.AppHelper.hideKeyboard
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.InputHelper.toString
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.widgets.FontEditText

class SearchFilePresenter : BasePresenter<SearchFileMvp.View>(), SearchFileMvp.Presenter {
    @JvmField
    @com.evernote.android.state.State
    var repoId: String? = null

    @JvmField
    @com.evernote.android.state.State
    var login: String? = null

    override fun onSearchClicked(editText: FontEditText, inPath: Boolean) {
        val isEmpty = isEmpty(editText) || toString(editText).length < 2
        editText.error =
            if (isEmpty) editText.resources.getString(R.string.minimum_three_chars) else null
        if (!isEmpty) {
            hideKeyboard(editText)
            val query = toString(editText)
            if (view != null && isViewAttached) view!!.onValidSearchQuery(
                modifyQueryForFileSearch(
                    query,
                    inPath
                )
            )
        }
    }

    override fun onActivityCreated(extras: Bundle?) {
        repoId = extras!!.getString(BundleConstant.ID)
        login = extras.getString(BundleConstant.EXTRA)
    }

    private fun modifyQueryForFileSearch(query: String, inPath: Boolean): String {
        //restrict the search to file paths and the current repo user is looking at
        return query + "+" + "in:" + (if (inPath) "path" else "" + "file") + "+" + "repo:" + login + "/" + repoId
    }
}