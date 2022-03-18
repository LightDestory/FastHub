package com.fastaccess.ui.widgets.dialog

import android.content.Context
import android.os.Parcelable
import com.fastaccess.ui.base.BaseDialogFragment
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.R
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.ui.adapter.SimpleListAdapter
import com.fastaccess.ui.base.mvp.BaseMvp
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import java.util.ArrayList

/**
 * Created by Kosh on 31 Dec 2016, 3:19 PM
 */
class ListDialogView<O : Parcelable> :
    BaseDialogFragment<BaseMvp.FAView, BasePresenter<BaseMvp.FAView>>(),
    BaseViewHolder.OnItemClickListener<O> {
    lateinit var title: FontTextView
    lateinit var recycler: DynamicRecyclerView
    lateinit var fastScroller: RecyclerViewFastScroller

    interface OnSimpleItemSelection<T : Parcelable> {
        fun onItemSelected(item: T)
    }
    interface OnSimpleItemLongSelection<T : Parcelable> {
        fun onItemLongSelected(item: T)
    }

    private var simpleItemSelection: OnSimpleItemSelection<O>? = null
    private var simpleItemLongSelection: OnSimpleItemLongSelection<O>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)!!
        this.title = root.findViewById(R.id.title)
        this.recycler = root.findViewById(R.id.recycler)
        this.fastScroller = root.findViewById(R.id.fastScroller)
        return root
    }

    override fun fragmentLayout(): Int {
        return R.layout.simple_list_dialog
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        val objects: ArrayList<O> = requireArguments().getParcelableArrayList(BundleConstant.ITEM)!!
        val titleText = requireArguments().getString(BundleConstant.EXTRA)!!
        title.text = titleText
        val adapter = SimpleListAdapter(objects, this)
        recycler.addDivider()
        recycler.adapter = adapter
        fastScroller.attachRecyclerView(recycler)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment != null) {
            if (parentFragment is OnSimpleItemSelection<*>) {
                simpleItemSelection = parentFragment as OnSimpleItemSelection<O>
            }
            if (parentFragment is OnSimpleItemLongSelection<*>) {
                simpleItemLongSelection = parentFragment as OnSimpleItemLongSelection<O>
            }
        } else {
            if (parentFragment is OnSimpleItemSelection<*>) {
                simpleItemSelection = context as OnSimpleItemSelection<O>
            }
            if (parentFragment is OnSimpleItemLongSelection<*>) {
                simpleItemLongSelection = context as OnSimpleItemLongSelection<O>
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        simpleItemSelection = null
        simpleItemLongSelection = null
    }

    override fun providePresenter(): BasePresenter<BaseMvp.FAView> {
        return BasePresenter()
    }

    override fun onItemClick(position: Int, v: View?, item: O) {
        simpleItemSelection?.onItemSelected(item)
        dismiss()
    }

    override fun onItemLongClick(position: Int, v: View?, item: O) {
        simpleItemLongSelection?.onItemLongSelected(item)
    }


    fun initArguments(title: String, objects: ArrayList<O>) {
        arguments = Bundler.start()
            .put(BundleConstant.EXTRA, title)
            .putParcelableArrayList(BundleConstant.ITEM, objects)
            .end()
    }

    fun initArguments(title: String, objects: List<O>) {
        arguments = Bundler.start()
            .put(BundleConstant.EXTRA, title)
            .putParcelableArrayList(
                BundleConstant.ITEM,
                objects.toMutableList() as ArrayList<out Parcelable>
            )
            .end()
    }

    companion object {
        val TAG: String = ListDialogView::class.java.simpleName
    }
}