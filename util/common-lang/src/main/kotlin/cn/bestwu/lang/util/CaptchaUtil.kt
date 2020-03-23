package cn.bestwu.lang.util

import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.OutputStream
import java.util.*
import javax.imageio.ImageIO

/**
 * 验证码工具类
 *
 * @author Peter Wu
 */
object CaptchaUtil {

    /**
     * 使用到Algerian字体，系统里没有的话需要安装字体，字体只显示大写，去掉了1,0,i,o几个容易混淆的字符
     */
    @JvmStatic
    val CAPTCHA_CODES = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    private val random = Random()

    /**
     * @return 随机int色值
     */
    private val randomIntColor: Int
        get() {
            val rgb = randomRgb
            var color = 0
            for (c in rgb) {
                color = color shl 8
                color = color or c
            }
            return color
        }

    /**
     * @return 随机Rgb色值
     */
    private val randomRgb: IntArray
        get() {
            val rgb = IntArray(3)
            for (i in 0..2) {
                rgb[i] = random.nextInt(255)
            }
            return rgb
        }

    /**
     * 使用系统默认字符源生成验证码
     *
     * @param size 验证码长度
     * @return 验证码
     */
    @JvmStatic
    fun generateCaptcha(size: Int): String {
        return generateCaptcha(size, CAPTCHA_CODES)
    }

    /**
     * 使用指定源生成验证码
     *
     * @param size 验证码长度
     * @param sources 验证码字符源
     * @return 验证码
     */
    private fun generateCaptcha(size: Int, sources: String?): String {
        var source = sources
        if (source == null || source.isEmpty()) {
            source = CAPTCHA_CODES
        }
        val codesLen = source.length
        val rand = Random(System.currentTimeMillis())
        val verifyCode = StringBuilder(size)
        for (i in 0 until size) {
            verifyCode.append(source[rand.nextInt(codesLen - 1)])
        }
        return verifyCode.toString()
    }

    /**
     * 输出指定验证码图片流
     *
     * @param w 宽度
     * @param h 高度
     * @param os 输出流
     * @param code 验证码
     * @throws IOException IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun generateImage(w: Int, h: Int, os: OutputStream, code: String) {
        val verifySize = code.length
        val image = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        val rand = Random()
        val g2 = image.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val colors = arrayOfNulls<Color>(5)
        val colorSpaces = arrayOf(Color.WHITE, Color.CYAN, Color.GRAY, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.YELLOW)
        val fractions = FloatArray(colors.size)
        for (i in colors.indices) {
            colors[i] = colorSpaces[rand.nextInt(colorSpaces.size)]
            fractions[i] = rand.nextFloat()
        }
        Arrays.sort(fractions)

        g2.color = Color.GRAY// 设置边框色
        g2.fillRect(0, 0, w, h)

        val c = getRandColor(200, 250)
        g2.color = c// 设置背景色
        g2.fillRect(0, 2, w, h - 4)

        // 绘制干扰线
        val random = Random()
        g2.color = getRandColor(160, 200)// 设置线条的颜色
        for (i in 0..19) {
            val x = random.nextInt(w - 1)
            val y = random.nextInt(h - 1)
            val xl = random.nextInt(6) + 1
            val yl = random.nextInt(12) + 1
            g2.drawLine(x, y, x + xl + 40, y + yl + 20)
        }

        // 添加噪点
        val yawpRate = 0.05f// 噪声率
        val area = (yawpRate * w.toFloat() * h.toFloat()).toInt()
        for (i in 0 until area) {
            val x = random.nextInt(w)
            val y = random.nextInt(h)
            val rgb = randomIntColor
            image.setRGB(x, y, rgb)
        }

        shear(g2, w, h, c)// 使图片扭曲

        g2.color = getRandColor(60, 80)
        val fontSize = h - 4
        val font = Font("Algerian", Font.ITALIC, fontSize)
        g2.font = font
        val chars = code.toCharArray()
        for (i in 0 until verifySize) {
            val affine = AffineTransform()
            affine.setToRotation(Math.PI / 4 * rand.nextDouble() * (if (rand.nextBoolean()) 1 else -1).toDouble(),
                    (w / verifySize * i + fontSize / 2).toDouble(), (h / 2).toDouble())
            g2.transform = affine
            g2.drawChars(chars, i, 1, (w - 10) / verifySize * i + 5, h / 2 + fontSize / 2 - 10)
        }

        g2.dispose()
        ImageIO.write(image, "jpg", os)
    }

    /**
     * @param fc 开始色值
     * @param bc 结束色值
     * @return 随机色
     */
    private fun getRandColor(fc: Int, bc: Int): Color {
        var f = fc
        var bco = bc
        if (f > 255) {
            f = 255
        }
        if (bco > 255) {
            bco = 255
        }
        val r = f + random.nextInt(bco - f)
        val g = f + random.nextInt(bco - f)
        val b = f + random.nextInt(bco - f)
        return Color(r, g, b)
    }

    /**
     * 干扰线
     *
     * @param g 图像
     * @param w1 宽度
     * @param h1 高度
     * @param color 颜色
     */
    private fun shear(g: Graphics, w1: Int, h1: Int, color: Color) {
        shearX(g, w1, h1, color)
        shearY(g, w1, h1, color)
    }

    /**
     * X干扰线
     *
     * @param g 图像
     * @param w1 宽度
     * @param h1 高度
     * @param color 颜色
     */
    private fun shearX(g: Graphics, w1: Int, h1: Int, color: Color) {

        val period = random.nextInt(2)

        val frames = 1
        val phase = random.nextInt(2)

        for (i in 0 until h1) {
            val d = (period shr 1).toDouble() * Math.sin(
                    i.toDouble() / period.toDouble() + 6.2831853071795862 * phase.toDouble() / frames.toDouble())
            g.copyArea(0, i, w1, 1, d.toInt(), 0)
            g.color = color
            g.drawLine(d.toInt(), i, 0, i)
            g.drawLine(d.toInt() + w1, i, w1, i)
        }

    }

    /**
     * Y干扰线
     *
     * @param g 图像
     * @param w1 宽度
     * @param h1 高度
     * @param color 颜色
     */
    private fun shearY(g: Graphics, w1: Int, h1: Int, color: Color) {

        val period = random.nextInt(40) + 10 // 50;

        val frames = 20
        val phase = 7
        for (i in 0 until w1) {
            val d = (period shr 1).toDouble() * Math.sin(
                    i.toDouble() / period.toDouble() + 6.2831853071795862 * phase.toDouble() / frames.toDouble())
            g.copyArea(i, 0, 1, h1, 0, d.toInt())
            g.color = color
            g.drawLine(i, d.toInt(), i, 0)
            g.drawLine(i, d.toInt() + h1, i, h1)

        }

    }

}
