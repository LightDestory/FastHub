package com.fastaccess.ui.modules.repos.git

import android.content.Intent
import android.util.Base64
import com.fastaccess.data.dao.CommitRequestModel
import com.fastaccess.data.dao.EditRepoFileModel
import com.fastaccess.helper.BundleConstant
import com.fastaccess.provider.rest.RestProvider
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by kosh on 29/08/2017.
 */
class EditRepoFilePresenter : BasePresenter<EditRepoFileMvp.View>(), EditRepoFileMvp.Presenter {

    @com.evernote.android.state.State var model: EditRepoFileModel? = null
    var fileContent: String? = null

    var downloadedContent: String? = null

    override fun onInit(intent: Intent?) {
        if (downloadedContent.isNullOrBlank()) {
            intent?.let { intent1 ->
                intent1.extras?.let {
                    model = it.getParcelable(BundleConstant.ITEM)
                    loadContent()
                }
            }
        } else {
            sendToView { it.onSetText(downloadedContent) }
        }
    }

    override fun onSubmit(text: String?, filename: String?, description: String?) {
        if (model?.login.isNullOrBlank() || model?.repoId.isNullOrBlank()) return

        sendToView {
            it.onSetTextError(text.isNullOrBlank())
            it.onSetFilenameError(filename.isNullOrBlank())
            it.onSetDescriptionError(description.isNullOrBlank())
        }
        if (!text.isNullOrBlank() && !description.isNullOrBlank() && !filename.isNullOrBlank()) {
            model?.let { editRepoFileModel ->
                val commitModel = CommitRequestModel(description, Base64.encodeToString(text.toByteArray(), Base64.DEFAULT), editRepoFileModel.sha!!, editRepoFileModel.ref)
                val path = if (editRepoFileModel.path.isNullOrBlank()) {
                    filename
                } else {
                    if (editRepoFileModel.path.endsWith("/")) {
                        "${editRepoFileModel.path}$filename"
                    } else {
                        "${editRepoFileModel.path}"
                    }
                }
                val observable = RestProvider.getContentService(isEnterprise).updateCreateFile(editRepoFileModel.login, editRepoFileModel.repoId,
                        path, editRepoFileModel.ref, commitModel)
                makeRestCall(observable) { sendToView { it.onSuccessfullyCommitted() } }
            }
        }
    }

    private fun loadContent() {
        model?.contentUrl?.let { s ->
            makeRestCall(RestProvider.getRepoService(isEnterprise)
                    .getFileAsStream(s)
            ) {
                fileContent = it
                sendToView { v -> v.onSetText(it) }
            }
        }
    }
}