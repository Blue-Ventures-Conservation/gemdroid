package org.blueventures.gemdroid.ui.analysis

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object CRA {

    fun contentDisplayName(context: Context, uri: Uri): String? {
        var name: String? = null

        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.let { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                name = cursor.getString(nameIndex)
                cursor.close()
            }
        }

        return name
    }
}