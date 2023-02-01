package com.github.zibnix.droidbones.location

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.github.zibnix.droidbones.R
import com.github.zibnix.droidbones.ui.Dialogs

class LocationDisabledDialog() : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(getString(R.string.turn_on_location))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val request = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
                    request.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("activity cannot be null")
    }

    companion object {
        fun show(manager: FragmentManager) {
            LocationDisabledDialog().show(manager, "LocationDisabledDialog")
        }
    }
}