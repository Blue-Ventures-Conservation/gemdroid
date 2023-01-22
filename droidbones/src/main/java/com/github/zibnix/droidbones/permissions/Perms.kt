package com.github.zibnix.droidbones.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

object Perms {
    fun has(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * The callback here should inspect the Boolean value, and if true, go ahead and perform
     * the task that requires the requested permission.
     */
    fun register(fragment: Fragment, callback: (Boolean) -> Unit): ActivityResultLauncher<String> {
        return fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            callback(granted)
        }
    }

    fun registerMultiple(fragment: Fragment, callback: (Map<String,Boolean>) -> Unit): ActivityResultLauncher<Array<String>> {
        return fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            callback(map)
        }
    }

    fun registerMultipleReduce(fragment: Fragment, callback: (Boolean) -> Unit): ActivityResultLauncher<Array<String>> {
        return fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            callback(reduce(map))
        }
    }

    /**
     * The callback here should inspect the Boolean value, and if true, go ahead and perform
     * the task that requires the requested permission.
     */
    fun register(activity: ComponentActivity, callback: (Boolean) -> Unit): ActivityResultLauncher<String> {
        return activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            callback(granted)
        }
    }

    fun registerMultiple(activity: ComponentActivity, callback: (Map<String,Boolean>) -> Unit): ActivityResultLauncher<Array<String>> {
        return activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            callback(map)
        }
    }

    fun registerMultipleReduce(activity: ComponentActivity, callback: (Boolean) -> Unit): ActivityResultLauncher<Array<String>> {
        return activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            callback(reduce(map))
        }
    }

    private fun reduce(map: Map<String, Boolean>): Boolean {
        for (grant in map.values) {
            if (!grant) {
                return false
            }
        }

        return true
    }

    /**
     * The callback here should inspect the Boolean value, and if true, request the permission.
     */
    fun rationale(activity: FragmentActivity, permissions: Array<String>, rationale: String, callback: (Boolean) -> Unit) {
        var should = false
        for (perm in permissions) {
            if (shouldShowRequestPermissionRationale(activity, perm)) {
                should = true
                break
            }
        }

        if (should) {
            RationaleDialog(rationale, callback).show(activity.supportFragmentManager, "RationaleDialog")
        } else {
            callback(true)
        }
    }

    fun request(launcher: ActivityResultLauncher<String>, permission: String) {
        launcher.launch(permission)
    }

    fun request(launcher: ActivityResultLauncher<Array<String>>, vararg permissions: String) {
        launcher.launch(arrayOf(*permissions))
    }
}