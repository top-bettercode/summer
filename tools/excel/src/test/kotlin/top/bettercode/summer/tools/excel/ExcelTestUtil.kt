package top.bettercode.summer.tools.excel

import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 *
 * @author Peter Wu
 */
object ExcelTestUtil {
    @JvmStatic
    fun openExcel(@Suppress("UNUSED_PARAMETER") x: String) {
//        Runtime.getRuntime().exec(arrayOf("xdg-open", System.getProperty("user.dir") + "/" + x))
    }


    @JvmStatic
    fun numberImage(number: Int): ByteArray {
        // 创建一个 BufferedImage 对象，指定宽度、高度和图像类型
        val width = 400
        val height = 200
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        // 获取图形上下文
        val graphics: Graphics2D = image.createGraphics()

        // 设置背景颜色和绘图颜色
        graphics.paint = java.awt.Color.LIGHT_GRAY
        graphics.fillRect(0, 0, width, height)
        graphics.paint = java.awt.Color.RED

        // 设置字体和大小
        val font = Font("Arial", Font.PLAIN, 100)
        graphics.font = font

        // 计算数字的宽度和高度，以便居中显示
        val fontMetrics = graphics.fontMetrics
        val stringWidth = fontMetrics.stringWidth(number.toString())
        val stringHeight = fontMetrics.height
        val x = (width - stringWidth) / 2
        val y = (height + stringHeight) / 2

        // 绘制数字
        graphics.drawString(number.toString(), x, y)

        // 释放图形上下文
        graphics.dispose()

        // 将图像写入到 ByteArrayOutputStream
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)

        // 返回字节数组
        return outputStream.toByteArray()
    }
}