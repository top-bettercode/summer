import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.JavaVisibility
import top.bettercode.generator.dom.java.element.Parameter

/**
 * @author Peter Wu
 */
class Entity : ModuleJavaGenerator() {

    override var cover: Boolean = true
    override val type: JavaType
        get() = entityType

    override fun content() {
        //entityClass
        clazz {
            import("java.util.Objects")
            import("org.springframework.data.jpa.domain.support.AuditingEntityListener")
            if (columns.any { it.autoIncrement || it.idgenerator || it.sequence.isNotBlank() }) {
                import("javax.persistence.GenerationType")
            }
            annotation("@org.hibernate.annotations.DynamicInsert")
            annotation("@org.hibernate.annotations.DynamicUpdate")
            annotation("@javax.persistence.Entity")
            annotation("@javax.persistence.Table(name = \"$tableName\")")
            annotation("@javax.persistence.EntityListeners(AuditingEntityListener.class)")

            javadoc {
                +"/**"
                +" * $remarks 对应数据库表名：$tableName"
                +" */"
            }
            implement {
                +"java.io.Serializable"
            }
            serialVersionUID()

            //constructor no args
            constructor {
                if (compositePrimaryKey)
                    +"this.$primaryKeyName = new ${primaryKeyClass}();"
            }

            field("TABLE_NAME", JavaType.stringInstance, "\"${tableName}\"") {
                visibility = JavaVisibility.PUBLIC
                isStatic = true
                isFinal = true
                javadoc {
                    +"/**"
                    +" * 对应数据库表名"
                    +" */"
                }
            }

            //constructor with id
            constructor(Parameter(primaryKeyName, primaryKeyType)) {
                +"this.${primaryKeyName} = ${primaryKeyName};"
            }

            //primaryKey
            field(primaryKeyName, primaryKeyType) {
                if (primaryKeys.size == 1) {
                    if (primaryKey.remarks.isNotBlank() || !primaryKey.columnDef.isNullOrBlank())
                        javadoc {
                            +"/**"
                            +" * ${getRemark(primaryKey)}"
                            +" */"
                        }

                    annotation("@javax.persistence.Id")
                    if (primaryKey.autoIncrement) {
                        annotation("@javax.persistence.GeneratedValue(strategy = GenerationType.IDENTITY)")
                    } else if (primaryKey.idgenerator) {
                        val generatorStrategy = extension.idgenerator
                        val generator = generatorStrategy.substringAfterLast(".")
                            .substringBeforeLast("Generator").capitalize()
                        annotation("@javax.persistence.GeneratedValue(strategy = GenerationType.AUTO, generator = \"$entityName$generator\")")
                        annotation("@org.hibernate.annotations.GenericGenerator(name = \"$entityName$generator\", strategy = \"$generatorStrategy\")")
                    } else if (primaryKey.sequence.isNotBlank()) {
                        annotation("@javax.persistence.GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"${entityName}Sequence\")")
                        annotation("@javax.persistence.SequenceGenerator(name = \"${entityName}Sequence\", sequenceName = \"${primaryKey.sequence}\", allocationSize = 1)")
                    }
                } else {
                    javadoc {
                        +"/**"
                        +" * ${remarks}主键"
                        +" */"
                    }
                    annotation("@javax.persistence.EmbeddedId")
                }
            }
            //primaryKey getter
            method("get${primaryKeyName.capitalize()}", primaryKeyType) {
                javadoc {
                    +"/**"
                    +" * ${remarks}主键"
                    +" */"
                }
                +"return ${primaryKeyName};"
            }
            //primaryKey setter
            method(
                "set${primaryKeyName.capitalize()}",
                entityType,
                Parameter(primaryKeyName, primaryKeyType)
            ) {
                javadoc {
                    +"/**"
                    +" * 设置主键"
                    +" *"
                    +" * @param $primaryKeyName 主键"
                    +" * @return ${remarks}实例"
                    +" */"
                }
                +"this.${primaryKeyName} = ${primaryKeyName};"
                +"return this;"
            }

            otherColumns.forEach {
                //field
                field(it.javaName, it.javaType) {
                    if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                        javadoc {
                            +"/**"
                            +" * ${getRemark(it)}"
                            +" */"
                        }

                    var columnAnnotation =
                        "@javax.persistence.Column(name = \"${it.columnName}\", columnDefinition = \"${it.typeDesc}${it.defaultDesc}${if (it.extra.isBlank()) "" else " ${it.extra}"}\""
                    if (it.columnSize > 0 && it.columnSize != 255 || !it.nullable) {
                        if (it.columnSize > 0 && it.columnSize != 255) {
                            columnAnnotation += ", length = ${it.columnSize}"
                        }
                        if (!it.nullable) {
                            columnAnnotation += ", nullable = false"
                        }
                    }
                    columnAnnotation += ")"
                    annotation(columnAnnotation)
                    if (it.javaName == "createdDate") {
                        annotation("@org.springframework.data.annotation.CreatedDate")
                    }
                    if (it.extra.contains("ON UPDATE CURRENT_TIMESTAMP")) {
                        annotation("@org.springframework.data.annotation.LastModifiedDate")
                    }
                    if (it.version) {
                        annotation("@javax.persistence.Version")
                    }
                    if (it.isSoftDelete) {
                        annotation("@top.bettercode.simpleframework.data.jpa.SoftDelete")
                    }
                }

                //getter
                method("get${it.javaName.capitalize()}", it.javaType) {
                    if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                        javadoc {
                            +"/**"
                            +" * ${getReturnRemark(it)}"
                            +" */"
                        }
                    +"return ${it.javaName};"
                }
                //setter
                method(
                    "set${it.javaName.capitalize()}",
                    entityType,
                    Parameter(it.javaName, it.javaType)
                ) {
                    javadoc {
                        +"/**"
                        +" * ${getParamRemark(it)}"
                        +" * @return ${remarks}实例"
                        +" */"
                    }
                    +"this.${it.javaName} = ${it.javaName};"
                    +"return this;"
                }
            }

            //equals
            method(
                "equals",
                JavaType.booleanPrimitiveInstance,
                Parameter("o", JavaType.objectInstance)
            ) {
                annotation("@Override")
                +"if (this == o) {"
                +"return true;"
                +"}"
                +"if (!(o instanceof ${className})) {"
                +"return false;"
                +"}"
                +"$className that = (${className}) o;"
                +"if (${primaryKeyName} != that.get${primaryKeyName.capitalize()}()) {"
                +"return false;"
                +"}"


                val size = otherColumns.size
                if (size == 0) {
                    +"return true;"
                }
                if (size == 1) {
                    +"return Objects.equals(${otherColumns[0].javaName}, that.${otherColumns[0].javaName});"
                } else {
                    otherColumns.forEachIndexed { index, column ->
                        when (index) {
                            0 -> {
                                +"return Objects.equals(${column.javaName}, that.${column.javaName}) &&"
                            }
                            size - 1 -> {
                                +"    Objects.equals(${column.javaName}, that.${column.javaName});"
                            }
                            else -> {
                                +"    Objects.equals(${column.javaName}, that.${column.javaName}) &&"
                            }
                        }
                    }
                }
            }

            //hashCode
            method("hashCode", JavaType.intPrimitiveInstance) {
                annotation("@Override")
                if (compositePrimaryKey) {
                    +"return Objects.hash(${
                        (listOf(primaryKeyName) + otherColumns.map { it.javaName }).joinToString(
                            ", "
                        )
                    });"
                } else {
                    +"return Objects.hash(${columns.joinToString(", ") { it.javaName }});"
                }
            }

