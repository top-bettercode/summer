import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.InnerClass
import top.bettercode.generator.dom.java.element.InnerInterface
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
        clazz {
            if (hasPrimaryKey) {
                annotation("@org.hibernate.annotations.DynamicInsert")
                annotation("@org.hibernate.annotations.DynamicUpdate")
                annotation("@javax.persistence.Entity")
                annotation("@javax.persistence.Table(name = \"$tableName\")")
                import("org.springframework.data.jpa.domain.support.AuditingEntityListener")
                annotation("@javax.persistence.EntityListeners(AuditingEntityListener.class)")
            }

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
                    +"this.$primaryKeyName = new ${primaryKeyType.shortName}();"
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

            if (hasPrimaryKey) {
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
                            import("javax.persistence.GenerationType")
                            annotation("@javax.persistence.GeneratedValue(strategy = GenerationType.IDENTITY)")
                        } else if (primaryKey.idgenerator) {
                            val generatorStrategy = extension.idgenerator
                            import("javax.persistence.GenerationType")
                            val generator = generatorStrategy.substringAfterLast(".")
                                .substringBeforeLast("Generator").capitalize()
                            annotation("@javax.persistence.GeneratedValue(strategy = GenerationType.AUTO, generator = \"$entityName$generator\")")
                            annotation("@org.hibernate.annotations.GenericGenerator(name = \"$entityName$generator\", strategy = \"$generatorStrategy\")")
                        } else if (primaryKey.sequence.isNotBlank()) {
                            import("javax.persistence.GenerationType")
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

                    if (hasPrimaryKey) {
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

                        if (it.autoIncrement) {
                            import("javax.persistence.GenerationType")
                            annotation("@javax.persistence.GeneratedValue(strategy = GenerationType.IDENTITY)")
                        } else if (it.idgenerator) {
                            val generatorStrategy = extension.idgenerator
                            import("javax.persistence.GenerationType")
                            val generator = generatorStrategy.substringAfterLast(".")
                                .substringBeforeLast("Generator").capitalize()
                            annotation("@javax.persistence.GeneratedValue(strategy = GenerationType.AUTO, generator = \"$entityName$generator\")")
                            annotation("@org.hibernate.annotations.GenericGenerator(name = \"$entityName$generator\", strategy = \"$generatorStrategy\")")
                        } else if (it.sequence.isNotBlank()) {
                            import("javax.persistence.GenerationType")
                            annotation("@javax.persistence.GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"${entityName}Sequence\")")
                            annotation("@javax.persistence.SequenceGenerator(name = \"${entityName}Sequence\", sequenceName = \"${it.sequence}\")")
                        }
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
            import("java.util.Objects")
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
                if (hasPrimaryKey) {
                    +"if (${primaryKeyName} != that.get${primaryKeyName.capitalize()}()) {"
                    +"return false;"
                    +"}"
                }


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
                if (hasPrimaryKey) {
                    +"    \"${primaryKeyName}='\" + $primaryKeyName + '\\'' +"
                }
                otherColumns.forEachIndexed { i, it ->
                    +"    \"${if (i > 0 || hasPrimaryKey) ", " else ""}${it.javaName}=${if (it.javaType == JavaType.stringInstance) "'" else ""}\" + ${it.javaName} ${if (it.javaType == JavaType.stringInstance) "+ '\\'' " else ""}+"
                }
                +"    '}';"
            }
            if (compositePrimaryKey) {
                val keySep = "_"
                import("javax.persistence.Embeddable")
                val innerClass = InnerClass(primaryKeyType)
                innerClass(innerClass)
                innerClass.apply {
                    visibility = JavaVisibility.PUBLIC
                    isStatic = true
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
                    //constructor with key String
                    import("org.springframework.util.Assert")
                    constructor(Parameter(primaryKeyName, JavaType.stringInstance)) {
                        +"Assert.hasText(${primaryKeyName},\"${primaryKeyName}不能为空\");"
                        +"String[] split = ${primaryKeyName}.split(\"${keySep}\");"
                        primaryKeys.forEachIndexed { index, column ->
                            +"this.${column.javaName} = split.length > $index ? ${
                                column.setValue(
                                    "split[${index}]"
                                )
                            } : null;"
                        }
                    }

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
                    import("java.util.Objects")
                    method(
                        "equals",
                        JavaType.booleanPrimitiveInstance,
                        Parameter("o", JavaType.objectInstance)
                    ) {
                        annotation("@Override")
                        +"if (this == o) {"
                        +"return true;"
                        +"}"
                        +"if (!(o instanceof ${className}Key)) {"
                        +"return false;"
                        +"}"
                        +"${className}Key that = (${className}Key) o;"
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
                        +"return ${primaryKeys.joinToString(" + \"${keySep}\" + ") { "(this.${it.javaName} == null ? \"\" : ${if (it.javaType == JavaType.stringInstance) "this.${it.javaName}" else "String.valueOf(this.${it.javaName})"})" }};"
                    }
                }
            }

            if (hasPrimaryKey) {
                val specType =
                    JavaType("top.bettercode.simpleframework.data.jpa.query.MatcherSpecification").typeArgument(
                        entityType
                    )
                val specMatcherType = JavaType("${className}Matcher")
                val specMatcherBaseType =
                    JavaType("top.bettercode.simpleframework.data.jpa.query.DefaultSpecMatcher").typeArgument(
                        specMatcherType
                    )
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
                val specMatcherClass = InnerClass(specMatcherType)
                innerClass(specMatcherClass)

                specMatcherClass.apply {
                    visibility = JavaVisibility.PUBLIC
                    isStatic = true

                    javadoc {
                        +"/**"
                        +" * $remarks SpecMatcher"
                        +" */"
                    }

                    superClass(specMatcherBaseType)

                    val modeType =
                        JavaType("SpecMatcherMode")

                    constructor(Parameter("mode", modeType)) {
                        this.visibility = JavaVisibility.PRIVATE
                        +"super(mode);"
                    }


                    //创建实例
                    method("matching", specMatcherType) {
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

                    method("matchingAll", specMatcherType) {
                        this.isStatic = true
                        javadoc {
                            +"/**"
                            +" * 创建 SpecMatcher 实例"
                            +" *"
                            +" * @return $remarks SpecMatcher 实例"
                            +" */"
                        }
                        +"return new ${specMatcherType}(SpecMatcherMode.ALL);"
                    }

                    method("matchingAny", specMatcherType) {
                        this.isStatic = true
                        javadoc {
                            +"/**"
                            +" * 创建 SpecMatcher 实例"
                            +" *"
                            +" * @return $remarks SpecMatcher 实例"
                            +" */"
                        }
                        +"return new ${specMatcherType}(SpecMatcherMode.ANY);"
                    }

                    val pathType =
                        JavaType("top.bettercode.simpleframework.data.jpa.query.SpecPath").typeArgument(
                            specMatcherType
                        )
                    val matcherType =
                        JavaType("top.bettercode.simpleframework.data.jpa.query.PathMatcher")
                    if (hasPrimaryKey) {
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
                                    specMatcherType,
                                    Parameter(it.javaName, it.javaType)
                                ) {
                                    this.visibility = JavaVisibility.PUBLIC
                                    +"super.specPath(\"${primaryKeyName}.${it.javaName}\").setValue(${it.javaName});"
                                    +"return this;"
                                }
                                method(
                                    javaName,
                                    specMatcherType,
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
                                specMatcherType,
                                Parameter(primaryKeyName, primaryKeyType)
                            ) {
                                this.visibility = JavaVisibility.PUBLIC
                                +"super.specPath(\"${primaryKeyName}\").setValue(${primaryKeyName});"
                                +"return this;"
                            }
                            method(
                                javaName,
                                specMatcherType,
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

                    }

                    otherColumns.forEach {
                        val javaName =
                            if (it.javaName == "spec") "specField" else it.javaName
                        method(javaName, pathType) {
                            this.visibility = JavaVisibility.PUBLIC
                            +"return super.specPath(\"${it.javaName}\");"
                        }
                        method(javaName, specMatcherType, Parameter(it.javaName, it.javaType)) {
                            this.visibility = JavaVisibility.PUBLIC
                            +"super.specPath(\"${it.javaName}\").setValue(${it.javaName});"
                            +"return this;"
                        }
                        method(
                            javaName, specMatcherType, Parameter(it.javaName, it.javaType),
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

            val innerInterface = InnerInterface(JavaType("${className}Properties"))
            innerInterface(innerInterface)
            innerInterface.apply {
                visibility = JavaVisibility.PUBLIC
                if (hasPrimaryKey) {
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


}