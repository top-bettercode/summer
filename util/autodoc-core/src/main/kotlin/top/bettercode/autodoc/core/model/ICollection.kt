package top.bettercode.autodoc.core.model

import top.bettercode.autodoc.core.operation.DocOperation

interface ICollection {

    val name: String

    val operations: List<DocOperation>

}

