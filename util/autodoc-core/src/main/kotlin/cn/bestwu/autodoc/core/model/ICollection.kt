package cn.bestwu.autodoc.core.model

import cn.bestwu.autodoc.core.operation.DocOperation

interface ICollection {

    val name: String

    val operations: List<DocOperation>

}

