package com.fastaccess.ui.modules.search

import android.widget.AutoCompleteTextView
import androidx.viewpager.widget.ViewPager
import com.annimon.stream.Stream
import com.fastaccess.R
import com.fastaccess.data.dao.model.SearchHistory
import com.fastaccess.helper.AppHelper.hideKeyboard
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.InputHelper.toString
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.search.code.SearchCodeFragment
import com.fastaccess.ui.modules.search.issues.SearchIssuesFragment
import com.fastaccess.ui.modules.search.repos.SearchReposFragment
import com.fastaccess.ui.modules.search.users.SearchUsersFragment

/**
 * Created by Kosh on 08 Dec 2016, 8:20 PM
 */
class SearchPresenter : BasePresenter<SearchMvp.View>(), SearchMvp.Presenter {
    override val hints = ArrayList<SearchHistory>()
    override fun onAttachView(view: SearchMvp.View) {
        super.onAttachView(view)
        if (hints.isEmpty()) {
            manageDisposable(SearchHistory.getHistory()
                .subscribe { strings ->
                    hints.clear()
                    if (strings != null) hints.addAll(strings)
                    view.onNotifyAdapter(null)
                })
        }
    }

    override fun onSearchClicked(viewPager: ViewPager, editText: AutoCompleteTextView) {
        val isEmpty = isEmpty(editText) || toString(editText).length < 2
        editText.error =
            if (isEmpty) editText.resources.getString(R.string.minimum_three_chars) else null
        if (!isEmpty) {
            editText.dismissDropDown()
            hideKeyboard(editText)
            val query = toString(editText)
            val repos = viewPager.adapter!!
                .instantiateItem(viewPager, 0) as SearchReposFragment
            val users = viewPager.adapter!!
                .instantiateItem(viewPager, 1) as SearchUsersFragment
            val issues = viewPager.adapter!!
                .instantiateItem(viewPager, 2) as SearchIssuesFragment
            val code = viewPager.adapter!!.instantiateItem(viewPager, 3) as SearchCodeFragment
            repos.onQueueSearch(query)
            users.onQueueSearch(query)
            issues.onQueueSearch(query)
            code.onQueueSearch(query, true)
            val noneMatch = Stream.of(hints)
                .noneMatch { value: SearchHistory -> value.text.equals(query, ignoreCase = true) }
            if (noneMatch) {
                val searchHistory = SearchHistory(query)
                manageObservable(searchHistory.save(searchHistory).toObservable())
                sendToView { view ->
                    view.onNotifyAdapter(
                        SearchHistory(
                            query
                        )
                    )
                }
            }
        }
    }
}