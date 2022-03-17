package com.fastaccess.ui.modules.profile.org

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.transition.TransitionManager
import butterknife.BindView
import butterknife.OnClick
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.model.User
import com.fastaccess.helper.ActivityHelper.startCustomTab
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler.Companion.start
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.provider.emoji.EmojiParser.parseToUnicode
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.modules.profile.org.project.OrgProjectActivity.Companion.startActivity
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView

/**
 * Created by Kosh on 04 Apr 2017, 10:47 AM
 */
class OrgProfileOverviewFragment :
    BaseFragment<OrgProfileOverviewMvp.View, OrgProfileOverviewPresenter>(),
    OrgProfileOverviewMvp.View {
    @JvmField
    @BindView(R.id.avatarLayout)
    var avatarLayout: AvatarLayout? = null

    @JvmField
    @BindView(R.id.username)
    var username: FontTextView? = null

    @JvmField
    @BindView(R.id.description)
    var description: FontTextView? = null

    @JvmField
    @BindView(R.id.location)
    var location: FontTextView? = null

    @JvmField
    @BindView(R.id.email)
    var email: FontTextView? = null

    @JvmField
    @BindView(R.id.link)
    var link: FontTextView? = null

    @JvmField
    @BindView(R.id.joined)
    var joined: FontTextView? = null

    @JvmField
    @BindView(R.id.progress)
    var progress: LinearLayout? = null

    @JvmField
    @BindView(R.id.projects)
    var projects: View? = null

    @JvmField
    @State
    var userModel: User? = null

    @OnClick(R.id.userInformation)
    fun onOpenAvatar() {
        if (userModel != null) startCustomTab(requireActivity(), userModel!!.avatarUrl)
    }

    @OnClick(R.id.projects)
    fun onOpenProjects() {
        startActivity(requireContext(), presenter!!.login!!, isEnterprise)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onInitViews(userModel: User?) {
        if (view != null) {
            TransitionManager.beginDelayedTransition((view as ViewGroup?)!!)
        }
        if (this.userModel != null) return
        progress!!.visibility = View.GONE
        if (userModel == null) return
        this.userModel = userModel
        username!!.text = if (isEmpty(userModel.name)) userModel.login else userModel.name
        if (userModel.description != null) {
            description!!.text = parseToUnicode(userModel.description)
            description!!.visibility = View.VISIBLE
        } else {
            description!!.visibility = View.GONE
        }
        avatarLayout!!.setUrl(userModel.avatarUrl, null, isOrg = false, isEnterprise = false)
        avatarLayout!!.findViewById<View>(R.id.avatar)
            .setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    startCustomTab(requireActivity(), userModel.avatarUrl)
                    return@setOnTouchListener true
                }
                false
            }
        if (!isEmpty(userModel.location)) {
            location!!.visibility = View.VISIBLE
            location!!.text = userModel.location
        }
        if (!isEmpty(userModel.email)) {
            email!!.visibility = View.VISIBLE
            email!!.text = userModel.email
        }
        if (!isEmpty(userModel.blog)) {
            link!!.visibility = View.VISIBLE
            link!!.text = userModel.blog
        }
        if (!isEmpty(userModel.createdAt)) {
            joined!!.visibility = View.VISIBLE
            joined!!.text = getTimeAgo(userModel.createdAt)
        }
        projects!!.visibility = if (userModel.isHasOrganizationProjects) View.VISIBLE else View.GONE
    }

    override fun fragmentLayout(): Int {
        return R.layout.org_profile_overview_layout
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            presenter!!.onFragmentCreated(arguments)
        } else {
            if (userModel != null) {
                onInitViews(userModel)
            } else {
                presenter!!.onFragmentCreated(arguments)
            }
        }
    }

    override fun providePresenter(): OrgProfileOverviewPresenter {
        return OrgProfileOverviewPresenter()
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

    private fun onHideProgress() {
        hideProgress()
    }

    companion object {
        fun newInstance(login: String): OrgProfileOverviewFragment {
            val view = OrgProfileOverviewFragment()
            view.arguments = start().put(BundleConstant.EXTRA, login).end()
            return view
        }
    }
}