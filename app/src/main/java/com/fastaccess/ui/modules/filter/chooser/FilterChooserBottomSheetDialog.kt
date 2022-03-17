package com.fastaccess.ui.modules.filter.chooser

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fastaccess.R
import com.fastaccess.ui.base.BaseBottomSheetDialog
import com.fastaccess.utils.setOnThrottleClickListener

/**
 * Created by Kosh on 10 Apr 2017, 12:18 PM
 */
class FilterChooserBottomSheetDialog : BaseBottomSheetDialog() {
    private var listener: FilterAddChooserListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is FilterAddChooserListener) {
            listener = parentFragment as FilterAddChooserListener?
        } else if (context is FilterAddChooserListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)!!
        listOf<View>(
            root.findViewById(R.id.add),
            root.findViewById(R.id.search),
        ).setOnThrottleClickListener {
            onViewClicked(it)
        }
        return root
    }

    override fun onDestroy() {
        listener = null
        super.onDestroy()
    }

    override fun layoutRes(): Int {
        return R.layout.add_filter_row_layout
    }

    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.add -> listener!!.onAddSelected()
            R.id.search -> listener!!.onSearchSelected()
        }
        dismiss()
    }

    companion object {
        fun newInstance(): FilterChooserBottomSheetDialog {
            return FilterChooserBottomSheetDialog()
        }
    }
}