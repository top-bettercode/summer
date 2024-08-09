package top.bettercode.summer.tools.autodoc.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import top.bettercode.summer.tools.autodoc.operation.DocOperation
import java.io.File

@JsonPropertyOrder("name", "items")
@JsonIgnoreProperties(ignoreUnknown = true)
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

}

