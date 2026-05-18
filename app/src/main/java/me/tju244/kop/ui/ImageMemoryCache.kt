package me.tju244.kop.ui

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest

object ImageMemoryCache {
    private const val MaxEntries = 180

    private val images = object : LinkedHashMap<String, ImageBitmap>(MaxEntries, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean {
            return size > MaxEntries
        }
    }

    @Volatile
    private var diskCacheDir: File? = null

    fun setDiskCacheDir(dir: File) {
        diskCacheDir = File(dir, "image_cache").also { it.mkdirs() }
    }

    @Synchronized
    fun get(key: String): ImageBitmap? = images[key]

    @Synchronized
    fun put(key: String, image: ImageBitmap) {
        images[key] = image
    }

    fun getBytesDisk(key: String): ByteArray? {
        val file = diskCacheFile(key) ?: return null
        if (!file.exists() || file.length() == 0L) return null
        return runCatching { file.readBytes() }.getOrNull()
    }

    fun putBytesDisk(key: String, bytes: ByteArray) {
        val file = diskCacheFile(key) ?: return
        if (file.exists()) return
        runCatching { file.writeBytes(bytes) }
    }

    fun decodeFromDisk(key: String, maxDecodeSize: Int): ImageBitmap? {
        val file = diskCacheFile(key) ?: return null
        if (!file.exists()) return null
        return runCatching {
            val bytes = file.readBytes()
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(ByteArrayInputStream(bytes), null, bounds)
            val sample = calculateImageSampleSize(bounds.outWidth, bounds.outHeight, maxDecodeSize)
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
                inSampleSize = sample
            }
            BitmapFactory.decodeStream(ByteArrayInputStream(bytes), null, options)?.asImageBitmap()
        }.getOrNull()
    }

    @Synchronized
    fun clear() {
        images.clear()
    }

    private fun diskCacheFile(key: String): File? {
        val dir = diskCacheDir ?: return null
        val hash = MessageDigest.getInstance("MD5").digest(key.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(dir, hash)
    }

    private fun calculateImageSampleSize(width: Int, height: Int, maxSize: Int): Int {
        if (width <= 0 || height <= 0 || maxSize <= 0) return 1
        var sample = 1
        var sampledWidth = width
        var sampledHeight = height
        while (sampledWidth / 2 >= maxSize || sampledHeight / 2 >= maxSize) {
            sample *= 2
            sampledWidth /= 2
            sampledHeight /= 2
        }
        return sample.coerceAtLeast(1)
    }
}

