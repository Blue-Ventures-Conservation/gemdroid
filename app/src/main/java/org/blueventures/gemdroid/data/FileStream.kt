package org.blueventures.gemdroid.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import java.io.InputStream

object FileStream {
    data class Streams(val streams: List<InputStream?>, val names: List<String?>)

    fun makeStreams(context: Context, background: (() -> Streams, (Streams) -> Unit) -> Unit, uris: List<Uri>, callback: (Streams) -> Unit) {
        background({
            val strms = mutableListOf<InputStream?>()
            val names = mutableListOf<String?>()
            try {
                uris.forEach { uri ->
                    val stream = if (isVirtualFile(context, uri)) {
                        getInputStreamForVirtualFile(context, uri)
                    } else {
                        context.contentResolver.openInputStream(uri)
                    }
                    strms.add(stream)
                    names.add(contentDisplayName(context, uri))
                }
                Streams(strms, names)
            } catch(e: Exception) {
                Streams(strms, names)
            }
        }) { streams ->
            callback(streams)
        }
    }

    private fun isVirtualFile(context: Context, uri: Uri): Boolean {
        if (!DocumentsContract.isDocumentUri(context, uri)) {
            return false
        }

        val cursor = context.contentResolver.query(
            uri,
            arrayOf(DocumentsContract.Document.COLUMN_FLAGS),
            null, null, null
        )
        var flags = 0
        if (cursor?.moveToFirst() == true) {
            flags = cursor.getInt(0)
        }
        cursor?.close()
        return (flags and DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT) != 0
    }

    private fun getInputStreamForVirtualFile(context: Context, uri: Uri): InputStream? {
        val openableMimeTypes = context.contentResolver.getStreamTypes(uri, "*/*")
        if (openableMimeTypes == null || openableMimeTypes.size < 1) {
            return null
        }

        return context.contentResolver.openTypedAssetFileDescriptor(uri, openableMimeTypes[0], null)?.createInputStream()
    }

    private fun contentDisplayName(context: Context, uri: Uri): String? {
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