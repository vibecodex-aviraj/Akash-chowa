package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.io.InputStream

object ImageUtil {

    fun createTempImageUri(context: Context): Uri {
        val imageDir = File(context.cacheDir, "images")
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }
        val file = File(imageDir, "camera_${System.currentTimeMillis()}.jpg")
        val authority = "${context.packageName}.provider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    fun uriToBitmap(context: Context, uri: Uri, maxDimension: Int = 1024): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val width = info.size.width
                    val height = info.size.height
                    if (width > maxDimension || height > maxDimension) {
                        val scale = maxOf(width.toFloat() / maxDimension, height.toFloat() / maxDimension)
                        decoder.setTargetSize((width / scale).toInt(), (height / scale).toInt())
                    }
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                var input: InputStream? = context.contentResolver.openInputStream(uri)
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(input, null, options)
                input?.close()

                var inSampleSize = 1
                val width = options.outWidth
                val height = options.outHeight
                if (width > maxDimension || height > maxDimension) {
                    val halfWidth = width / 2
                    val halfHeight = height / 2
                    while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                        inSampleSize *= 2
                    }
                }

                input = context.contentResolver.openInputStream(uri)
                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val bitmap = BitmapFactory.decodeStream(input, null, decodeOptions)
                input?.close()
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
