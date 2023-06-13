package top.bettercode.summer.tools.lang.operation

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xml.sax.ErrorHandler
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import top.bettercode.summer.tools.lang.util.StringUtil
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

            fun prettyPrint(content: ByteArray): ByteArray

    }

    private class XmlPrettyPrinter : PrettyPrinter {

        private val transformerFactory = TransformerFactory.newInstance()
        private val parserFactory = SAXParserFactory.newInstance()
        private val errorListener = object : ErrorListener, ErrorHandler {
            override fun warning(e: TransformerException) {
                log.warn("XML parsing warning: ${e.message}")
            }

            override fun fatalError(e: TransformerException) {
                throw e
            }

            override fun error(e: TransformerException) {
                throw e
            }

            override fun warning(e: SAXParseException) {
                log.warn("XML parsing warning: ${e.message}")
            }

            override fun error(e: SAXParseException) {
                throw e
            }

            override fun fatalError(e: SAXParseException) {
                throw e
            }
        }

            override fun prettyPrint(content: ByteArray): ByteArray {
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount",
                "4"
            )
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes")
            val transformed = ByteArrayOutputStream()
            transformer.errorListener = errorListener
            transformer.transform(createSaxSource(content), StreamResult(transformed))

            return transformed.toByteArray()
        }

            private fun createSaxSource(original: ByteArray): SAXSource {
            val parser = parserFactory.newSAXParser()
            val xmlReader = parser.xmlReader
            xmlReader.errorHandler = errorListener
            return SAXSource(xmlReader, InputSource(ByteArrayInputStream(original)))
        }

    }

    private class JsonPrettyPrinter : PrettyPrinter {

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
