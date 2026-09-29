package com.zynergylabs.forager.app.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.provider.DocumentsContract
import com.zynergylabs.forager.app.domain.BackupFiles
import com.zynergylabs.forager.app.domain.BackupTarget
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * [BackupFiles] over the Storage Access Framework: a file the user picked, or a folder the user picked and the
 * app keeps permission for. A file is opened `"wt"` (truncating), so backing up over an existing file cannot
 * leave the tail of an older, longer one behind. **There is no delete here**, by design: a scheduled run only
 * ever adds a file.
 *
 * Under Robolectric there is no document provider, so [createInFolder] and [keepAccessToFolder] are exercised
 * on a device only; [openForRead] and [openForWrite] are tested through `file:` URIs, which the same
 * `ContentResolver` calls handle.
 */
class ContentResolverBackupFiles(context: Context) : BackupFiles {
    private val resolver = context.applicationContext.contentResolver

    private companion object {
        const val TAG = "ContentResolverBackupFiles"
    }

    override fun openForWrite(uri: String): OutputStream =
        resolver.openOutputStream(Uri.parse(uri), "wt") ?: throw IOException("no output stream for $uri")

    override fun openForRead(uri: String): InputStream =
        resolver.openInputStream(Uri.parse(uri)) ?: throw IOException("no input stream for $uri")

    /**
     * Deletes the file at [uri]: a document through the provider, or (tests, and nothing else in the app makes one) a
     * `file:` URI directly. Reports whether it is gone; a failure is logged here and reported as `false`, so the caller,
     * which only ever deletes the file its own run created, can log and carry on.
     */
    override fun delete(uri: String): Boolean {
        val parsed = Uri.parse(uri)
        return try {
            if (parsed.scheme == "file") File(parsed.path!!).delete() else DocumentsContract.deleteDocument(resolver, parsed)
        } catch (e: Exception) {
            Log.w(TAG, "could not delete $uri", e)
            false
        }
    }

    override fun createInFolder(folderUri: String, displayName: String): BackupTarget {
        val tree = Uri.parse(folderUri)
        val parent = DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
        val document = DocumentsContract.createDocument(resolver, parent, "application/zip", displayName)
            ?: throw IOException("the folder would not create $displayName")
        return BackupTarget(document.toString(), resolver.openOutputStream(document, "wt") ?: throw IOException("no output stream for the new file $displayName"))
    }

    override fun keepAccessToFolder(folderUri: String) {
        resolver.takePersistableUriPermission(
            Uri.parse(folderUri),
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
    }
}
