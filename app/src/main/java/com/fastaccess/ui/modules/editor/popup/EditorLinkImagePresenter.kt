package com.fastaccess.ui.modules.editor.popup

import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import okhttp3.RequestBody
import com.fastaccess.provider.rest.ImgurProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Created by Kosh on 15 Apr 2017, 9:08 PM
 */
class EditorLinkImagePresenter : BasePresenter<EditorLinkImageMvp.View>(),
    EditorLinkImageMvp.Presenter {
    override fun onSubmit(title: String?, file: File) {
        if (file.exists()) {
            val image: RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            makeRestCall(
                ImgurProvider.imgurService.postImage(title ?: "", image),
                { imgurResponseModel ->
                    if (imgurResponseModel.data != null) {
                        val imageResponse = imgurResponseModel.data!!
                        sendToView { view ->
                            view?.onUploaded(
                                title ?: imageResponse.title!!, imageResponse.link!!
                            )
                        }
                    } else {
                        sendToView { view ->
                            view?.onUploaded(
                                null,
                                null
                            )
                        }
                    }

                }, false
            )
        } else {
            if (view != null) view!!.onUploaded(null, null)
        }
    }
}