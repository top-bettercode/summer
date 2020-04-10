package cn.bestwu.autodoc.core

import cn.bestwu.autodoc.core.model.Field

/**
 *
 * @author Peter Wu
 */
abstract class AbstractbGenerator {

    fun Set<Field>.check(operationPath: String = ""): Set<Field> {
        this.filter { it.description.isBlank() }.forEach {
            System.err.println("[$operationPath]未找到字段[${it.name}]的描述")
        }
        return this
    }
}