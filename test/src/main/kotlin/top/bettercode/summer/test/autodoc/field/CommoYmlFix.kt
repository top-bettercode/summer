package top.bettercode.summer.test.autodoc.field

import com.fasterxml.jackson.databind.type.TypeFactory
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.autodoc.AutodocUtil.yamlMapper
import top.bettercode.summer.tools.autodoc.model.Field
import java.io.InputStream

/**
 *
 * @author Peter Wu
 */
class CommoYmlFix() : FieldDescFix() {

    private val log = LoggerFactory.getLogger(CommoYmlFix::class.java)

    override val fixChildren: Boolean = false

    private val requestHeaders: Set<Field> by lazy {
        commonFields(DocProperties.REQUEST_HEADERS.propertyName)
    }
    private val requestParameters: Set<Field> by lazy {
        commonFields(DocProperties.REQUEST_PARAMETERS.propertyName)
    }
    private val responseContent: Set<Field> by lazy {
        commonFields(DocProperties.RESPONSE_CONTENT.propertyName)
    }

    private fun commonFields(name: String): Set<Field> {
        return CommoYmlFix::class.java.getResourceAsStream("/field/$name.yml")!!.parseList(Field::class.java)
    }

    private fun <T> InputStream.parseList(clazz: Class<T>): LinkedHashSet<T> {
        return try {
            val collectionType = TypeFactory.defaultInstance()
                .constructCollectionType(LinkedHashSet::class.java, clazz)
            val set = yamlMapper.readValue<LinkedHashSet<T>>(this, collectionType)
                .filterNot { it == null }
            LinkedHashSet(set)
        } catch (e: Exception) {
            log.warn("$this>>${e.message}")
            linkedSetOf()
        }
    }


    override fun descFields(properties: DocProperties): Set<Field> {
        return when (properties) {
            DocProperties.REQUEST_HEADERS -> {
                requestHeaders
            }

            DocProperties.REQUEST_PARAMETERS -> {
                requestParameters
            }

            DocProperties.RESPONSE_CONTENT -> {
                responseContent
            }
        }
    }
}