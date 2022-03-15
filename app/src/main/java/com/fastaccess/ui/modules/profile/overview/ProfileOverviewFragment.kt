package com.fastaccess.ui.modules.profile.overview

import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.R
import com.fastaccess.ui.widgets.FontTextView
import android.widget.LinearLayout
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontButton
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import com.fastaccess.ui.widgets.contributions.GitHubContributionsView
import com.fastaccess.ui.modules.profile.ProfilePagerMvp
import butterknife.OnClick
import android.os.Bundle
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.fastaccess.provider.emoji.EmojiParser
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.fastaccess.ui.widgets.SpannableBuilder
import com.fastaccess.ui.adapter.ProfileOrgsAdapter
import com.fastaccess.ui.widgets.recyclerview.layout_manager.GridManager
import com.fastaccess.ui.adapter.ProfilePinnedReposAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.provider.scheme.SchemeParser
import androidx.annotation.StringRes
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.evernote.android.state.State
import com.fastaccess.data.dao.model.Login
import com.fastaccess.data.dao.model.User
import com.fastaccess.github.GetPinnedReposQuery
import com.fastaccess.helper.*
import com.prettifier.pretty.PrettifyWebView

/**
 * Created by Kosh on 03 Dec 2016, 9:16 AM
 */
class ProfileOverviewFragment : BaseFragment<ProfileOverviewMvp.View, ProfileOverviewPresenter>(),
    ProfileOverviewMvp.View {

    var contributionsCaption: FontTextView? = null

    var organizationsCaption: FontTextView? = null

    var userInformation: LinearLayout? = null

    var username: FontTextView? = null

    var fullname: FontTextView? = null

    var description: FontTextView? = null

    var avatarLayout: AvatarLayout? = null

    var organization: FontTextView? = null

    var location: FontTextView? = null

    var email: FontTextView? = null

    var link: FontTextView? = null

    var joined: FontTextView? = null

    var following: FontButton? = null

    var followers: FontButton? = null

    var progress: View? = null

    var followBtn: Button? = null

    var orgsList: DynamicRecyclerView? = null

    var orgsCard: CardView? = null

    var parentView: NestedScrollView? = null

    var contributionView: GitHubContributionsView? = null

    var contributionCard: CardView? = null

    var pinnedReposTextView: FontTextView? = null

    var pinnedList: DynamicRecyclerView? = null

    var pinnedReposCard: CardView? = null

    var readmeWebView: PrettifyWebView? = null

    @State
    var userModel: User? = null
    private var profileCallback: ProfilePagerMvp.View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        this.contributionsCaption = view.findViewById(R.id.contributionsCaption)
        this.organizationsCaption = view.findViewById(R.id.organizationsCaption)
        this.userInformation = view.findViewById(R.id.userInformation)
        this.username = view.findViewById(R.id.username)
        this.fullname = view.findViewById(R.id.fullname)
        this.description = view.findViewById(R.id.description)
        this.avatarLayout = view.findViewById(R.id.avatarLayout)
        this.organization = view.findViewById(R.id.organization)
        this.location = view.findViewById(R.id.location)
        this.email = view.findViewById(R.id.email)
        this.link = view.findViewById(R.id.link)
        this.joined = view.findViewById(R.id.joined)
        this.following = view.findViewById(R.id.following)
        this.followers = view.findViewById(R.id.followers)
        this.progress = view.findViewById(R.id.progress)
        this.followBtn = view.findViewById(R.id.followBtn)
        this.orgsList = view.findViewById(R.id.orgsList)
        this.orgsCard = view.findViewById(R.id.orgsCard)
        this.parentView = view.findViewById(R.id.parentView)
        this.contributionView = view.findViewById(R.id.contributionView)
        this.contributionCard = view.findViewById(R.id.contributionCard)
        this.pinnedReposTextView = view.findViewById(R.id.pinnedReposTextView)
        this.pinnedList = view.findViewById(R.id.pinnedList)
        this.pinnedReposCard = view.findViewById(R.id.pinnedReposCard)
        this.readmeWebView = view.findViewById(R.id.readmeWebView)
        return view
    }

    @OnClick(R.id.following, R.id.followers, R.id.followBtn)
    fun onClick(view: View) {
        when (view.id) {
            R.id.followers -> {
                profileCallback!!.onNavigateToFollowers()
            }
            R.id.following -> {
                profileCallback!!.onNavigateToFollowing()
            }
            R.id.followBtn -> {
                presenter!!.onFollowButtonClicked(presenter!!.login!!)
                followBtn!!.isEnabled = false
            }
        }
    }

    @OnClick(R.id.userInformation)
    fun onOpenAvatar() {
        if (userModel != null) ActivityHelper.startCustomTab(
            requireActivity(),
            userModel!!.avatarUrl
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        profileCallback = if (parentFragment is ProfilePagerMvp.View) {
            parentFragment as ProfilePagerMvp.View?
        } else {
            context as ProfilePagerMvp.View
        }
    }

    override fun onDetach() {
        profileCallback = null
        super.onDetach()
    }

    override fun fragmentLayout(): Int {
        return R.layout.profile_overview_layout
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        onInitOrgs(presenter!!.orgs)
        onInitPinnedRepos(presenter!!.nodes)
        if (savedInstanceState == null) {
            presenter!!.onFragmentCreated(arguments)
        } else {
            if (userModel != null) {
                invalidateFollowBtn()
                onInitViews(userModel)
            } else {
                presenter!!.onFragmentCreated(arguments)
            }
        }
        if (isMeOrOrganization) {
            followBtn!!.visibility = View.GONE
        }
    }

    override fun providePresenter(): ProfileOverviewPresenter {
        return ProfileOverviewPresenter()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onInitViews(userModel: User?) {
        progress!!.visibility = View.GONE
        userModel ?: return
        if (profileCallback != null) profileCallback!!.onCheckType(userModel.isOrganizationType)
        if (view != null) {
            if (this.userModel == null) {
                TransitionManager.beginDelayedTransition(
                    (view as ViewGroup?)!!,
                    AutoTransition().addListener(object : Transition.TransitionListener {
                        override fun onTransitionStart(transition: Transition) {}
                        override fun onTransitionEnd(transition: Transition) {
                            if (contributionView != null) presenter!!.onLoadContributionWidget(
                                contributionView!!
                            )
                        }

                        override fun onTransitionCancel(transition: Transition) {}
                        override fun onTransitionPause(transition: Transition) {}
                        override fun onTransitionResume(transition: Transition) {}
                    })
                )
            } else {
                presenter!!.onLoadContributionWidget(contributionView!!)
            }
        }
        this.userModel = userModel
        followBtn!!.visibility = if (!isMeOrOrganization) View.VISIBLE else View.GONE
        if (userModel.login != null) {
            username!!.text = userModel.login
        }
        if (userModel.name != null) {
            fullname!!.text = userModel.name
        }
        if (userModel.bio != null) {
            description!!.text = EmojiParser.parseToUnicode(userModel.bio)
        } else {
            description!!.visibility = View.GONE
        }
        avatarLayout!!.setUrl(userModel.avatarUrl, null, false, false, true)
        avatarLayout!!.findViewById<View>(R.id.avatar)
            .setOnTouchListener { _: View?, event: MotionEvent ->
                if (event.action == MotionEvent.ACTION_UP) {
                    ActivityHelper.startCustomTab(requireActivity(), userModel.avatarUrl)
                    return@setOnTouchListener true
                }
                false
            }
        if (InputHelper.isEmpty(userModel.company)) {
            organization!!.visibility = View.GONE
        } else {
            organization!!.text = userModel.company
            organization!!.visibility = View.VISIBLE
        }
        if (InputHelper.isEmpty(userModel.location)) {
            location!!.visibility = View.GONE
        } else {
            location!!.text = userModel.location
            location!!.visibility = View.VISIBLE
        }
        if (InputHelper.isEmpty(userModel.email)) {
            email!!.visibility = View.GONE
        } else {
            email!!.text = userModel.email
            email!!.visibility = View.VISIBLE
        }
        if (InputHelper.isEmpty(userModel.blog)) {
            link!!.visibility = View.GONE
        } else {
            link!!.text = userModel.blog!!
            link!!.visibility = View.VISIBLE
        }
        if (InputHelper.isEmpty(userModel.createdAt)) {
            joined!!.visibility = View.GONE
        } else {
            joined!!.text = ParseDateFormat.getTimeAgo(userModel.createdAt!!)
            joined!!.visibility = View.VISIBLE
        }
        followers!!.text = SpannableBuilder.builder()
            .append(getString(R.string.followers))
            .append(" (")
            .bold(userModel.followers.toString())
            .append(")")
        following!!.text = SpannableBuilder.builder()
            .append(getString(R.string.following))
            .append(" (")
            .bold(userModel.following.toString())
            .append(")")
    }

    override fun invalidateFollowBtn() {
        hideProgress()
        if (isMeOrOrganization) return
        if (presenter!!.isSuccessResponse) {
            followBtn!!.isEnabled = true
            followBtn!!.isActivated = presenter!!.isFollowing
            followBtn!!.text =
                if (presenter!!.isFollowing) getString(R.string.unfollow) else getString(R.string.follow)
        }
    }

    override fun onInitContributions(show: Boolean) {
        if (contributionView == null) return
        if (show) {
            contributionView!!.onResponse()
        }
        contributionCard!!.visibility = if (show) View.VISIBLE else View.GONE
        contributionsCaption!!.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    override fun onInitOrgs(orgs: List<User>) {
        if (orgs.isNotEmpty()) {
            orgsList!!.isNestedScrollingEnabled = false
            val adapter = ProfileOrgsAdapter()
            adapter.addItems(orgs)
            orgsList!!.adapter = adapter
            orgsCard!!.visibility = View.VISIBLE
            organizationsCaption!!.visibility = View.VISIBLE
            (orgsList!!.layoutManager as GridManager?)!!.iconSize =
                resources.getDimensionPixelSize(R.dimen.header_icon_zie) + resources
                    .getDimensionPixelSize(R.dimen.spacing_xs_large)
        } else {
            organizationsCaption!!.visibility = View.GONE
            orgsCard!!.visibility = View.GONE
        }
    }

    override fun onUserNotFound() {
        showMessage(R.string.error, R.string.no_user_found)
    }

    override fun onInitPinnedRepos(nodes: List<GetPinnedReposQuery.Node>) {
        if (pinnedReposTextView == null) return
        if (nodes.isNotEmpty()) {
            pinnedReposTextView!!.visibility = View.VISIBLE
            pinnedReposCard!!.visibility = View.VISIBLE
            val adapter = ProfilePinnedReposAdapter(nodes.toMutableList())
            adapter.listener =
                object : BaseViewHolder.OnItemClickListener<GetPinnedReposQuery.Node> {
                    override fun onItemClick(
                        position: Int,
                        v: View?,
                        item: GetPinnedReposQuery.Node
                    ) {
                        SchemeParser.launchUri(requireContext(), item.onRepository!!.url.toString())
                    }

                    override fun onItemLongClick(
                        position: Int,
                        v: View?,
                        item: GetPinnedReposQuery.Node
                    ) {
                    }
                }
            pinnedList!!.addDivider()
            pinnedList!!.adapter = adapter
        } else {
            pinnedReposTextView!!.visibility = View.GONE
            pinnedReposCard!!.visibility = View.GONE
        }
    }

    override fun onSetMdText(text: String, baseUrl: String, replace: Boolean) {
        hideProgress()
        readmeWebView!!.visibility = View.VISIBLE
        readmeWebView!!.setGithubContentWithReplace(text, baseUrl, replace);
        requireActivity().invalidateOptionsMenu();
    }

    override fun showProgress(@StringRes resId: Int) {
        progress!!.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress!!.visibility = View.GONE
    }

    override fun showErrorMessage(msgRes: String) {
        onHideProgress()
        super.showErrorMessage(msgRes)
    }

    override fun showMessage(titleRes: Int, msgRes: Int) {
        onHideProgress()
        super.showMessage(titleRes, msgRes)
    }

    override fun onScrollTop(index: Int) {
        super.onScrollTop(index)
    }

    private fun onHideProgress() {
        hideProgress()
    }

    private val isMeOrOrganization: Boolean
        get() = Login.getUser() != null && Login.getUser().login.equals(
            presenter!!.login,
            ignoreCase = true
        ) ||
                userModel != null && userModel!!.type != null && !userModel!!.type.equals(
            "user",
            ignoreCase = true
        )

    companion object {
        @JvmStatic
        fun newInstance(login: String): ProfileOverviewFragment {
            val view = ProfileOverviewFragment()
            view.arguments = Bundler.start().put(BundleConstant.EXTRA, login).end()
            return view
        }
    }
}