package top.bettercode.summer.tools.generator.dom.java

import top.bettercode.summer.tools.generator.dom.java.element.CompilationUnit
import top.bettercode.summer.tools.lang.util.JavaType

object JavaDomUtils {
    /**
     * Calculates type names for writing into generated Java.  We try to
     * use short names wherever possible.  If the type requires an import,
     * but has not been imported, then we need to use the fully qualified
     * type name.
     *
     * @param compilationUnit the compilation unit being written
     * @param fqjt the type in question
     */
    fun calculateTypeName(compilationUnit: CompilationUnit?, fqjt: JavaType): String {

        if (fqjt.typeArguments.size > 0) {
            return calculateParameterizedTypeName(compilationUnit, fqjt)
        }

        return if (compilationUnit == null
                || typeDoesNotRequireImport(fqjt)
                || typeIsInSamePackage(compilationUnit, fqjt)
                || typeIsAlreadyImported(compilationUnit, fqjt)
        ) {
            fqjt.shortName
        } else {
            fqjt.fullyQualifiedName
        }
    }

    private fun calculateParameterizedTypeName(
            compilationUnit: CompilationUnit?,
            fqjt: JavaType
    ): String {
        val baseTypeName = calculateTypeName(
                compilationUnit,
                JavaType(fqjt.fullyQualifiedNameWithoutTypeParameters)
        )

        val sb = StringBuilder()
        sb.append(baseTypeName)
        sb.append('<')
        var comma = false
        for (ft in fqjt.typeArguments) {
            if (comma) {
                sb.append(", ")
            } else {
                comma = true
            }
            sb.append(calculateTypeName(compilationUnit, ft))
        }
        sb.append('>')
        if (fqjt.isArray) {
            sb.append("[]")
        }
        return sb.toString()

    }

    private fun typeDoesNotRequireImport(fullyQualifiedJavaType: JavaType): Boolean {
        return fullyQualifiedJavaType.isPrimitive || !fullyQualifiedJavaType.isExplicitlyImported
    }

    private fun typeIsInSamePackage(
            compilationUnit: CompilationUnit,
            fullyQualifiedJavaType: JavaType
    ): Boolean {
        return fullyQualifiedJavaType
                .packageName == compilationUnit.type.packageName
    }

    private fun typeIsAlreadyImported(
            compilationUnit: CompilationUnit,
            fullyQualifiedJavaType: JavaType
    ): Boolean {
        return compilationUnit.importedTypes.flatMap { it.importList }
                .contains(fullyQualifiedJavaType.fullyQualifiedNameWithoutTypeParameters)
    }
}
