package top.bettercode.summer.gradle.plugin.project.template.unit

import top.bettercode.summer.gradle.plugin.project.template.ProjectGenerator
import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.generator.dom.java.element.JavaVisibility
import top.bettercode.summer.tools.generator.dom.java.element.Parameter
import top.bettercode.summer.tools.generator.dom.java.element.TopLevelClass
import top.bettercode.summer.tools.lang.capitalized

/**
 *
 * @author Peter Wu
 */
val matcher: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/** $remarks SpecMatcher */"
        }

        superClass(
                JavaType("top.bettercode.summer.data.jpa.query.SpecMatcher").typeArgument(
                        entityType, type
                )
        )

        serialVersionUID()

        val modeType = JavaType("top.bettercode.summer.data.jpa.query.SpecMatcherMode")

        constructor(Parameter("mode", modeType), Parameter("probe", JavaType.objectInstance)) {
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

        method("matching", type, Parameter("probe", JavaType.objectInstance)) {
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

        method("matchingAny", type, Parameter("probe", JavaType.objectInstance)) {
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

        val matcherType =
                JavaType("top.bettercode.summer.data.jpa.query.PathMatcher")
        val existMethodNames = arrayOf(
                "between", "ne", "all", "any", "lt", "criteriaUpdate", "asc", "containing", "toPredicate", "createCriteriaUpdate", "notEqual", "gt", "ge", "like", "notLike", "criteria", "starting", "sortBy", "notIn", "getMatchMode", "getIdAttribute", "setIdAttribute", "getVersionAttribute", "setVersionAttribute", "ending", "notStarting", "notEnding", "notContaining", "in", "desc", "le", "path", "eq", "equal", "wait", "equals", "toString", "hashCode", "getClass", "notify", "notifyAll", "or", "and"
        )
        //primaryKey
        val primaryKeyMethodName = if (primaryKeyName in existMethodNames) "${primaryKeyName}Field" else primaryKeyName
        val remark = if (isCompositePrimaryKey) "${remarks}主键" else primaryKey.remark
        val paramRemark = if (isCompositePrimaryKey) "@param $primaryKeyName 主键" else primaryKey.paramRemark
        method(primaryKeyMethodName, JavaType("top.bettercode.summer.data.jpa.query.SpecPath").typeArgument(
                primaryKeyType,
                entityType, type
        )) {
            javadoc {
                +"/**"
                +" * @return ${remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                +" */"
            }
            this.visibility = JavaVisibility.PUBLIC
            +"return super.path(\"${primaryKeyName}\");"
        }
        method(
                primaryKeyMethodName,
                type,
                Parameter(primaryKeyName, primaryKeyType)
        ) {
            javadoc {
                +"/**"
                +" * $paramRemark"
                +" * @return ${remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                +" */"
            }
            this.visibility = JavaVisibility.PUBLIC
            +"return super.criteria(\"${primaryKeyName}\", ${primaryKeyName});"
        }
        method(
                "set${primaryKeyName.capitalized()}",
                type,
                Parameter(primaryKeyName, primaryKeyType)
        ) {
            javadoc {
                +"/**"
                +" * $paramRemark"
                +" * @return ${remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                +" */"
            }
            this.visibility = JavaVisibility.PUBLIC
            +"return super.criteriaUpdate(\"${primaryKeyName}\", ${primaryKeyName});"
        }
        method(
                primaryKeyMethodName,
                type,
                Parameter(primaryKeyName, primaryKeyType),
                Parameter(
                        "matcher",
                        matcherType
                )
        ) {
            javadoc {
                +"/**"
                +" * $paramRemark"
                +" * @param matcher PathMatcher"
                +" * @return ${remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                +" */"
            }
            this.visibility = JavaVisibility.PUBLIC
            +"return super.criteria(\"${primaryKeyName}\", ${primaryKeyName}, matcher);"
        }

        if (isCompositePrimaryKey) {
            primaryKeys.forEach {
                val methodName =
                        if (it.javaName == "spec") "specField" else it.javaName
                method(methodName, JavaType("top.bettercode.summer.data.jpa.query.SpecPath").typeArgument(
                        it.javaType,
                        entityType, type
                )) {
                    javadoc {
                        +"/**"
                        +" * @return ${it.remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                        +" */"
                    }
                    this.visibility = JavaVisibility.PUBLIC
                    +"return super.path(\"${primaryKeyName}.${it.javaName}\");"
                }
                method(
                        methodName,
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
                    +"return super.criteria(\"${primaryKeyName}.${it.javaName}\", ${it.javaName});"
                }
                method(
                        "set${it.javaName.capitalized()}",
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
                    +"return super.criteriaUpdate(\"${primaryKeyName}.${it.javaName}\", ${it.javaName});"
                }
                method(
                        methodName,
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
                    +"return super.criteria(\"${primaryKeyName}.${it.javaName}\", ${it.javaName}, matcher);"
                }
            }
        }

        otherColumns.forEach {
            val javaName = if (it.javaName in existMethodNames) "${it.javaName}Field" else it.javaName
            method(javaName, JavaType("top.bettercode.summer.data.jpa.query.SpecPath").typeArgument(it.javaType, entityType, type)) {
                javadoc {
                    +"/**"
                    +" * @return ${it.remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                    +" */"
                }
                this.visibility = JavaVisibility.PUBLIC
                +"return super.path(\"${it.javaName}\");"
            }
            method(javaName, type, Parameter(it.javaName, it.javaType)) {
                javadoc {
                    +"/**"
                    +" * ${it.paramRemark}"
                    +" * @return ${it.remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                    +" */"
                }
                this.visibility = JavaVisibility.PUBLIC
                +"return super.criteria(\"${it.javaName}\", ${it.javaName});"
            }
            method("set${it.javaName.capitalized()}", type, Parameter(it.javaName, it.javaType)) {
                javadoc {
                    +"/**"
                    +" * ${it.paramRemark}"
                    +" * @return ${it.remark.split(Regex("[:：,， (（]"))[0]} 相关Matcher"
                    +" */"
                }
                this.visibility = JavaVisibility.PUBLIC
                +"return super.criteriaUpdate(\"${it.javaName}\", ${it.javaName});"
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
                +"return super.criteria(\"${it.javaName}\", ${it.javaName}, matcher);"
            }
        }
    }
}