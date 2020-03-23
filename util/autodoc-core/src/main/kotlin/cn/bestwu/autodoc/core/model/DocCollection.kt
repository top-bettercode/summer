package cn.bestwu.autodoc.core.model

import cn.bestwu.autodoc.core.Util
import cn.bestwu.autodoc.core.operation.DocOperation
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.io.File

@JsonPropertyOrder("name", "items")
@JsonIgnoreProperties(ignoreUnknown = true)
data class DocCollection(override var name: String = "", var items: LinkedHashSet<String> = linkedSetOf()) : ICollection {

    /**
     * 集合目录
     */
    @JsonIgnore
    lateinit var dir: File

    fun operationFile(operationName: String): File = File(dir, "$operationName.yml")

    fun operation(operationName: String): DocOperation? {
        val operationFile = operationFile(operationName)
        return if (operationFile.exists()) {
            val docOperation = Util.yamlMapper.readValue(operationFile, DocOperation::class.java)
            docOperation.operationFile = operationFile
            docOperation.collectionName = name
            docOperation.name = operationName
            if (docOperation.protocol.isBlank()) {
                docOperation.protocol = "HTTP/1.1"
            }
            docOperation
        } else {
            null
        }
    }

    override val operations: List<DocOperation>
        @JsonIgnore
        get() = items.mapNotNull { operation(it) }

    private fun File.fix(initText: String = ""): File {
        if (!this.exists()) {
            this.parentFile.mkdirs()
            this.writeText(initText)
            println("创建$this")
        }
        return this
    }
}

