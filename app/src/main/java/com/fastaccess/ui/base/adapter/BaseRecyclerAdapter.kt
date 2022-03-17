package com.fastaccess.ui.base.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fastaccess.helper.AnimHelper.startBeatsAnimation
import com.fastaccess.helper.PrefGetter.isRVAnimationEnabled
import com.fastaccess.ui.widgets.recyclerview.ProgressBarViewHolder
import kotlin.math.abs

/**
 * Created by Kosh on 17 May 2016, 7:10 PM
 */
abstract class BaseRecyclerAdapter<M, VH : BaseViewHolder<M>, P : BaseViewHolder.OnItemClickListener<M>> :
    RecyclerView.Adapter<VH> {
//    constructor(list: List<M>) {
//        this.data.addAll(list)
//    }
//
//    constructor(list: List<M>, listener: P?) {
//        this.data.addAll(list)
//        this.listener = listener
//    }


    @Suppress("UNCHECKED_CAST")
    constructor(list: MutableList<M>) {
        this.data = list as MutableList<M?>
    }

    @Suppress("UNCHECKED_CAST")
    constructor(list: MutableList<M>, listener: P?) {
        this.data = list as MutableList<M?>
        this.listener = listener
    }

    constructor()

    var data: MutableList<M?> = mutableListOf()
        private set
    var listener: P? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var lastKnowingPosition = -1
    private var isEnableAnimation = isRVAnimationEnabled
        @SuppressLint("NotifyDataSetChanged")
        set(enableAnimation) {
            field = enableAnimation
            notifyDataSetChanged()
        }
    private var isShowedGuide = false
    private var guideListener: GuideListener<M>? = null
    var isProgressAdded = false
        private set
    var rowWidth = 0
        @SuppressLint("NotifyDataSetChanged")
        set(rowWidth) {
            if (this.rowWidth == 0) {
                field = rowWidth
                notifyDataSetChanged()
            }
        }

    protected constructor(listener: P?) : this(mutableListOf(), listener)

    protected abstract fun viewHolder(parent: ViewGroup, viewType: Int): VH
    protected abstract fun onBindView(holder: VH, position: Int)

    fun getItemByPosition(position: Int): M? {
        return data[position]
    }

    fun getItem(position: Int): M? {
        return data[position]
    }

    fun getItem(t: M): Int {
        return data.indexOf(t)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return if (viewType == PROGRESS_TYPE) {
            addSpanLookup(parent)
            ProgressBarViewHolder.newInstance(parent) as VH
        } else {
            viewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (holder is ProgressBarViewHolder) {
            if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                val layoutParams =
                    holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
                layoutParams.isFullSpan = true
            }
        } else if (getItem(position) != null) {
            animate(holder, position)
            onBindView(holder, position)
            onShowGuide(holder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) == null) {
            PROGRESS_TYPE
        } else super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun onShowGuide(
        holder: VH,
        position: Int
    ) { // give the flexibility to other adapters to override this
        if (position == 0 && !isShowedGuide && guideListener != null) {
            val item = getItem(position)
            guideListener!!.onShowGuide(holder.itemView, item)
            isShowedGuide = true
        }
    }

    private fun animate(holder: VH, position: Int) {
        if (isEnableAnimation && position > lastKnowingPosition) {
            startBeatsAnimation(holder.itemView)
            lastKnowingPosition = position
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun insertItems(items: List<M>) {
        // shared variables not use insert
        if (items != data) {
            data.clear()
            data.addAll(items)
        }
        notifyDataSetChanged()
        isProgressAdded = false
    }

    fun addItem(item: M, position: Int) {
        data.add(position, item)
        notifyItemInserted(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(item: M?) {
        removeProgress()
        data.add(item)
        if (data.size == 0) {
            notifyDataSetChanged()
        } else {
            notifyItemInserted(data.size - 1)
        }
    }

    fun addItems(items: List<M>) {
        removeProgress()
        data.addAll(items)
        notifyItemRangeInserted(itemCount, itemCount + items.size - 1)
    }

    fun removeItem(position: Int) {
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeItem(item: M) {
        val position = data.indexOf(item)
        if (position != -1) removeItem(position)
    }

    fun removeItems(items: List<M>) {
        val prevSize = itemCount
        data.removeAll(items)
        notifyItemRangeRemoved(prevSize, abs(data.size - prevSize))
    }

    fun swapItem(model: M) {
        val index = getItem(model)
        swapItem(model, index)
    }

    fun swapItem(model: M, position: Int) {
        if (position != -1) {
            data[position] = model
            notifyItemChanged(position)
        }
    }

    fun subList(fromPosition: Int, toPosition: Int) {
        if (data.isEmpty()) return
        data.subList(fromPosition, toPosition).clear()
        notifyItemRangeRemoved(fromPosition, toPosition)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        isProgressAdded = false
        data.clear()
        notifyDataSetChanged()
    }

    val isEmpty: Boolean
        get() = data.isEmpty()

    override fun onViewDetachedFromWindow(holder: VH) {
        holder.onViewIsDetaching()
        super.onViewDetachedFromWindow(holder)
    }

    fun addProgress() {
        if (!isProgressAdded && !isEmpty) {
            addItem(null)
            isProgressAdded = true
        }
    }

    private fun removeProgress() {
        if (!isEmpty) {
            val m = getItem(itemCount - 1)
            if (m == null) {
                removeItem(itemCount - 1)
            }
            isProgressAdded = false
        }
    }

    private fun addSpanLookup(parent: ViewGroup) {
        if (parent is RecyclerView) {
            if (parent.layoutManager is GridLayoutManager) {
                val layoutManager = parent.layoutManager as GridLayoutManager?
                layoutManager!!.spanSizeLookup = object : SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (getItemViewType(position) == PROGRESS_TYPE) layoutManager.spanCount else 1
                    }
                }
            }
        }
    }

    interface GuideListener<M> {
        fun onShowGuide(itemView: View, model: M?)
    }

    companion object {
        private const val PROGRESS_TYPE = 2017
    }

    init {
        this.listener = listener
    }
}