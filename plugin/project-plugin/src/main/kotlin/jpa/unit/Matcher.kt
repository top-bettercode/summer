package jpa.unit

import ProjectGenerator
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.JavaVisibility
import top.bettercode.generator.dom.java.element.Parameter
import top.bettercode.generator.dom.java.element.TopLevelClass

/**
 *
 * @author Peter Wu
 */
val matcher: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/**"
            +" * $remarks SpecMatcher"
            +" */"
        }

        superClass(
            JavaType("top.bettercode.simpleframework.data.jpa.query.DefaultSpecMatcher").typeArgument(
                type
            )
        )

        val modeType = JavaType("SpecMatcherMode")

        constructor(Parameter("mode", modeType)) {
            this.visibility = JavaVisibility.PRIVATE
            +"super(mode);"
        }


        //创建实例
        method("matching", type) {
            this.isStatic = true
            javadoc {
                +"/**"
                +" * 创建 SpecMatcher 实例"
                +" *"
                +" * @return $remarks SpecMatcher 实例"
                +" */"
            }
            +"return matchingAll();"
        }

        method("matchingAll", type) {
            this.isStatic = true
            javadoc {
                +"/**"
                +" * 创建 SpecMatcher 实例"
                +" *"
                +" * @return $remarks SpecMatcher 实例"
                +" */"
            }
            +"return new ${type.shortName}(SpecMatcherMode.ALL);"
        }

        method("matchingAny", type) {
            this.isStatic = true
            javadoc {
                +"/**"
                +" * 创建 SpecMatcher 实例"
                +" *"
                +" * @return $remarks SpecMatcher 实例"
                +" */"
            }
            +"return new ${type.shortName}(SpecMatcherMode.ANY);"
        }

        val pathType =
            JavaType("top.bettercode.simpleframework.data.jpa.query.SpecPath").typeArgument(
                type
            )
        val matcherType =
            JavaType("top.bettercode.simpleframework.data.jpa.query.PathMatcher")
        //primaryKey
        if (isCompositePrimaryKey) {
            primaryKeys.forEach {
                val javaName =
                    if (it.javaName == "spec") "specField" else it.javaName
                method(javaName, pathType) {
                    this.visibility = JavaVisibility.PUBLIC
                    +"return super.specPath(\"${primaryKeyName}.${it.javaName}\");"
                }
                method(
                    javaName,
                    type,
                    Parameter(it.javaName, it.javaType)
                ) {
                    this.visibility = JavaVisibility.PUBLIC
                    +"super.specPath(\"${primaryKeyName}.${it.javaName}\").setValue(${it.javaName});"
                    +"return this;"
                }
                method(
                    javaName,
                    type,
                    Parameter(it.javaName, it.javaType),
                    Parameter(
                        "matcher",
                        matcherType
                    )
                ) {
                    this.visibility = JavaVisibility.PUBLIC
                    +"super.specPath(\"${primaryKeyName}.${it.javaName}\").setValue(${it.javaName}).withMatcher(matcher);"
                    +"return this;"
                }
            }
        } else {
            val javaName =
                if (primaryKeyName == "spec") "specField" else primaryKeyName
            method(javaName, pathType) {
                this.visibility = JavaVisibility.PUBLIC
                +"return super.specPath(\"${primaryKeyName}\");"
            }
            method(
                javaName,
                type,
                Parameter(primaryKeyName, primaryKeyType)
            ) {
                this.visibility = JavaVisibility.PUBLIC
                +"super.specPath(\"${primaryKeyName}\").setValue(${primaryKeyName});"
                +"return this;"
            }
            method(
                javaName,
                type,
                Parameter(primaryKeyName, primaryKeyType),
                Parameter(
                    "matcher",
                    matcherType
                )
            ) {
                this.visibility = JavaVisibility.PUBLIC
                +"super.specPath(\"${primaryKeyName}\").setValue(${primaryKeyName}).withMatcher(matcher);"
                +"return this;"
            }
        }


        otherColumns.forEach {
            val javaName =
                if (it.javaName == "spec") "specField" else it.javaName
            method(javaName, pathType) {
                this.visibility = JavaVisibility.PUBLIC
                +"return super.specPath(\"${it.javaName}\");"
            }
            method(javaName, type, Parameter(it.javaName, it.javaType)) {
                this.visibility = JavaVisibility.PUBLIC
                +"super.specPath(\"${it.javaName}\").setValue(${it.javaName});"
                +"return this;"
            }
            method(
                javaName, type, Parameter(it.javaName, it.javaType),
                Parameter(
                    "matcher",
                    matcherType
                )
            ) {
                this.visibility = JavaVisibility.PUBLIC
                +"super.specPath(\"${it.javaName}\").setValue(${it.javaName}).withMatcher(matcher);"
                +"return this;"
            }
        }
    }
}