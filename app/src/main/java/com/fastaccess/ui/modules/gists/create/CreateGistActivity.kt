package com.fastaccess.ui.modules.gists.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.OnClick
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.FilesListModel
import com.fastaccess.data.dao.model.Gist
import com.fastaccess.data.dao.model.Login
import com.fastaccess.helper.*
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.InputHelper.toString
import com.fastaccess.helper.Logger.e
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.modules.gists.gist.files.GistFilesListFragment
import com.fastaccess.ui.widgets.dialog.MessageDialogView
import com.fastaccess.ui.widgets.dialog.MessageDialogView.Companion.newInstance
import com.google.android.material.textfield.TextInputLayout

/**
 * Created by Kosh on 30 Nov 2016, 11:02 AM
 */
class CreateGistActivity : BaseActivity<CreateGistMvp.View, CreateGistPresenter>(),
    CreateGistMvp.View {
    @JvmField
    @BindView(R.id.description)
    var description: TextInputLayout? = null

    @JvmField
    @BindView(R.id.buttonsHolder)
    var buttonsHolder: View? = null

    @JvmField
    @State
    var id: String? = null
    private var filesListFragment: GistFilesListFragment? = null

    @OnClick(value = [R.id.createPublicGist, R.id.createSecretGist])
    fun onClick(view: View) {
        if (view.id == R.id.createSecretGist) {
            ActivityHelper.startCustomTab(
                this,
                "https://blog.github.com/2018-02-18-deprecation-notice-removing-anonymous-gist-creation/"
            )
            return
        }
        presenter!!.onSubmit(
            toString(description?.editText?.text.toString()),
            filesFragment!!.files, view.id == R.id.createPublicGist
        )
    }

    @OnClick(R.id.addFile)
    fun onViewClicked() {
        filesFragment!!.onAddNewFile()
    }

    override fun onDescriptionError(isEmptyDesc: Boolean) {
        description!!.error = if (isEmptyDesc) getString(R.string.required_field) else null
    }

    override fun onFileNameError(isEmptyDesc: Boolean) {
//        fileName.setError(isEmptyDesc ? getString(R.string.required_field) : null);
    }

    override fun onFileContentError(isEmptyDesc: Boolean) {
//        fileContent.setError(isEmptyDesc ? getString(R.string.required_field) : null);
    }

    override fun onSuccessSubmission(gistsModel: Gist?) {
        hideProgress()
        setResult(RESULT_OK)
        finish()
        showMessage(R.string.success, R.string.successfully_submitted)
    }

    override fun layout(): Int {
        return R.layout.create_gist_layout
    }

    override val isTransparent = false

    override fun canBack(): Boolean {
        return true
    }

    override var isSecured = false

    override fun providePresenter(): CreateGistPresenter {
        return CreateGistPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter!!.isEnterprise = PrefGetter.isEnterprise
        setTaskName(getString(R.string.create_gist))
        if (savedInstanceState == null) {
            if (intent != null && intent.extras != null) {
                val bundle = intent.extras!!
                val models: ArrayList<FilesListModel> =
                    bundle.getParcelableArrayList(BundleConstant.ITEM)!!
                val isOwner = bundle.getBoolean(BundleConstant.EXTRA)
                id = bundle.getString(BundleConstant.ID)
                val descriptionText = bundle.getString(BundleConstant.EXTRA_TWO)
                if (description!!.editText != null) description!!.editText!!.setText(descriptionText)
                filesFragment!!.onInitFiles(models, isOwner)
            } else {
                filesFragment!!.onInitFiles(ArrayList(), true)
            }
        }
        buttonsHolder!!.visibility =
            if (!isEmpty(id)) View.GONE else View.VISIBLE
    }

    override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {
        super.onMessageDialogActionClicked(isOk, bundle)
        if (isOk && bundle != null) {
            finish()
        }
    }

    override fun onBackPressed() {
        if (isEmpty(description)) {
            super.onBackPressed()
        } else {
            ViewHelper.hideKeyboard(description!!)
            newInstance(
                getString(R.string.close), getString(R.string.unsaved_data_warning),
                Bundler.start().put("primary_extra", getString(R.string.discard))
                    .put("secondary_extra", getString(R.string.cancel))
                    .put(BundleConstant.EXTRA, true).end()
            ).show(supportFragmentManager, MessageDialogView.TAG)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!isEmpty(id)) {
            menuInflater.inflate(R.menu.done_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.submit) {
            presenter!!.onSubmitUpdate(id!!, toString(description), filesFragment!!.files)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private val filesFragment: GistFilesListFragment?
        get() {
            if (filesListFragment == null) {
                filesListFragment =
                    supportFragmentManager.findFragmentById(R.id.files) as GistFilesListFragment?
            }
            return filesListFragment
        }

    companion object {
        @JvmStatic
        fun start(context: Activity, gistsModel: Gist) {
            val starter = Intent(context, CreateGistActivity::class.java)
            putBundle(gistsModel, starter)
            context.startActivityForResult(starter, BundleConstant.REQUEST_CODE)
        }

        @JvmStatic
        fun start(context: Fragment, gistsModel: Gist) {
            val starter = Intent(context.context, CreateGistActivity::class.java)
            putBundle(gistsModel, starter)
            context.startActivityForResult(starter, BundleConstant.REQUEST_CODE)
        }

        @JvmStatic
        private fun putBundle(gistsModel: Gist, starter: Intent) {
            val login =
                if (gistsModel.owner != null) gistsModel.owner.login else if (gistsModel.user != null) gistsModel.user.login else ""
            starter.putExtras(
                Bundler.start()
                    .putParcelableArrayList(BundleConstant.ITEM, gistsModel.filesAsList)
                    .put(
                        BundleConstant.EXTRA,
                        Login.getUser().login.equals(login, ignoreCase = true)
                    )
                    .put(BundleConstant.ID, gistsModel.gistId)
                    .put(BundleConstant.EXTRA_TWO, gistsModel.description)
                    .end()
            )
        }
    }
}