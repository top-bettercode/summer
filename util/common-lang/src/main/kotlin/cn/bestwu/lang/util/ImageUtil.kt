package cn.bestwu.lang.util

import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.imageio.ImageIO

/**
 * 图像处理工具类
 */
object ImageUtil {

    /**
     * 读取图像
     *
     * @param file 文件
     * @return 图像
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    fun readImage(file: File): BufferedImage {
        return ImageIO.read(file)
    }

    /**
     * 读取图像
     *
     * @param inputStream 输入流
     * @return 图像
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun readImage(inputStream: InputStream): BufferedImage {
        val image = ImageIO.read(inputStream) // 构造Image对象
        inputStream.close()
        return image
    }

    /**
     * 把图像写入文件
     *
     * @param im 图像
     * @param formatName 格式名
     * @param file 文件
     * @return 是否成功
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun writeImage(im: RenderedImage, formatName: String, file: File): Boolean {
        return ImageIO.write(im, formatName, file)
    }

    /**
     * 把图像写入流
     *
     * @param im 图像
     * @param formatName 格式名
     * @param outputStream 输出流
     * @return 是否成功
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun writeImage(im: RenderedImage, formatName: String, outputStream: OutputStream): Boolean {
        return ImageIO.write(im, formatName, outputStream)
    }

    /**
     * 根据文件名创建ImageBuilder
     *
     * @param fileName 文件名
     * @return ImageBuilder
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun builder(fileName: String): ImageBuilder {
        return ImageBuilder(File(fileName))
    }

    /**
     * 根据文件创建ImageBuilder
     *
     * @param file 文件
     * @return ImageBuilder
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun builder(file: File): ImageBuilder {
        return ImageBuilder(file)
    }

    /**
     * 根据输入流创建ImageBuilder
     *
     * @param inputStream 输入流
     * @return ImageBuilder
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun builder(inputStream: InputStream): ImageBuilder {
        return ImageBuilder(inputStream)
    }

}
