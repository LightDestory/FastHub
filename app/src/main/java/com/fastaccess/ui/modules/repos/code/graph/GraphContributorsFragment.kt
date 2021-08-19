package com.fastaccess.ui.modules.repos.code.graph

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.helper.Bundler
import com.fastaccess.provider.rest.RestProvider
import com.fastaccess.ui.base.BaseDialogFragment
import com.fastaccess.ui.base.mvp.BaseMvp
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import java.util.*

class GraphContributorsFragment : BaseDialogFragment<BaseMvp.FAView, BasePresenter<BaseMvp.FAView>>() {
    @BindView(R.id.toolbar) lateinit var toolbar: Toolbar
    @BindView(R.id.swipeRefreshLayout) lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.graphView) lateinit var graphView: GraphView
    @BindView(R.id.titleView) lateinit var titleView: TextView

    private lateinit var viewModel: GraphContributorsViewModel

    override fun providePresenter(): BasePresenter<BaseMvp.FAView> = BasePresenter()

    override fun fragmentLayout(): Int = R.layout.dialog_contributors_graph

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        val factory = GraphContributorsViewModelFactory(
                RestProvider.getRepoService(isEnterprise),
                arguments?.getString("OwnerName")!!,
                arguments?.getString("RepoName")!!
        )
        viewModel = ViewModelProvider(viewModelStore, factory).get(GraphContributorsViewModel::class.java)

        toolbar.setNavigationIcon(R.drawable.ic_clear)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.title = getString(R.string.contributions)

        swipeRefreshLayout.isEnabled = false
        swipeRefreshLayout.isRefreshing = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val filterByLogin = arguments?.getString("FilterOwner")
        viewLifecycleOwner.lifecycle.coroutineScope.launchWhenStarted {
            viewModel.contributions.filterNotNull().collect { stateModel ->
                if (filterByLogin != null) {
                    val weeks = stateModel.items.first { it.author.login == filterByLogin }.weeks
                    val firstDate = Calendar.getInstance().apply {
                        time = Date(weeks.first().w * 1000)
                    }
                    val lastDate = Calendar.getInstance().apply {
                        time = Date(weeks.last().w * 1000)
                    }
                    titleView.text = "${getDateString(firstDate)} - ${getDateString(lastDate)}"
                    graphView.graphData = weeks
                    graphView.visibility = View.VISIBLE
                } else {
                    // TODO: Add support for displaying full
                }
                swipeRefreshLayout.isRefreshing = false
            }
        }

        viewLifecycleOwner.lifecycle.coroutineScope.launchWhenStarted {
            viewModel.error.filterNotNull().collect {
                Toasty.error(requireContext(), it).show()
            }
        }
    }

    private fun getDateString(calendar: Calendar): String {
       return "${calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ROOT)} ${calendar.get(Calendar.DAY_OF_MONTH)} ${calendar.get(Calendar.YEAR)}"
    }

    companion object {
        @JvmStatic @JvmOverloads
        fun newInstance(owner: String, repo: String, filterByLogin: String? = null): GraphContributorsFragment {
            val fragment = GraphContributorsFragment()
            fragment.arguments = Bundler.start()
                    .put("OwnerName", owner)
                    .put("RepoName", repo)
                    .put("FilterOwner", filterByLogin)
                    .end()
            return fragment
        }
    }
}