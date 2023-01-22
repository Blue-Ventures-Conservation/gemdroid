package com.github.zibnix.droidbones.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.zibnix.droidbones.R
import com.github.zibnix.droidbones.databinding.FragmentBaseBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseFragment : Fragment(), Progress {
    abstract val layoutId: Int
    abstract val contentId: Int

    private lateinit var baseBinding: FragmentBaseBinding

    abstract fun bind(inflated: View)

    override fun showProgress() {
        view?.findViewById<View>(contentId)?.visibility = View.GONE
        baseBinding.progress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        baseBinding.progress.visibility = View.GONE
        view?.findViewById<View>(contentId)?.visibility = View.VISIBLE
    }

    override fun showSnackBar(msg: String) {
        context?.let { ctx ->
            val snack = Snackbar.make(baseBinding.contentWrapper, msg, Snackbar.LENGTH_LONG)
            snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                .setTextColor(ContextCompat.getColor(ctx, R.color.colorTextLight))
            snack.show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseBinding.stub.layoutResource = layoutId
        baseBinding.stub.inflate()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        baseBinding = FragmentBaseBinding.inflate(inflater, container, false)
        baseBinding.stub.setOnInflateListener { _, inflated -> bind(inflated) }
        return baseBinding.root
    }

    override fun onPause() {
        super.onPause()
        Dialogs.dismiss(this)
    }

    protected fun started(block: suspend CoroutineScope.() -> Unit): Job {
        return viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }
}