package com.fastaccess.ui.modules.gists.gist

import com.fastaccess.data.dao.FilesListModel

object GistFragmentHelper {
    private const val TEXT_MIME = "text/"

    /**
     * This method will remove the content of non text/\* mime type files to save the
     * parcelable size limit.
     *
     * Such files will be automatically opened in CustomChromeTabs.
     */
    @JvmStatic
    fun mapNonMarkdownFiles(files: ArrayList<FilesListModel>): ArrayList<FilesListModel> {
        return ArrayList(files.map {
            if (isTextMimeType(it)) return@map it
            return@map it.apply { content = "" }
        }.toList())
    }

    @JvmStatic
    fun isTextMimeType(file: FilesListModel): Boolean = file.type!!.contains(TEXT_MIME)
}