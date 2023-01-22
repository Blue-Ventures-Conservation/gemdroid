package com.github.zibnix.droidbones.permissions

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.github.zibnix.droidbones.ui.Dialogs

class RationaleDialog(private val rationale: String, val callback: (Boolean) -> Unit): DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, Dialogs.dialogTheme)
            builder.setMessage(rationale)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    callback(true)
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    callback(false)
                }
            builder.create()
        } ?: throw IllegalStateException("activity cannot be null")
    }
}