package com.fastaccess.ui.modules.parser

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.fastaccess.App
import com.fastaccess.R
import com.fastaccess.data.dao.model.Login
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.modules.login.chooser.LoginChooserActivity

/**
 * Created by Kosh on 09 Dec 2016, 12:31 PM
 */
class LinksParserActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Login.getUser() == null) {
            Toast.makeText(App.getInstance(), R.string.please_login, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginChooserActivity::class.java))
            finish()
            return
        }
        onCreate(intent)
    }

    override fun onStart() {
        super.onStart()
        setVisible(true)
    }

    private fun onCreate(intent: Intent?) {
        if (intent == null || intent.action == null) {
            finish()
            return
        }
        if (Intent.ACTION_SEND == intent.action) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!isEmpty(sharedText)) {
                try {
                    val uri = Uri.parse(sharedText)
                    onUriReceived(uri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (intent.action == Intent.ACTION_VIEW) {
            if (intent.data != null) {
                onUriReceived(intent.data!!)
            }
        }
        finish()
    }

    private fun onUriReceived(uri: Uri) {
        launchUri(this, uri, showRepoBtn = false, newDocument = true)
    }
}