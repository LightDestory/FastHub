package com.fastaccess.ui.modules.gists.gist.files

import android.os.Bundle
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.FilesListModel
import com.fastaccess.helper.*
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.Logger.e
import com.fastaccess.helper.PrefGetter.isAllFeaturesUnlocked
import com.fastaccess.helper.PrefGetter.isProEnabled
import com.fastaccess.provider.markdown.MarkDownProvider.isImage
import com.fastaccess.provider.rest.RestProvider
import com.fastaccess.ui.adapter.GistFilesAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.modules.code.CodeViewerActivity.Companion.startActivity
import com.fastaccess.ui.modules.gists.create.dialog.AddGistBottomSheetDialog.Companion.TAG
import com.fastaccess.ui.modules.gists.create.dialog.AddGistBottomSheetDialog.Companion.newInstance
import com.fastaccess.ui.modules.main.premium.PremiumActivity.Companion.startActivity
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.dialog.MessageDialogView
import com.fastaccess.ui.widgets.dialog.MessageDialogView.Companion.newInstance
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller
import java.util.HashMap

/**
 * Created by Kosh on 13 Nov 2016, 1:36 PM
 */
class GistFilesListFragment : BaseFragment<GistFilesListMvp.View, GistFilesListPresenter>(),
    GistFilesListMvp.View {
    @JvmField
    @BindView(R.id.recycler)
    var recycler: DynamicRecyclerView? = null

    @JvmField
    @BindView(R.id.refresh)
    var refresh: SwipeRefreshLayout? = null

    @JvmField
    @BindView(R.id.stateLayout)
    var stateLayout: StateLayout? = null

    @JvmField
    @BindView(R.id.fastScroller)
    var fastScroller: RecyclerViewFastScroller? = null

    @JvmField
    @State
    var isOwner = false
    private var adapter: GistFilesAdapter? = null
    override val filesMap: HashMap<String, FilesListModel> = hashMapOf()

    val files: MutableMap<String, FilesListModel>
        get() = presenter.filesMap

    override fun fragmentLayout(): Int {
        return R.layout.small_grid_refresh_list
    }

    override fun providePresenter(): GistFilesListPresenter {
        return GistFilesListPresenter()
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        stateLayout!!.setEmptyText(R.string.no_files)
        stateLayout!!.showEmptyState()
        recycler!!.setEmptyView(stateLayout!!, refresh)
        refresh!!.isEnabled = false
        adapter = GistFilesAdapter(presenter.files, presenter, isOwner)
        recycler!!.adapter = adapter
        if (arguments != null && savedInstanceState == null) {
            val filesListModel: MutableList<FilesListModel> =
                requireArguments().getParcelableArrayList(BundleConstant.ITEM)!!
            isOwner = requireArguments().getBoolean(BundleConstant.EXTRA_TYPE)
            onInitFiles(filesListModel, isOwner)
            arguments = null //CLEAR
        } else {
            onInitFiles(presenter.files, isOwner)
        }
        fastScroller!!.attachRecyclerView(recycler!!)
    }

    override fun onOpenFile(item: FilesListModel, position: Int) {
        if (canOpen(item) && !isOwner) {
            startActivity(requireContext(), item.rawUrl!!, item.rawUrl!!)
        } else if (isOwner && canOpen(item)) {
            onEditFile(item, position)
        }
    }

    override fun onDeleteFile(item: FilesListModel, position: Int) {
        newInstance(
            getString(R.string.delete), getString(R.string.confirm_message), false,
            Bundler.start()
                .put(BundleConstant.ID, position)
                .put(BundleConstant.YES_NO_EXTRA, true)
                .end()
        )
            .show(childFragmentManager, MessageDialogView.TAG)
    }

    override fun onEditFile(item: FilesListModel, position: Int) {
        newInstance(item, position).show(childFragmentManager, TAG)
    }


    override fun onInitFiles(file: MutableList<FilesListModel>, isOwner: Boolean) {
        if (presenter.filesMap.isEmpty() && file.isNotEmpty()) {
            for (listModel in file) {
                presenter.filesMap[listModel.filename!!] = listModel
            }
        }

        adapter!!.setOwner(isOwner)
        presenter.files = file
        adapter!!.insertItems(file)
    }

    override fun onAddNewFile() {
        if (adapter!!.itemCount == 0 || isProEnabled || isAllFeaturesUnlocked) {
            newInstance(null, -1)
                .show(childFragmentManager, TAG)
        } else {
            startActivity(requireContext())
        }
    }

    override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {
        super.onMessageDialogActionClicked(isOk, bundle)
        if (isOk && bundle != null) {
            val url = bundle.getString(BundleConstant.EXTRA)
            if (!isEmpty(url)) {
                if (ActivityHelper.checkAndRequestReadWritePermission(requireActivity())) {
                    RestProvider.downloadFile(requireContext(), url!!)
                }
            } else if (bundle.getBoolean(BundleConstant.YES_NO_EXTRA)) {
                if (adapter != null) {
                    val position = bundle.getInt(BundleConstant.ID)
                    var file = adapter!!.getItem(position)
                    if (file != null) {
                        if (presenter.filesMap[file.filename] != null) {
                            file = presenter.filesMap[file.filename]!!
                            file.content = null
                            presenter.filesMap[file.filename!!] = file
                        }
                    }
                    adapter!!.removeItem(position)
                }
            }
        }
    }

    override fun onScrollTop(index: Int) {
        super.onScrollTop(index)
        if (recycler != null) recycler!!.scrollToPosition(0)
    }

    override fun onFileAdded(file: FilesListModel, position: Int?) {
        if (position == null || position == -1) {
            adapter!!.addItem(file)
            presenter.filesMap[file.filename!!] = file
        } else {
            val current = adapter!!.getItem(position)!!
            if (presenter.filesMap.contains(current.filename)) {
                presenter.filesMap[current.filename]?.let { toUpdate ->
                    toUpdate.filename = file.filename
                    toUpdate.content = file.content
                    presenter.filesMap.put(current.filename!!, toUpdate)
                }
            }
            adapter!!.swapItem(file, position)
        }
    }

    private fun canOpen(item: FilesListModel): Boolean {
        if (item.rawUrl == null) return false
        if (item.size!! > FileHelper.ONE_MB && !isImage(item.rawUrl)) {
            newInstance(
                getString(R.string.big_file), getString(R.string.big_file_description),
                isMarkDown = false,
                hideCancel = true,
                bundle = Bundler.start().put(BundleConstant.YES_NO_EXTRA, true)
                    .put(BundleConstant.EXTRA, item.rawUrl).end()
            )
                .show(childFragmentManager, "MessageDialogView")
            return false
        }
        return true
    }

    companion object {
        fun newInstance(
            files: ArrayList<FilesListModel>,
            isOwner: Boolean
        ): GistFilesListFragment {
            val view = GistFilesListFragment()
            view.arguments = Bundler.start()
                .putParcelableArrayList(BundleConstant.ITEM, files)
                .put(BundleConstant.EXTRA_TYPE, isOwner)
                .end()
            return view
        }
    }
}