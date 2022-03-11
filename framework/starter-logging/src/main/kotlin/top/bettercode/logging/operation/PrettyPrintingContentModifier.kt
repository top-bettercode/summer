package top.bettercode.logging.operation

import com.sun.org.apache.xml.internal.utils.DefaultErrorHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import top.bettercode.lang.util.StringUtil
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult


/**
 * A ContentModifier that modifies the content by pretty printing it.
 *
 */
object PrettyPrintingContentModifier {

    private val log: Logger = LoggerFactory.getLogger(PrettyPrintingContentModifier::class.java)

    @JvmStatic
    fun modifyContent(originalContent: ByteArray): ByteArray {
        if (originalContent.isNotEmpty()) {
            for (prettyPrinter in PRETTY_PRINTERS) {
                try {
                    return prettyPrinter.prettyPrint(originalContent)
                } catch (_: Exception) {
                }
            }
        }
        return originalContent
    }

    @JvmStatic
    fun modifyContent(originalContent: String?): String {
        return String(modifyContent(originalContent?.toByteArray() ?: ByteArray(0)))
    }

    private interface PrettyPrinter {

        @Throws(Exception::class)
        fun prettyPrint(content: ByteArray): ByteArray

    }

    private class XmlPrettyPrinter : PrettyPrinter {

        private val transformerFactory = TransformerFactory.newInstance()
        private val parserFactory = SAXParserFactory.newInstance()

        @Throws(Exception::class)
        override fun prettyPrint(content: ByteArray): ByteArray {
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount",
                "4"
            )
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes")
            val transformed = ByteArrayOutputStream()
            transformer.errorListener = DefaultErrorHandler()
            transformer.transform(createSaxSource(content), StreamResult(transformed))

            return transformed.toByteArray()
        }

        @Throws(ParserConfigurationException::class, SAXException::class)
        private fun createSaxSource(original: ByteArray): SAXSource {
            val parser = parserFactory.newSAXParser()
            val xmlReader = parser.xmlReader
            xmlReader.errorHandler = DefaultErrorHandler()
            return SAXSource(xmlReader, InputSource(ByteArrayInputStream(original)))
        }

    }

    private class JsonPrettyPrinter : PrettyPrinter {

        @Throws(IOException::class)
        override fun prettyPrint(content: ByteArray): ByteArray {
            StringUtil.OBJECT_MAPPER.readTree(content)
            return StringUtil.prettyJson(String(content))!!.toByteArray()
        }

    }

    private val PRETTY_PRINTERS = Collections
        .unmodifiableList(
            listOf(JsonPrettyPrinter(), XmlPrettyPrinter())
        )

}
