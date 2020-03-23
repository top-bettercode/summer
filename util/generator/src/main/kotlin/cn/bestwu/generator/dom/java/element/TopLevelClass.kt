package cn.bestwu.generator.dom.java.element

import cn.bestwu.generator.dom.java.JavaType

/**
 * The Class TopLevelClass.
 */
class TopLevelClass(type: JavaType) : InnerClass(type), CompilationUnit {
    override val importedTypes: MutableSet<JavaType> = mutableSetOf()
    override val staticImports: MutableSet<String> = mutableSetOf()
    override val fileCommentLines: MutableList<String> = mutableListOf()

    override val formattedContent: String
        get() {
            val sb = StringBuilder()

            for (fileCommentLine in fileCommentLines) {
                sb.append(fileCommentLine)
                newLine(sb)
            }

            if (type.packageName.isNotBlank()) {
                sb.append("package ")
                sb.append(type.packageName)
                sb.append(';')
                newLine(sb)
                newLine(sb)
            }

            for (staticImport in staticImports) {
                sb.append("import static ")
                sb.append(staticImport)
                sb.append(';')
                newLine(sb)
            }

            if (staticImports.size > 0) {
                newLine(sb)
            }
            if (super.superClass != null) {
                importedTypes.add(super.superClass!!)
            }
            super.typeParameters.forEach {
                importedTypes.addAll(it.extendsTypes)
            }
            super.fields.forEach { field1 ->
                importedTypes.add(field1.type)
                field1.annotations.needImportedTypes.forEach {
                    importedTypes.add(it)
                }
            }
            super.methods.forEach { method ->
                importedTypes.add(method.returnType)
                method.parameters.forEach { parameter ->
                    importedTypes.add(parameter.type)
                    parameter.annotations.needImportedTypes.forEach {
                        importedTypes.add(it)
                    }
                }
                method.annotations.needImportedTypes.forEach {
                    importedTypes.add(it)
                }
            }
            super.superInterfaceTypes.forEach {
                importedTypes.add(it)
            }
            super.annotations.needImportedTypes.forEach {
                importedTypes.add(it)
            }

            val importStrings = calculateImports(importedTypes)
            for (importString in importStrings) {
                sb.append(importString)
                newLine(sb)
            }

            if (importStrings.isNotEmpty()) {
                newLine(sb)
            }

            sb.append(super.getFormattedContent(0, this))

            return sb.toString()
        }

    override val isJavaInterface: Boolean
        get() = false

    override val isJavaEnumeration: Boolean
        get() = false
}
