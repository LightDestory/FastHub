package com.fastaccess.ui.modules.settings

import android.content.Context
import com.fastaccess.ui.base.BaseBottomSheetDialog
import com.fastaccess.R
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.FontButton
import com.fastaccess.helper.ActivityHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fastaccess.utils.setOnThrottleClickListener

/**
 * Created by Kosh on 01 May 2017, 12:58 AM
 */
class SlackBottomSheetDialog : BaseBottomSheetDialog() {
    interface SlackDialogListener {
        fun onDismissed()
    }

    lateinit var title: FontTextView

    lateinit var message: FontTextView

    lateinit var cancel: FontButton

    lateinit var ok: FontButton
    private var listener: SlackDialogListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)!!
        this.title = root.findViewById(R.id.title)
        this.message = root.findViewById(R.id.message)
        this.cancel = root.findViewById(R.id.cancel)
        this.ok = root.findViewById(R.id.ok)
        listOf<View>(this.ok, this.cancel).setOnThrottleClickListener {
            this.onViewClicked(it)
        }
//        this.ok.setOnThrottleClickListener {
//            this.onViewClicked(it)
//        }
//        this.cancel.setOnThrottleClickListener {
//            this.onViewClicked(it)
//        }
        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SlackDialogListener) {
            listener = context
        }
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    override fun layoutRes(): Int {
        return R.layout.message_dialog
    }

    fun onViewClicked(view: View) {
        if (view.id == R.id.ok) {
            ActivityHelper.startCustomTab(requireActivity(), "http://rebrand.ly/fasthub")
        }
        if (listener != null) listener!!.onDismissed()
        dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cancel.setText(R.string.no)
        ok.setText(R.string.yes)
        title.setText(R.string.join_slack)
        message.text = getString(R.string.join_slack_message)
    }

    override fun onHidden() {
        if (listener != null) listener!!.onDismissed()
        super.onHidden()
    }

    override fun onDismissedByScrolling() {
        if (listener != null) listener!!.onDismissed()
        super.onDismissedByScrolling()
    }

    companion object {
        @JvmField
        val TAG: String = SlackBottomSheetDialog::class.java.simpleName
    }
}