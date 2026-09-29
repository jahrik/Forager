package com.zynergylabs.forager.app.data.backup

import android.content.Context
import com.zynergylabs.forager.app.domain.BackupFiles
import java.io.InputStream
import java.io.OutputStream

/** [BackupFiles] over the Storage Access Framework. (Tests-first stub: every operation is unsupported.) */
class ContentResolverBackupFiles(private val context: Context) : BackupFiles {
    override fun openForWrite(uri: String): OutputStream = throw UnsupportedOperationException("backup files: not built")

    override fun openForRead(uri: String): InputStream = throw UnsupportedOperationException("backup files: not built")

    override fun createInFolder(folderUri: String, displayName: String): OutputStream = throw UnsupportedOperationException("backup files: not built")

    override fun keepAccessToFolder(folderUri: String) = throw UnsupportedOperationException("backup files: not built")
}
