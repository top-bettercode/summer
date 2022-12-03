package top.bettercode.summer.tools.autodoc.model

import top.bettercode.summer.tools.autodoc.operation.DocOperation

interface ICollection {

    val name: String

    val operations: List<DocOperation>

}

