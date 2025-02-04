package top.bettercode.summer.tools.autodoc.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.autodoc.AutodocUtil.yamlMapper
import top.bettercode.summer.tools.autodoc.operation.DocOperation
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2

@JsonPropertyOrder("name", "items")
data class DocCollection(
    override val name: String = "", var items: LinkedHashSet<String> = linkedSetOf(),
    /**
     * 集合目录
     */
    @JsonIgnore
    val dir: File
) : ICollection {

    private val log: Logger = LoggerFactory.getLogger(DocCollection::class.java)
    private fun operationFile(operationName: String): File = File(dir, "$operationName.yml")

    fun operation(operationName: String): DocOperation? {
        val operationFile = operationFile(operationName)
        return DocOperation.read(
            operationFile = operationFile,
            collectionName = name,
            operationName = operationName
        )
    }

    override val operations: List<DocOperation>
        @JsonIgnore
        get() = items.mapNotNull { operation(it) }

    companion object {

        fun read(file: File): LinkedHashSet<DocCollection> {
            return if (file.exists() && file.length() > 0) yamlMapper.readValue(
                file.inputStream(),
                DocCollections::class.java
            ).mapTo(linkedSetOf()) { (k, v) ->
                DocCollection(k, LinkedHashSet(v), File(file.parentFile, "collection/${k}"))
            } else linkedSetOf()
        }

        fun LinkedHashSet<DocCollection>.write(file: File) {
            file.writer().use { w ->
                w.write("")
                this.forEach { collection ->
                    w.appendLine("\"${collection.name}\":")
                    collection.items.forEach {
                        w.appendLine("  - \"$it\"")
                    }
                }
            }
        }

    }
}

