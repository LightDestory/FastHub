package com.fastaccess.ui.modules.search.repos.files

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.appcompat.widget.AppCompatSpinner
import butterknife.*
import com.fastaccess.R
import com.fastaccess.helper.AnimHelper.animateVisibility
import com.fastaccess.helper.BundleConstant
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.modules.search.code.SearchCodeFragment
import com.fastaccess.ui.widgets.FontEditText
import com.fastaccess.ui.widgets.ForegroundImageView

class SearchFileActivity : BaseActivity<SearchFileMvp.View, SearchFilePresenter>(),
    SearchFileMvp.View {
    @JvmField
    @BindView(R.id.searchEditText)
    var searchEditText: FontEditText? = null

    @JvmField
    @BindView(R.id.clear)
    var clear: ForegroundImageView? = null

    @JvmField
    @BindView(R.id.searchOptions)
    var searchOptions: AppCompatSpinner? = null
    private var onSpinnerTouched = false
    private var searchCodeFragment: SearchCodeFragment? = null
    @OnTouch(R.id.searchOptions)
    fun onTouch(): Boolean {
        onSpinnerTouched = true
        return false
    }

    @OnItemSelected(R.id.searchOptions)
    fun onOptionSelected() {
        if (onSpinnerTouched) {
            onSearch()
        }
    }

    @OnTextChanged(
        value = [R.id.searchEditText],
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED
    )
    fun onTextChange(s: Editable) {
        val text = s.toString()
        if (text.isEmpty()) {
            animateVisibility(clear, false)
        } else {
            animateVisibility(clear, true)
        }
    }

    @OnEditorAction(R.id.searchEditText)
    fun onEditor(): Boolean {
        onSearch()
        return true
    }

    @OnClick(value = [R.id.clear])
    fun onClear(view: View) {
        if (view.id == R.id.clear) {
            searchEditText!!.setText("")
        }
    }

    @OnClick(R.id.search)
    fun onSearchClicked() {
        onSearch()
    }

    override fun layout(): Int {
        return R.layout.activity_search_file
    }

    override val isTransparent: Boolean = false

    override fun canBack(): Boolean {
        return true
    }

    override val isSecured: Boolean = false

    override fun providePresenter(): SearchFilePresenter {
        return SearchFilePresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter!!.onActivityCreated(intent.extras)
        searchCodeFragment =
            supportFragmentManager.findFragmentById(R.id.filesFragment) as SearchCodeFragment?
    }

    override fun onValidSearchQuery(query: String) {
        searchCodeFragment!!.onSetSearchQuery(query, false)
    }

    private fun onSearch() {
        presenter!!.onSearchClicked(searchEditText!!, searchOptions!!.selectedItemPosition == 0)
    }

    companion object {
        @JvmStatic
        fun createIntent(
            context: Context,
            login: String,
            repoId: String,
            isEnterprise: Boolean
        ): Intent {
            val intent = Intent(context, SearchFileActivity::class.java)
            intent.putExtra(BundleConstant.ID, repoId)
            intent.putExtra(BundleConstant.EXTRA, login)
            intent.putExtra(BundleConstant.IS_ENTERPRISE, isEnterprise)
            return intent
        }
    }
}