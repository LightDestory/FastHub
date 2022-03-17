package com.fastaccess.ui.modules.filter.issues

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.Toast
import butterknife.*
import com.evernote.android.state.State
import com.fastaccess.App
import com.fastaccess.R
import com.fastaccess.data.dao.LabelModel
import com.fastaccess.data.dao.MilestoneModel
import com.fastaccess.data.dao.model.User
import com.fastaccess.helper.*
import com.fastaccess.helper.AnimHelper.animateVisibility
import com.fastaccess.helper.AnimHelper.revealPopupWindow
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.InputHelper.toString
import com.fastaccess.provider.timeline.CommentsHelper.heart
import com.fastaccess.provider.timeline.CommentsHelper.hooray
import com.fastaccess.provider.timeline.CommentsHelper.laugh
import com.fastaccess.provider.timeline.CommentsHelper.sad
import com.fastaccess.provider.timeline.CommentsHelper.thumbsDown
import com.fastaccess.provider.timeline.CommentsHelper.thumbsUp
import com.fastaccess.ui.adapter.LabelsAdapter
import com.fastaccess.ui.adapter.MilestonesAdapter
import com.fastaccess.ui.adapter.SimpleListAdapter
import com.fastaccess.ui.adapter.UsersAdapter
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.modules.filter.issues.FilterIssuesActivity
import com.fastaccess.ui.modules.filter.issues.fragment.FilterIssueFragment
import com.fastaccess.ui.widgets.FontEditText
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView
import com.fastaccess.ui.widgets.SpannableBuilder.Companion.builder
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import es.dmoral.toasty.Toasty
import java.util.*

/**
 * Created by Kosh on 09 Apr 2017, 6:23 PM
 */
