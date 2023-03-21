package com.github.zibnix.droidbones.ui

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

object Dialogs {
    fun dismiss(fragment: Fragment) {
        if (fragment.isAdded) {
            dismiss(fragment.parentFragmentManager)
        }
        dismiss(fragment.childFragmentManager)
    }

    private fun dismiss(manager: FragmentManager?) {
        manager?.let { fm ->
            for (fragment in fm.fragments) {
                if (fragment is DialogFragment) {
                    fragment.dismissAllowingStateLoss()
                }
            }
        }
    }
}