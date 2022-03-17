package com.fastaccess.ui.modules.gists.gist

import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import com.fastaccess.ui.modules.editor.comment.CommentEditorFragment.CommentListener
import com.fastaccess.ui.base.mvp.BaseMvp.FAPresenter
import android.content.Intent
import com.fastaccess.data.dao.model.Gist

/**
 * Created by Kosh on 12 Nov 2016, 12:17 PM
 */
interface GistMvp {
    interface View : FAView, CommentListener {
        fun onSuccessDeleted()
        fun onErrorDeleting()
        fun onGistStarred(isStarred: Boolean)
        fun onGistForked(isForked: Boolean)
        fun onSetupDetails()
        fun onUpdatePinIcon(gist: Gist)
    }

    interface Presenter : FAPresenter {
        val gist: Gist?
        fun gistId(): String
        fun onActivityCreated(intent: Intent?)
        fun onDeleteGist()
        val isOwner: Boolean
        fun onStarGist()
        fun onForkGist()
        val isForked: Boolean
        val isStarred: Boolean
        fun checkStarring(gistId: String)
        fun callApi()
        fun onWorkOffline(gistId: String)
        fun onPinUnpinGist()
    }
}