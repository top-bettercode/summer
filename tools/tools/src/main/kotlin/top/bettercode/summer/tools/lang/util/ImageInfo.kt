package top.bettercode.summer.tools.lang.util

import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files

/**
 * 快速获取图片的大小，支持：gif,jpg,png,bmp,webp,tiff
 */
class ImageInfo {
    var height = 0
        private set
    var width = 0
        private set
    var mimeType: String? = null
        private set

    companion object {
        @JvmStatic
        fun of(file: File): ImageInfo {
            return ImageInfo(file)
        }

        @JvmStatic
        fun of(inputStream: InputStream): ImageInfo {
            return ImageInfo(inputStream)
        }

        @JvmStatic
        fun of(bytes: ByteArray): ImageInfo {
            return ImageInfo(bytes)
        }
    }

    constructor(file: File) {
        Files.newInputStream(file.toPath()).use { `is` -> processStream(`is`) }
    }

    constructor(`is`: InputStream) {
        processStream(`is`)
    }

    constructor(bytes: ByteArray) {
        ByteArrayInputStream(bytes).use { `is` -> processStream(`is`) }
    }

    private fun processStream(`is`: InputStream) {
        val c1 = `is`.read()
        val c2 = `is`.read()
        var c3 = `is`.read()
        mimeType = null
        height = -1
        width = height
        if (c1 == 'G'.toInt() && c2 == 'I'.toInt() && c3 == 'F'.toInt()) { // GIF
            `is`.skip(3)
            width = readInt(`is`, 2, false)
            height = readInt(`is`, 2, false)
            mimeType = "image/gif"
        } else if (c1 == 0xFF && c2 == 0xD8) { // JPG
            while (c3 == 255) {
                val marker = `is`.read()
                val len = readInt(`is`, 2, true)
                if (marker == 192 || marker == 193 || marker == 194) {
                    `is`.skip(1)
                    height = readInt(`is`, 2, true)
                    width = readInt(`is`, 2, true)
                    mimeType = "image/jpeg"
                    break
                }
                `is`.skip((len - 2).toLong())
                c3 = `is`.read()
            }
        } else if (c1 == 137 && c2 == 80 && c3 == 78) { // PNG
            `is`.skip(15)
            width = readInt(`is`, 2, true)
            `is`.skip(2)
            height = readInt(`is`, 2, true)
            mimeType = "image/png"
        } else if (c1 == 66 && c2 == 77) { // BMP
            `is`.skip(15)
            width = readInt(`is`, 2, false)
            `is`.skip(2)
            height = readInt(`is`, 2, false)
            mimeType = "image/bmp"
        } else if (c1 == 'R'.toInt() && c2 == 'I'.toInt() && c3 == 'F'.toInt()) { // WEBP
            val bytes = ByteArray(27)
            `is`.read(bytes)
            width = bytes[24].toInt() and 0xff shl 8 or (bytes[23].toInt() and 0xff)
            height = bytes[26].toInt() and 0xff shl 8 or (bytes[25].toInt() and 0xff)
            mimeType = "image/webp"
        } else {
            val c4 = `is`.read()
            if ((c1 == 'M'.toInt() && c2 == 'M'.toInt() && c3 == 0 && c4 == 42) || (c1 == 'I'.toInt() && c2 == 'I'.toInt() && c3 == 42 && c4 == 0)) { //TIFF
                val bigEndian = c1 == 'M'.toInt()
                val ifd: Int = readInt(`is`, 4, bigEndian)
                `is`.skip((ifd - 8).toLong())
                val entries: Int = readInt(`is`, 2, bigEndian)
                for (i in 1..entries) {
                    val tag = readInt(`is`, 2, bigEndian)
                    val fieldType = readInt(`is`, 2, bigEndian)
                    var valOffset: Int
                    if (fieldType == 3 || fieldType == 8) {
                        valOffset = readInt(`is`, 2, bigEndian)
                        `is`.skip(2)
                    } else {
                        valOffset = readInt(`is`, 4, bigEndian)
                    }
                    if (tag == 256) {
                        width = valOffset
                    } else if (tag == 257) {
                        height = valOffset
                    }
                    if (width != -1 && height != -1) {
                        mimeType = "image/tiff"
                        break
                    }
                }
            }
        }
        if (mimeType == null) {
            throw IOException("Unsupported image type")
        }
    }

    private fun readInt(`is`: InputStream, noOfBytes: Int, bigEndian: Boolean): Int {
        var ret = 0
        var sv = if (bigEndian) (noOfBytes - 1) * 8 else 0
        val cnt = if (bigEndian) -8 else 8
        for (i in 0 until noOfBytes) {
            ret = ret or (`is`.read() shl sv)
            sv += cnt
        }
        return ret
    }

    override fun toString(): String {
        return ("MIME Type : $mimeType\t Width : $width\t Height : $height")
    }
}