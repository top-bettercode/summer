package top.bettercode.logging.operation

import io.micrometer.core.instrument.util.JsonUtils
import org.xml.sax.ErrorHandler
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import top.bettercode.lang.util.StringUtil
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.ErrorListener
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult

/**
 * A ContentModifier that modifies the content by pretty printing it.
 *
 */
object PrettyPrintingContentModifier {

    @JvmStatic
    fun modifyContent(originalContent: ByteArray): ByteArray {
        if (originalContent.isNotEmpty()) {
            for (prettyPrinter in PRETTY_PRINTERS) {
                try {
                    return prettyPrinter.prettyPrint(originalContent)
                } catch (ex: Exception) {
                    // Continue
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
            transformer.errorListener = SilentErrorListener()
            transformer.transform(createSaxSource(content), StreamResult(transformed))

            return transformed.toByteArray()
        }

        @Throws(ParserConfigurationException::class, SAXException::class)
        private fun createSaxSource(original: ByteArray): SAXSource {
            val parser = parserFactory.newSAXParser()
            val xmlReader = parser.xmlReader
            xmlReader.errorHandler = SilentErrorHandler()
            return SAXSource(xmlReader, InputSource(ByteArrayInputStream(original)))
        }

        private class SilentErrorListener : ErrorListener {

            @Throws(TransformerException::class)
            override fun warning(exception: TransformerException) {
                // Suppress
            }

            @Throws(TransformerException::class)
            override fun error(exception: TransformerException) {
                // Suppress
            }

            @Throws(TransformerException::class)
            override fun fatalError(exception: TransformerException) {
                // Suppress
            }

        }

        private class SilentErrorHandler : ErrorHandler {

            @Throws(SAXException::class)
            override fun warning(exception: SAXParseException) {
                // Suppress
            }

            @Throws(SAXException::class)
            override fun error(exception: SAXParseException) {
                // Suppress
            }

            @Throws(SAXException::class)
            override fun fatalError(exception: SAXParseException) {
                // Suppress
            }

        }

    }

    private class JsonPrettyPrinter : PrettyPrinter {

        @Throws(IOException::class)
        override fun prettyPrint(content: ByteArray): ByteArray {
            StringUtil.OBJECT_MAPPER.readTree(content)
            return JsonUtils.prettyPrint(String(content)).toByteArray()
        }

    }

    private val PRETTY_PRINTERS = Collections
        .unmodifiableList(
            listOf(JsonPrettyPrinter(), XmlPrettyPrinter())
        )

}
