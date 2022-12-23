package top.bettercode.summer.gradle.plugin.project.template.unit

import top.bettercode.summer.gradle.plugin.project.template.ProjectGenerator
import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.JavaVisibility
import top.bettercode.summer.tools.generator.dom.java.element.Parameter
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass

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
            JavaType("top.bettercode.summer.data.jpa.query.SpecMatcher").typeArgument(
                entityType, type
            )
        )

        serialVersionUID()

        val modeType = JavaType("top.bettercode.summer.data.jpa.query.SpecMatcherMode")

        constructor(Parameter("mode", modeType), Parameter("probe", entityType)) {
            this.visibility = JavaVisibility.PRIVATE
            +"super(mode, probe);"
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
            +"return new ${type.shortName}(SpecMatcherMode.ALL, null);"
        }

        method("matching", type, Parameter("probe", entityType)) {
            this.isStatic = true
            javadoc {
                +"/**"
                +" * 创建 SpecMatcher 实例"
                +" *"
                +" * @return $remarks SpecMatcher 实例"
                +" */"
            }
            +"return new ${type.shortName}(SpecMatcherMode.ALL, probe);"
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
            +"return new ${type.shortName}(SpecMatcherMode.ANY, null);"
        }

        method("matchingAny", type, Parameter("probe", entityType)) {
            this.isStatic = true
            javadoc {
                +"/**"
                +" * 创建 SpecMatcher 实例"
                +" *"
                +" * @return $remarks SpecMatcher 实例"
                +" */"
            }
            +"return new ${type.shortName}(SpecMatcherMode.ANY, probe);"
        }

        val pathType =
            JavaType("top.bettercode.summer.data.jpa.query.SpecPath").typeArgument(
                entityType, type
            )
        val matcherType =
            JavaType("top.bettercode.summer.data.jpa.query.PathMatcher")
        //primaryKey
        if (isCompositePrimaryKey) {
            primaryKeys.forEach {
                val javaName =
                    if (it.javaName == "spec") "specField" else it.javaName
                method(javaName, pathType) {
                    javadoc {
                        +"/**"
                        +" * @return ${it.remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                        +" */"
                    }
                    this.visibility = JavaVisibility.PUBLIC
                    +"return super.specPath(\"${primaryKeyName}.${it.javaName}\");"
                }
                method(
                    javaName,
                    type,
                    Parameter(it.javaName, it.javaType)
                ) {
                    javadoc {
                        +"/**"
                        +" * ${it.paramRemark}"
                        +" * @return ${it.remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                        +" */"
                    }
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
                    javadoc {
                        +"/**"
                        +" * ${it.paramRemark}"
                        +" * @param matcher PathMatcher"
                        +" * @return ${it.remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                        +" */"
                    }
                    this.visibility = JavaVisibility.PUBLIC
                    +"super.specPath(\"${primaryKeyName}.${it.javaName}\").setValue(${it.javaName}).withMatcher(matcher);"
                    +"return this;"
                }
            }
        } else {
            val javaName =
                if (primaryKeyName in arrayOf(
                        "asc",
                        "desc",
                        "specPath"
                    )
                ) "${primaryKeyName}Field" else primaryKeyName
            method(javaName, pathType) {
                javadoc {
                    +"/**"
                    +" * @return ${primaryKey.remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                    +" */"
                }
                this.visibility = JavaVisibility.PUBLIC
                +"return super.specPath(\"${primaryKeyName}\");"
            }
            method(
                javaName,
                type,
                Parameter(primaryKeyName, primaryKeyType)
            ) {
                javadoc {
                    +"/**"
                    +" * ${primaryKey.paramRemark}"
                    +" * @return ${primaryKey.remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                    +" */"
                }
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
                javadoc {
                    +"/**"
                    +" * ${primaryKey.paramRemark}"
                    +" * @param matcher PathMatcher"
                    +" * @return ${primaryKey.remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                    +" */"
                }
                this.visibility = JavaVisibility.PUBLIC
                +"super.specPath(\"${primaryKeyName}\").setValue(${primaryKeyName}).withMatcher(matcher);"
                +"return this;"
            }
        }


        otherColumns.forEach {
            val javaName =
                if (it.javaName in arrayOf(
                        "asc",
                        "desc",
                        "specPath"
                    )
                ) "${it.javaName}Field" else it.javaName
            method(javaName, pathType) {
                javadoc {
                    +"/**"
                    +" * @return ${it.remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                    +" */"
                }
                this.visibility = JavaVisibility.PUBLIC
                +"return super.specPath(\"${it.javaName}\");"
            }
            method(javaName, type, Parameter(it.javaName, it.javaType)) {
                javadoc {
                    +"/**"
                    +" * ${it.paramRemark}"
                    +" * @return ${it.remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                    +" */"
                }
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
                javadoc {
                    +"/**"
                    +" * ${it.paramRemark}"
                    +" * @param matcher PathMatcher"
                    +" * @return ${it.remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                    +" */"
                }
                this.visibility = JavaVisibility.PUBLIC
                +"super.specPath(\"${it.javaName}\").setValue(${it.javaName}).withMatcher(matcher);"
                +"return this;"
            }
        }
    }
}