class FilterIssuesActivity :
    BaseActivity<FilterIssuesActivityMvp.View, FilterIssuesActivityPresenter>(),
    FilterIssuesActivityMvp.View {
    @JvmField
    @BindView(R.id.back)
    var back: ForegroundImageView? = null

    @JvmField
    @BindView(R.id.open)
    var open: FontTextView? = null

    @JvmField
    @BindView(R.id.close)
    var close: FontTextView? = null

    @JvmField
    @BindView(R.id.author)
    var author: FontTextView? = null

    @JvmField
    @BindView(R.id.labels)
    var labels: FontTextView? = null

    @JvmField
    @BindView(R.id.milestone)
    var milestone: FontTextView? = null

    @JvmField
    @BindView(R.id.assignee)
    var assignee: FontTextView? = null

    @JvmField
    @BindView(R.id.sort)
    var sort: FontTextView? = null

    @JvmField
    @BindView(R.id.searchEditText)
    var searchEditText: FontEditText? = null

    @JvmField
    @BindView(R.id.clear)
    var clear: View? = null

    @JvmField
    @State
    var isIssue = false

    @JvmField
    @State
    var isOpen = false

    @JvmField
    @State
    var login: String? = null

    @JvmField
    @State
    var repoId: String? = null

    @JvmField
    @State
    var criteria: String? = null
    private var filterFragment: FilterIssueFragment? = null
        get() {
            if (field == null) {
                field =
                    supportFragmentManager.findFragmentById(R.id.filterFragment) as FilterIssueFragment?
            }
            return field
        }
    private var milestonesAdapter: MilestonesAdapter? = null
    private var labelsAdapter: LabelsAdapter? = null
    private var assigneesAdapter: UsersAdapter? = null
    private var popupWindow: PopupWindow? = null
    override fun layout(): Int {
        return R.layout.filter_issues_prs_layout
    }

    override val isTransparent: Boolean
        get() = true

    override fun canBack(): Boolean {
        return true
    }

    override val isSecured: Boolean
        get() = false

    override fun providePresenter(): FilterIssuesActivityPresenter {
        return FilterIssuesActivityPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val bundle = intent.extras
            isIssue = bundle!!.getBoolean(BundleConstant.EXTRA_TWO)
            isOpen = bundle.getBoolean(BundleConstant.EXTRA_THREE)
            repoId = bundle.getString(BundleConstant.ID)
            login = bundle.getString(BundleConstant.EXTRA)
            criteria = bundle.getString(BundleConstant.EXTRA_FOUR)
            presenter!!.onStart(login!!, repoId!!)
            if (isOpen) {
                onOpenClicked()
            } else {
                onCloseClicked()
            }
        }
    }

    @OnClick(R.id.back)
    fun onBackClicked() {
        onBackPressed()
    }

    @OnClick(R.id.open)
    fun onOpenClicked() {
        if (!open!!.isSelected) {
            open!!.isSelected = true
            close!!.isSelected = false
            var text = toString(searchEditText)
            if (!isEmpty(text)) {
                text = text.replace("is:closed", "is:open")
                searchEditText!!.setText(text)
                onSearch()
            } else {
                searchEditText!!.setText(
                    String.format(
                        "%s %s ",
                        if (isOpen) "is:open" else "is:closed",
                        if (isIssue) "is:issue" else "is:pr"
                    )
                )
                if (!isEmpty(criteria)) {
                    searchEditText!!.setText(
                        String.format(
                            "%s%s",
                            toString(searchEditText),
                            criteria
                        )
                    )
                    criteria = null
                }
                onSearch()
            }
        }
    }

    @OnClick(R.id.close)
    fun onCloseClicked() {
        if (!close!!.isSelected) {
            open!!.isSelected = false
            close!!.isSelected = true
            var text = toString(searchEditText)
            if (!isEmpty(text)) {
                text = text.replace("is:open", "is:closed")
                searchEditText!!.setText(text)
                onSearch()
            } else {
                searchEditText!!.setText(
                    String.format(
                        "%s %s ",
                        if (isOpen) "is:open" else "is:closed",
                        if (isIssue) "is:issue" else "is:pr"
                    )
                )
                onSearch()
            }
        }
    }

    @OnClick(R.id.author)
    fun onAuthorClicked() {
        Toasty.info(
            App.getInstance(),
            "GitHub doesn't have this API yet!\nYou can try typing it yourself for example author:k0shk0sh",
            Toast.LENGTH_LONG
        ).show()
    }

    @SuppressLint("InflateParams")
    @OnClick(R.id.labels)
    fun onLabelsClicked() {
        if (labels!!.tag != null) return
        labels!!.tag = true
        val viewHolder =
            ViewHolder(LayoutInflater.from(this).inflate(R.layout.simple_list_dialog, null))
        setupPopupWindow(viewHolder)
        viewHolder.recycler!!.adapter = getLabelsAdapter()
        revealPopupWindow(popupWindow!!, labels!!)
    }

    @SuppressLint("InflateParams")
    @OnClick(R.id.milestone)
    fun onMilestoneClicked() {
        if (milestone!!.tag != null) return
        milestone!!.tag = true
        val viewHolder =
            ViewHolder(LayoutInflater.from(this).inflate(R.layout.simple_list_dialog, null))
        setupPopupWindow(viewHolder)
        viewHolder.recycler!!.adapter = getMilestonesAdapter()
        revealPopupWindow(popupWindow!!, milestone!!)
    }

    @SuppressLint("InflateParams")
    @OnClick(R.id.assignee)
    fun onAssigneeClicked() {
        if (assignee!!.tag != null) return
        assignee!!.tag = true
        val viewHolder =
            ViewHolder(LayoutInflater.from(this).inflate(R.layout.simple_list_dialog, null))
        setupPopupWindow(viewHolder)
        viewHolder.recycler!!.adapter = getAssigneesAdapter()
        revealPopupWindow(popupWindow!!, assignee!!)
    }

    @SuppressLint("InflateParams")
    @OnClick(R.id.sort)
    fun onSortClicked() {
        if (sort!!.tag != null) return
        sort!!.tag = true
        val viewHolder =
            ViewHolder(LayoutInflater.from(this).inflate(R.layout.simple_list_dialog, null))
        setupPopupWindow(viewHolder)
        val lists = ArrayList<String>()
        Collections.addAll(lists, *resources.getStringArray(R.array.sort_prs_issues))
        lists.add(thumbsUp)
        lists.add(thumbsDown)
        lists.add(laugh)
        lists.add(hooray)
        lists.add(sad)
        lists.add(heart)
        viewHolder.recycler!!.adapter = SimpleListAdapter(
            lists,
            object : BaseViewHolder.OnItemClickListener<String> {
                override fun onItemClick(position: Int, v: View?, item: String) {
                    appendSort(item)
                }

                override fun onItemLongClick(position: Int, v: View?, item: String) {}
            })
        revealPopupWindow(popupWindow!!, sort!!)
    }

    @OnClick(R.id.clear)
    fun onClear(view: View) {
        if (view.id == R.id.clear) {
            AppHelper.hideKeyboard(searchEditText!!)
            searchEditText!!.setText("")
        }
    }

    @OnClick(R.id.search)
    fun onSearchClicked() {
        onSearch()
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
        onSearchClicked()
        return true
    }

    override fun onSetCount(count: Int, isOpen: Boolean) {
        if (isOpen) {
            open!!.text = builder()
                .append(getString(R.string.open))
                .append("(")
                .append(count.toString())
                .append(")")
            close!!.setText(R.string.closed)
        } else {
            close!!.text = builder()
                .append(getString(R.string.closed))
                .append("(")
                .append(count.toString())
                .append(")")
            open!!.setText(R.string.open)
        }
    }

    // let users stay within selected repo context.
    private val repoName: String
        get() = "repo:$login/$repoId "

    override fun onBackPressed() {
        if (popupWindow != null && popupWindow!!.isShowing) {
            popupWindow!!.dismiss()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupPopupWindow(viewHolder: ViewHolder) {
        if (popupWindow == null) {
            popupWindow = PopupWindow(this)
            popupWindow!!.elevation = resources.getDimension(R.dimen.spacing_micro)
            popupWindow!!.isOutsideTouchable = true
            popupWindow!!.setBackgroundDrawable(ColorDrawable(ViewHelper.getWindowBackground(this)))
            popupWindow!!.elevation = resources.getDimension(R.dimen.spacing_normal)
            popupWindow!!.setOnDismissListener {
                Handler(Looper.getMainLooper()).postDelayed(
                    {

                        //hacky way to dismiss on re-selecting tab.
                        if (assignee == null || milestone == null || sort == null || labels == null) return@postDelayed
                        assignee!!.tag = null
                        milestone!!.tag = null
                        sort!!.tag = null
                        labels!!.tag = null
                    }, 100
                )
            }
        }
        popupWindow!!.contentView = viewHolder.view
    }

    private fun onSearch() {
        if (!isEmpty(searchEditText)) {
            filterFragment!!.onSearch(
                repoName + toString(searchEditText),
                open!!.isSelected, isIssue, isEnterprise
            )
            searchEditText!!.setSelection(searchEditText!!.editableText.length)
        } else {
            filterFragment!!.onClear()
            showErrorMessage(getString(R.string.empty_search_error))
        }
    }

    private fun getMilestonesAdapter(): MilestonesAdapter {
        if (milestonesAdapter == null) {
            if (presenter!!.milestones.isNotEmpty()) {
                val milestone = MilestoneModel()
                milestone.title = getString(R.string.clear)
                presenter!!.milestones.add(0, milestone)
            }
            milestonesAdapter = MilestonesAdapter(presenter!!.milestones)
            milestonesAdapter!!.listener =
                object : BaseViewHolder.OnItemClickListener<MilestoneModel> {
                    override fun onItemClick(position: Int, v: View?, item: MilestoneModel) {
                        appendMilestone(item)
                    }

                    override fun onItemLongClick(position: Int, v: View?, item: MilestoneModel) {}
                }
        }
        return milestonesAdapter!!
    }

    private fun getLabelsAdapter(): LabelsAdapter {
        if (labelsAdapter == null) {
            if (presenter!!.labels.isNotEmpty()) {
                val label = LabelModel()
                label.name = getString(R.string.clear)
                presenter!!.labels.add(0, label)
            }
            labelsAdapter = LabelsAdapter(presenter!!.labels, null)
            labelsAdapter!!.listener = object : BaseViewHolder.OnItemClickListener<LabelModel> {
                override fun onItemClick(position: Int, v: View?, item: LabelModel) {
                    appendLabel(item)
                }

                override fun onItemLongClick(position: Int, v: View?, item: LabelModel) {}
            }
        }
        return labelsAdapter!!
    }

    private fun getAssigneesAdapter(): UsersAdapter {
        if (assigneesAdapter == null) {
            if (presenter!!.assignees.isNotEmpty()) {
                val user = User()
                user.login = getString(R.string.clear)
                presenter!!.assignees.add(0, user)
            }
            assigneesAdapter = UsersAdapter(presenter!!.assignees, false, true)
            assigneesAdapter!!.listener = object : BaseViewHolder.OnItemClickListener<User> {
                override fun onItemClick(position: Int, v: View?, item: User) {
                    appendAssignee(item)
                }

                override fun onItemLongClick(position: Int, v: View?, item: User) {}
            }
        }
        return assigneesAdapter!!
    }

    private fun appendIfEmpty() {
        if (isEmpty(searchEditText)) when {
            open!!.isSelected -> {
                searchEditText!!.setText(if (isIssue) "is:issue is:open " else "is:pr is:open ")
            }
            close!!.isSelected -> {
                searchEditText!!.setText(if (isIssue) "is:issue is:close " else "is:pr is:close ")
            }
            else -> {
                searchEditText!!.setText(if (isIssue) "is:issue is:open " else "is:pr is:open ")
            }
        }
    }

    private fun appendMilestone(item: MilestoneModel) {
        if (popupWindow != null) {
            popupWindow!!.dismiss()
        }
        appendIfEmpty()
        var text = toString(searchEditText)
        val regex = "milestone:(\".+\"|\\S+)"
        if (item.title.equals(getString(R.string.clear), ignoreCase = true)) {
            text = text.replace(regex.toRegex(), "")
            searchEditText!!.setText(text)
            onSearch()
            return
        }
        if (!text.replace(regex.toRegex(), "milestone:\"" + item.title + "\"")
                .equals(text, ignoreCase = true)
        ) {
            val space = if (text.endsWith(" ")) "" else " "
            text = text.replace(regex.toRegex(), space + "milestone:\"" + item.title + "\"")
        } else {
            text += if (text.endsWith(" ")) "" else " "
            text += "milestone:\"" + item.title + "\""
        }
        searchEditText!!.setText(text)
        onSearch()
    }

    private fun appendLabel(item: LabelModel) {
        if (popupWindow != null) {
            popupWindow!!.dismiss()
        }
        appendIfEmpty()
        var text = toString(searchEditText)
        val regex = "label:(\".+\"|\\S+)"
        if (item.name.equals(getString(R.string.clear), ignoreCase = true)) {
            text = text.replace(regex.toRegex(), "")
            searchEditText!!.setText(text)
            onSearch()
            return
        }
        if (!text.replace(regex.toRegex(), "label:\"" + item.name + "\"")
                .equals(text, ignoreCase = true)
        ) {
            val space = if (text.endsWith(" ")) "" else " "
            text = text.replace(regex.toRegex(), space + "label:\"" + item.name + "\"")
        } else {
            text += if (text.endsWith(" ")) "" else " "
            text += "label:\"" + item.name + "\""
        }
        searchEditText!!.setText(text)
        onSearch()
    }

    private fun appendAssignee(item: User) {
        if (popupWindow != null) {
            popupWindow!!.dismiss()
        }
        appendIfEmpty()
        var text = toString(searchEditText)
        val regex = "assignee:(\".+\"|\\S+)"
        if (item.login.equals(getString(R.string.clear), ignoreCase = true)) {
            text = text.replace(regex.toRegex(), "")
            searchEditText!!.setText(text)
            onSearch()
            return
        }
        if (!text.replace(regex.toRegex(), "assignee:\"" + item.login + "\"")
                .equals(text, ignoreCase = true)
        ) {
            val space = if (text.endsWith(" ")) "" else " "
            text = text.replace(regex.toRegex(), space + "assignee:\"" + item.login + "\"")
        } else {
            text += if (text.endsWith(" ")) "" else " "
            text += "assignee:\"" + item.login + "\""
        }
        searchEditText!!.setText(text)
        onSearch()
    }

    private fun appendSort(item: String) {
        dismissPopup()
        appendIfEmpty()
        val resources = resources
        val regex = "sort:(\".+\"|\\S+)"
        val oldestQuery = "created-asc"
        val mostCommentedQuery = "comments-desc"
        val leastCommentedQuery = "comments-asc"
        val recentlyUpdatedQuery = "updated-desc"
        val leastRecentUpdatedQuery = "updated-asc"
        val sortThumbUp = "reactions-%2B1-desc"
        val sortThumbDown = "reactions--1-desc"
        val sortThumbLaugh = "reactions-smile-desc"
        val sortThumbHooray = "reactions-tada-desc"
        val sortThumbConfused = "reactions-thinking_face-desc"
        val sortThumbHeart = "reactions-heart-desc"
        var toQuery = ""
        var text = toString(searchEditText)
        if (item.equals(resources.getString(R.string.newest), ignoreCase = true)) {
            text = text.replace(regex.toRegex(), "")
            if (!toString(searchEditText).equals(text, ignoreCase = true)) {
                searchEditText!!.setText(text)
                onSearch()
            }
            return
        }
        when {
            item.equals(resources.getString(R.string.oldest), ignoreCase = true) -> {
                toQuery = oldestQuery
            }
            item.equals(resources.getString(R.string.most_commented), ignoreCase = true) -> {
                toQuery = mostCommentedQuery
            }
            item.equals(resources.getString(R.string.least_commented), ignoreCase = true) -> {
                toQuery = leastCommentedQuery
            }
            item.equals(resources.getString(R.string.recently_updated), ignoreCase = true) -> {
                toQuery = recentlyUpdatedQuery
            }
            item.equals(
                resources.getString(R.string.least_recent_updated),
                ignoreCase = true
            ) -> {
                toQuery = leastRecentUpdatedQuery
            }
            item.equals(thumbsUp, ignoreCase = true) -> {
                toQuery = sortThumbUp
            }
            item.equals(thumbsDown, ignoreCase = true) -> {
                toQuery = sortThumbDown
            }
            item.equals(laugh, ignoreCase = true) -> {
                toQuery = sortThumbLaugh
            }
            item.equals(hooray, ignoreCase = true) -> {
                toQuery = sortThumbHooray
            }
            item.equals(sad, ignoreCase = true) -> {
                toQuery = sortThumbConfused
            }
            item.equals(heart, ignoreCase = true) -> {
                toQuery = sortThumbHeart
            }
        }
        if (!text.replace(regex.toRegex(), "sort:\"$toQuery\"").equals(text, ignoreCase = true)) {
            val space = if (text.endsWith(" ")) "" else " "
            text = text.replace(regex.toRegex(), space + "sort:\"" + toQuery + "\"")
        } else {
            text += if (text.endsWith(" ")) "" else " "
            text += "sort:\"$toQuery\""
        }
        if (!toString(searchEditText).equals(text, ignoreCase = true)) {
            searchEditText!!.setText(text)
            onSearch()
        }
    }

    private fun dismissPopup() {
        if (popupWindow != null) {
            popupWindow!!.dismiss()
        }
    }

    internal class ViewHolder(var view: View) {
        @JvmField
        val title: FontTextView? = view.findViewById(R.id.title)

        @JvmField
        val recycler: DynamicRecyclerView? = view.findViewById(R.id.recycler)

        init {
            ButterKnife.bind(this, view)
            title!!.visibility = View.GONE
        }
    }

    companion object {
        fun getIntent(context: Context, login: String, repoId: String, criteria: String): Intent {
            val intent = Intent(context, FilterIssuesActivity::class.java)
            intent.putExtras(
                Bundler.start()
                    .put(BundleConstant.EXTRA, login)
                    .put(BundleConstant.ID, repoId)
                    .put(BundleConstant.EXTRA_FOUR, criteria)
                    .put(BundleConstant.EXTRA_TWO, true)
                    .put(BundleConstant.EXTRA_THREE, true)
                    .end()
            )
            return intent
        }

        fun startActivity(
            context: Activity, login: String, repoId: String,
            isIssue: Boolean, isOpen: Boolean, isEnterprise: Boolean
        ) {
            val intent = Intent(context, FilterIssuesActivity::class.java)
            intent.putExtras(
                Bundler.start()
                    .put(BundleConstant.EXTRA, login)
                    .put(BundleConstant.ID, repoId)
                    .put(BundleConstant.EXTRA_TWO, isIssue)
                    .put(BundleConstant.EXTRA_THREE, isOpen)
                    .put(BundleConstant.IS_ENTERPRISE, isEnterprise)
                    .end()
            )
            val view = context.findViewById<View>(R.id.fab)
            if (view != null) {
                ActivityHelper.startReveal(context, intent, view)
            } else {
                context.startActivity(intent)
            }
        }

        fun startActivity(
            view: View, login: String, repoId: String,
            isIssue: Boolean, isOpen: Boolean, isEnterprise: Boolean, criteria: String
        ) {
            val intent = Intent(view.context, FilterIssuesActivity::class.java)
            intent.putExtras(
                Bundler.start()
                    .put(BundleConstant.EXTRA, login)
                    .put(BundleConstant.ID, repoId)
                    .put(BundleConstant.EXTRA_TWO, isIssue)
                    .put(BundleConstant.EXTRA_THREE, isOpen)
                    .put(BundleConstant.IS_ENTERPRISE, isEnterprise)
                    .put(BundleConstant.EXTRA_FOUR, criteria)
                    .end()
            )
            ActivityHelper.startReveal(ActivityHelper.getActivity(view.context)!!, intent, view)
        }
    }
}