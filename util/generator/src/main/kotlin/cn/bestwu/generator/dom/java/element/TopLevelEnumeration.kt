package cn.bestwu.generator.dom.java.element

import cn.bestwu.generator.dom.java.JavaType

class TopLevelEnumeration
(type: JavaType) : InnerEnum(type), CompilationUnit {
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

            if (type.packageName.isNotEmpty()) {
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
            super.fields.forEach {
                importedTypes.add(it.type)
                it.annotations.needImportedTypes.forEach {
                    importedTypes.add(it)
                }
            }
            super.methods.forEach {
                importedTypes.add(it.returnType)
                it.parameters.forEach {
                    importedTypes.add(it.type)
                    it.annotations.needImportedTypes.forEach {
                        importedTypes.add(it)
                    }
                }
                it.annotations.needImportedTypes.forEach {
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

    override val superClass: JavaType
        get() = throw UnsupportedOperationException("")

    override val isJavaInterface: Boolean
        get() = false

    override val isJavaEnumeration: Boolean
        get() = true

}
