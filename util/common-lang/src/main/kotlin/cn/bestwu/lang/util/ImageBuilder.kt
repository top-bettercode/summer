package cn.bestwu.lang.util

import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.OutputStream
import javax.imageio.ImageIO

/**
 * 图像Builder
 */
class ImageBuilder @Throws(IOException::class)
constructor(inputStream: Any) {

    private var bufferedImage: BufferedImage
    /**
     * @return 格式名
     */
    var formatName: String
        private set
    /**
     * @return 宽度
     */
    var width: Int = 0
        private set
    /**
     * @return 高度
     */
    var height: Int = 0
        private set
    /**
     * @return 格式
     */
    var type: Int = 0
        private set

    init {
        val iis = ImageIO.createImageInputStream(inputStream)
        val readers = ImageIO.getImageReaders(iis)
        val reader = readers.next()
        reader.input = iis
        formatName = reader.formatName
        bufferedImage = reader.read(0)
        width = bufferedImage.width
        height = bufferedImage.height
        type = bufferedImage.type
        if (type == BufferedImage.TYPE_CUSTOM) {
            type = BufferedImage.TYPE_INT_ARGB
        }
        reader.dispose()
        iis.close()
    }

    /**
     * 按宽高缩放
     *
     * @param width 宽度
     * @param height 高度
     * @param origin 从哪裁剪
     * @return ImageBuilder
     */
    @JvmOverloads
    fun scaleTrim(width: Int?, height: Int?, origin: Origin? = Origin.CENTER): ImageBuilder {
        var w = width
        var h = height
        var o = origin
        var scale = 1.0
        if (w == null) {
            w = this.width
        }
        if (h == null) {
            h = this.height
        }
        if (o == null) {
            o = Origin.CENTER
        }
        scale = w * scale / this.width
        val newheight = this.height * scale
        if (h > newheight) {
            scale = h * scale / newheight
        }
        if (scale < 1) {
            scale(scale)
        }
        sourceRegion(w, h, o)
        return this
    }

    /**
     * 根据最小高宽 自动缩放
     *
     * @param min_width -1 表示不限制
     * @param min_hight -1 表示不限制
     * @return ImageBuilder
     */
    fun autoScale(min_width: Int, min_hight: Int): ImageBuilder {
        if (this.width <= min_width || this.height <= min_hight) {
            return scale(1.0)
        } else {
            val w = min_width * 1.0 / this.width
            val h = min_hight * 1.0 / this.height
            if (min_hight == -1) {
                return scale(w)
            }
            return if (min_width == -1) {
                scale(h)
            } else scale(if (w > h) w else h)
        }
    }

    /**
     * @param scale 缩放比例
     * @return ImageBuilder
     */
    fun scale(scale: Double): ImageBuilder {
        if (scale != 1.0) {
            width = Math.ceil(this.width * scale).toInt()
            height = Math.ceil(this.height * scale).toInt()
            val image = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH)
            val bufImg = BufferedImage(image.getWidth(null), image.getHeight(null), type)
            val g = bufImg.createGraphics()
            g.drawImage(image, 0, 0, null)
            g.dispose()
            bufImg.flush()
            bufferedImage = bufImg
        }
        return this
    }

    /**
     * 按宽高及区域裁剪
     *
     * @param width 宽度
     * @param height 高度
     * @param origin 区域
     * @return ImageBuilder
     */
    @JvmOverloads
    fun sourceRegion(width: Int, height: Int, origin: Origin = Origin.CENTER): ImageBuilder {
        var w = width
        var h = height
        if (w > this.width) {
            w = this.width
        }
        if (h > this.height) {
            h = this.height
        }
        if (h == this.height && w == this.width) {
            return this
        }
        var x = 0
        var y = 0
        when (origin) {
            ImageBuilder.Origin.LEFT_BOTTOM -> y = this.height - h
            ImageBuilder.Origin.LEFT_TOP -> {
            }
            ImageBuilder.Origin.RIGHT_BOTTOM -> {
                x = this.width - w
                y = this.height - h
            }
            ImageBuilder.Origin.RIGHT_TOP -> x = this.width - w
            ImageBuilder.Origin.CENTER -> {
                x = (this.width - w) / 2
                y = (this.height - h) / 2
            }
        }
        return sourceRegion(x, y, w, h)
    }

    /**
     * 按位置裁剪
     *
     * @param x 起点X
     * @param y 起点Y
     * @param width 宽度
     * @param height 高度
     * @return ImageBuilder
     */
    fun sourceRegion(x: Int, y: Int, width: Int, height: Int): ImageBuilder {
        if (width < this.width) {
            this.width = width
        }
        if (height < this.height) {
            this.height = height
        }
        bufferedImage = bufferedImage.getSubimage(x, y, this.width, this.height)
        return this
    }

    /**
     * 输出到文件
     *
     * @param fileName 文件名
     * @return 文件
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    fun toFile(fileName: String): Boolean {
        return ImageUtil.writeImage(bufferedImage, formatName, File(fileName))
    }

    /**
     * 输出到文件
     *
     * @param file 文件
     * @return 文件
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    fun toFile(file: File): Boolean {
        return ImageUtil.writeImage(bufferedImage, formatName, file)
    }

    /**
     * 输出到流
     *
     * @param os 输出流
     * @return 是否成功
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    fun toOutputStream(os: OutputStream): Boolean {
        return ImageUtil.writeImage(bufferedImage, formatName, os)
    }

    /**
     * 设置给出格式
     *
     * @param formatName 格式名
     * @return ImageBuilder
     */
    fun outputFormat(formatName: String): ImageBuilder {
        if (StringUtil.hasText(formatName) && !this.formatName.equals(formatName, ignoreCase = true)) {
            /*
       * Note: The following code is a workaround for the JPEG writer
       * which ships with the JDK.
       *
       * At issue is, that the JPEG writer appears to write the alpha
       * channel when it should not. To circumvent this, images which are
       * to be saved as a JPEG will be copied to another BufferedImage
       * without an alpha channel before it is saved.
       *
       * Also, the BMP writer appears not to support ARGB, so an RGB image
       * will be produced before saving.
       */
            if (formatName.equals("jpg", ignoreCase = true) || formatName.equals("jpeg", ignoreCase = true) || formatName
                            .equals("bmp", ignoreCase = true)) {
                bufferedImage = copy(bufferedImage, BufferedImage.TYPE_INT_RGB)
            }
            this.formatName = formatName
        }
        return this
    }

    /**
     * 按格式copy
     *
     * @param bufferedImage 图像
     * @param imageType 图像格式
     * @return 新图像
     */
    private fun copy(bufferedImage: BufferedImage, imageType: Int): BufferedImage {
        val width = bufferedImage.width
        val height = bufferedImage.height

        val newImage = BufferedImage(width, height, imageType)
        val g = newImage.createGraphics()
        g.color = Color.WHITE
        g.fillRect(0, 0, width, height)
        g.drawImage(bufferedImage, 0, 0, null)

        g.dispose()

        return newImage

    }

    fun asBufferedImages(): BufferedImage? {
        return this.bufferedImage
    }

    /**
     * 图像区域
     */
    enum class Origin {
        /**
         * 左下
         */
        LEFT_BOTTOM,
        /**
         * 左上
         */
        LEFT_TOP,
        /**
         * 右下
         */
        RIGHT_BOTTOM,
        /**
         * 左上
         */
        RIGHT_TOP,
        /**
         * 中心
         */
        CENTER
    }

}