package com.zynergylabs.forager.app.ui.log

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.util.Base64

/**
 * A stand-in for the platform's media provider, registered under the authority `media` so a real
 * `ContentResolver.insert(MediaStore.Images.Media.getContentUri(...), values)` reaches it. It records every
 * insert, update and delete, and `openFile` hands back a real file, so the exporter's own stream copy runs
 * for real. It is not MediaStore: it applies no relative-path rules and shows nothing in any Gallery.
 */
internal class FakeMediaProvider : ContentProvider() {
    class Inserted(val uri: Uri, val values: ContentValues, val file: File)

    override fun onCreate(): Boolean = true

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (refuseInsert) return null
        val id = inserted.size + 1
        val file = File(context!!.cacheDir, "fake-media-$id").apply { parentFile?.mkdirs(); delete() }
        val row = Uri.parse("content://media/external_primary/images/media/$id")
        inserted += Inserted(row, ContentValues(values), file)
        return row
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        val row = inserted.first { it.uri == uri }
        return ParcelFileDescriptor.open(
            row.file,
            ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE,
        )
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        updated += uri to ContentValues(values)
        return 1
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        deleted += uri
        return 1
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null

    companion object {
        val inserted = mutableListOf<Inserted>()
        val updated = mutableListOf<Pair<Uri, ContentValues>>()
        val deleted = mutableListOf<Uri>()
        var refuseInsert = false

        fun reset() {
            inserted.clear()
            updated.clear()
            deleted.clear()
            refuseInsert = false
        }
    }
}

/** A real 40x20 JPEG (the scrub tests' fixture), so its header is a JPEG's. */
internal val EXPORT_TEST_JPEG: ByteArray = Base64.getDecoder().decode("/9j/4AAQSkZJRgABAgAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAAUACgDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD5/ooooAKKKKACiiigAooooAKKKKACiiigD//Z")

/** The first bytes of each format, followed by filler: enough for a header sniff, not a decodable image. */
internal fun exportTestPng(): ByteArray = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) + ByteArray(64) { it.toByte() }
internal fun exportTestWebp(): ByteArray = "RIFF".toByteArray() + byteArrayOf(0x40, 0, 0, 0) + "WEBPVP8 ".toByteArray() + ByteArray(64) { it.toByte() }
internal fun exportTestHeic(): ByteArray = byteArrayOf(0, 0, 0, 0x18) + "ftypheic".toByteArray() + ByteArray(72) { it.toByte() }
internal fun exportTestNotAnImage(): ByteArray = "this is not a picture, only text".toByteArray()
