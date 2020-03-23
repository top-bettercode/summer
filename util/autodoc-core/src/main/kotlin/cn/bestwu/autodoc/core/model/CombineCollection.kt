package cn.bestwu.autodoc.core.model

import cn.bestwu.autodoc.core.operation.DocOperation
import com.fasterxml.jackson.annotation.JsonIgnore

data class CombineCollection(private val rootCollection: DocCollection, var projectCollection: DocCollection?) : ICollection {

    override var name: String = rootCollection.name

    override val operations: List<DocOperation>
        @JsonIgnore
        get() {
            return if (projectCollection != null) {
                val operations = mutableListOf<DocOperation>()
                operations.addAll(rootCollection.operations.filter { !projectCollection!!.operations.contains(it) })
                operations.addAll(projectCollection!!.operations)
                operations
            } else rootCollection.operations
        }

}