            //toString
            method("toString", JavaType.stringInstance) {
                annotation("@Override")
                +"return \"${className}{\" +"
                +"    \"${primaryKeyName}='\" + $primaryKeyName + '\\'' +"
                otherColumns.forEachIndexed { _, it ->
                    +"    \", ${it.javaName}=${if (it.javaType == JavaType.stringInstance) "'" else ""}\" + ${it.javaName} ${if (it.javaType == JavaType.stringInstance) "+ '\\'' " else ""}+"
                }
                +"    '}';"
            }

            val specType =
                JavaType("top.bettercode.simpleframework.data.jpa.query.MatcherSpecification").typeArgument(
                    entityType
                )
            val specMatcherType = JavaType("${className}Matcher")
            method("spec", specType) {
                javadoc {
                    +"/**"
                    +" * 创建 SpecMatcher 实例"
                    +" *"
                    +" * @return $remarks SpecMatcher 实例"
                    +" */"
                }
                +"return spec(${className}Matcher.matching());"
            }
            method("spec", specType, Parameter("specMatcher", specMatcherType)) {
                javadoc {
                    +"/**"
                    +" * 创建 SpecMatcher 实例"
                    +" *"
                    +" * @param specMatcher specMatcher"
                    +" * @return $remarks SpecMatcher 实例"
                    +" */"
                }
                +"return new MatcherSpecification<>(specMatcher, this);"
            }
        }

        //primaryKeyClass
        if (compositePrimaryKey)
            clazz(type = primaryKeyType) {
                import("java.util.Objects")
                import("javax.persistence.Embeddable")
                import("top.bettercode.lang.util.StringUtil")
                visibility = JavaVisibility.PUBLIC
                annotation("@javax.persistence.Embeddable")
                javadoc {
                    +"/**"
                    +" * $remarks 主键 对应表名：$tableName"
                    +" */"
                }
                implement {
                    +"java.io.Serializable"
                }
                serialVersionUID()

                //constructor no args
                constructor { }

                if (compositePrimaryKey)
                    constructor {
                        primaryKeys.forEach { column ->
                            parameter(column.javaType, column.javaName)
                            +"this.${column.javaName} = ${column.javaName};"
                        }
                    }

                primaryKeys.forEach {
                    //field
                    field(it.javaName, it.javaType) {
                        if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                            javadoc {
                                +"/**"
                                +" * ${getRemark(it)}"
                                +" */"
                            }

                        var columnAnnotation =
                            "@javax.persistence.Column(name = \"${it.columnName}\", columnDefinition = \"${it.typeDesc}${it.defaultDesc}${if (it.extra.isBlank()) "" else " ${it.extra}"}\""
                        if (it.columnSize > 0 && it.columnSize != 255 || !it.nullable) {
                            if (it.columnSize > 0 && it.columnSize != 255) {
                                columnAnnotation += ", length = ${it.columnSize}"
                            }
                            if (!it.nullable) {
                                columnAnnotation += ", nullable = false"
                            }
                        }
                        columnAnnotation += ")"
                        annotation(columnAnnotation)
                    }


                    //getter
                    method("get${it.javaName.capitalize()}", it.javaType) {
                        if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                            javadoc {
                                +"/**"
                                +" * ${getReturnRemark(it)}"
                                +" */"
                            }
                        +"return ${it.javaName};"
                    }
                    //setter
                    method("set${it.javaName.capitalize()}", primaryKeyType) {
                        if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                            javadoc {
                                +"/**"
                                +" * ${getParamRemark(it)}"
                                +" * @return ${remarks}实例"
                                +" */"
                            }
                        parameter {
                            type = it.javaType
                            name = it.javaName
                        }
                        +"this.${it.javaName} = ${it.javaName};"
                        +"return this;"
                    }
                }
                //equals
                method(
                    "equals",
                    JavaType.booleanPrimitiveInstance,
                    Parameter("o", JavaType.objectInstance)
                ) {
                    annotation("@Override")
                    +"if (this == o) {"
                    +"return true;"
                    +"}"
                    +"if (!(o instanceof ${primaryKeyClass})) {"
                    +"return false;"
                    +"}"
                    +"${primaryKeyClass} that = (${primaryKeyClass}) o;"
                    val size = primaryKeys.size
                    primaryKeys.forEachIndexed { index, column ->
                        when (index) {
                            0 -> {
                                +"return Objects.equals(${column.javaName}, that.${column.javaName}) &&"
                            }
                            size - 1 -> {
                                +"    Objects.equals(${column.javaName}, that.${column.javaName});"
                            }
                            else -> {
                                +"    Objects.equals(${column.javaName}, that.${column.javaName}) &&"
                            }
                        }
                    }
                }

                //hashCode
                method("hashCode", JavaType.intPrimitiveInstance) {
                    annotation("@Override")
                    +"return Objects.hash(${primaryKeys.joinToString(", ") { it.javaName }});"
                }

                //toString
                method("toString", JavaType.stringInstance) {
                    annotation("@Override")
                    +"return StringUtil.json(this);"
                }

            }

        clazz(type = matcherType) {
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
                +"return new ${type}(SpecMatcherMode.ALL);"
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
                +"return new ${type}(SpecMatcherMode.ANY);"
            }

            val pathType =
                JavaType("top.bettercode.simpleframework.data.jpa.query.SpecPath").typeArgument(
                    type
                )
            val matcherType =
                JavaType("top.bettercode.simpleframework.data.jpa.query.PathMatcher")
            //primaryKey
            if (compositePrimaryKey) {
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

        //propertiesInterface
        interfaze(type = propertiesType) {
            visibility = JavaVisibility.PUBLIC
            if (compositePrimaryKey) {
                primaryKeys.forEach {
                    field(
                        it.javaName,
                        JavaType.stringInstance,
                        "\"${primaryKeyName}.${it.javaName}\""
                    ) {
                        visibility = JavaVisibility.DEFAULT
                        if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                            javadoc {
                                +"/**"
                                +" * ${getRemark(it)}"
                                +" */"
                            }
                    }
                }
            } else {
                field(primaryKeyName, JavaType.stringInstance, "\"${primaryKeyName}\"") {
                    visibility = JavaVisibility.DEFAULT
                    if (primaryKey.remarks.isNotBlank() || !primaryKey.columnDef.isNullOrBlank())
                        javadoc {
                            +"/**"
                            +" * ${getRemark(primaryKey)}"
                            +" */"
                        }
                }
            }
            otherColumns.forEach {
                field(it.javaName, JavaType.stringInstance, "\"${it.javaName}\"") {
                    visibility = JavaVisibility.DEFAULT
                    if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                        javadoc {
                            +"/**"
                            +" * ${getRemark(it)}"
                            +" */"
                        }
                }
            }
        }
    }
}