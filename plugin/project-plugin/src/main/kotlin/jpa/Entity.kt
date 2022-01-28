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

            //创建实例
            method("of", entityType) {
                this.isStatic = true
                javadoc {
                    +"/**"
                    +" * 创建实例"
                    +" *"
                    +" * @return ${remarks}实例"
                    +" */"
                }
                +"return new ${className}();"
            }

            //field
            field("matcher", JavaType("org.springframework.data.domain.ExampleMatcher")) {
                javadoc {
                    +"/**"
                    +" * ExampleMatcher"
                    +" */"
                }
                annotation("@javax.persistence.Transient")
            }

            if (hasPrimaryKey) {
                method(primaryKeyName, entityType, Parameter(primaryKeyName, primaryKeyType)) {
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
                method(
                    primaryKeyName,
                    entityType,
                    Parameter(primaryKeyName, primaryKeyType),
                    Parameter(
                        "genericPropertyMatcher",
                        JavaType("org.springframework.data.domain.ExampleMatcher.GenericPropertyMatcher")
                    )
                ) {
                    javadoc {
                        +"/**"
                        +" * 设置主键"
                        +" *"
                        +" * @param $primaryKeyName 主键"
                        +" * @param genericPropertyMatcher PropertyMatcher"
                        +" * @return ${remarks}实例"
                        +" */"
                    }
                    +"if(this.matcher == null){"
                    +"this.matcher = ExampleMatcher.matching();"
                    +"}"
                    +"this.matcher.withMatcher(\"${primaryKeyName}\", genericPropertyMatcher);"
                    +"this.${primaryKeyName} = ${primaryKeyName};"
                    +"return this;"
                }
            }
            otherColumns.forEach {
                method(it.javaName, entityType, Parameter(it.javaName, it.javaType)) {
                    javadoc {
                        +"/**"
                        +" * ${getParamRemark(it)}"
                        +" * @return ${remarks}实例"
                        +" */"
                    }
                    +"this.${it.javaName} = ${it.javaName};"
                    +"return this;"
                }
                method(
                    it.javaName,
                    entityType,
                    Parameter(it.javaName, it.javaType),
                    Parameter(
                        "genericPropertyMatcher",
                        JavaType("org.springframework.data.domain.ExampleMatcher.GenericPropertyMatcher")
                    )
                ) {
                    javadoc {
                        +"/**"
                        +" * ${getParamRemark(it)}"
                        +" * @param genericPropertyMatcher PropertyMatcher"
                        +" * @return ${remarks}实例"
                        +" */"
                    }
                    +"if (this.matcher == null) {"
                    +"this.matcher = ExampleMatcher.matching();"
                    +"}"
                    +"this.matcher.withMatcher(${className}Properties.${it.javaName}, genericPropertyMatcher);"
                    +"this.${it.javaName} = ${it.javaName};"
                    +"return this;"
                }
            }

            //example
            method(
                "example",
                JavaType("org.springframework.data.domain.Example").typeArgument(entityType)
            ) {
                javadoc {
                    +"/**"
                    +" * @return example实例"
                    +" */"
                }
                +"if (this.matcher == null) {"
                +"return Example.of(this);"
                +"} else {"
                +"return Example.of(this, this.matcher);"
                +"}"
            }

            method(
                "example",
                JavaType("org.springframework.data.domain.Example").typeArgument(entityType),
                Parameter("matcher", JavaType("org.springframework.data.domain.ExampleMatcher"))
            ) {
                javadoc {
                    +"/**"
                    +" * @param matcher ExampleMatcher"
                    +" * @return example实例"
                    +" */"
                }
                +"return Example.of(this, matcher);"
            }

            //constructor no args
            constructor {}

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
                method("set${primaryKeyName.capitalize()}") {
                    javadoc {
                        +"/**"
                        +" * ${remarks}主键"
                        +" */"
                    }
                    parameter {
                        type = primaryKeyType
                        name = primaryKeyName
                    }
                    +"this.${primaryKeyName} = ${primaryKeyName};"
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
                        if (it.javaName == "version") {
                            annotation("@javax.persistence.Version")
                        }
                        if (it.isSoftDelete) {
                            annotation("@top.bettercode.simpleframework.data.jpa.SoftDelete")
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
                method("set${it.javaName.capitalize()}") {
                    if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                        javadoc {
                            +"/**"
                            +" * ${getParamRemark(it)}"
                            +" */"
                        }
                    parameter {
                        type = it.javaType
                        name = it.javaName
                    }
                    +"this.${it.javaName} = ${it.javaName};"
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
                if (hasPrimaryKey) {
                    +"    \"${primaryKeyName}='\" + $primaryKeyName + '\\'' +"
                }
                otherColumns.forEachIndexed { i, it ->
                    +"    \"${if (i > 0) ", " else ""}${it.javaName}=${if (it.javaType == JavaType.stringInstance) "'" else ""}\" + ${it.javaName} ${if (it.javaType == JavaType.stringInstance) "+ '\\'' " else ""}+"
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

                    //创建实例
                    method("of", primaryKeyType) {
                        this.isStatic = true
                        javadoc {
                            +"/**"
                            +" * 创建实例"
                            +" *"
                            +" * @return ${remarks}实例"
                            +" */"
                        }
                        +"return new ${className}Key();"
                    }

                    primaryKeys.forEach {
                        method(it.javaName, primaryKeyType, Parameter(it.javaName, it.javaType)) {
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


                    //constructor no args
                    constructor {}
                    //constructor with key String
                    import("org.springframework.util.Assert")
                    constructor(Parameter(primaryKeyName, JavaType.stringInstance)) {
                        +"Assert.hasText(${primaryKeyName},\"${primaryKeyName}不能为空\");"
                        +"String[] split = ${primaryKeyName}.split(\"${keySep}\");"
                        +"Assert.isTrue(split.length==${primaryKeys.size},\"${primaryKeyName}格式不对\");"
                        primaryKeys.forEachIndexed { index, column ->
                            +"this.${column.javaName} = ${column.setValue("split[${index}]")};"
                        }
                    }

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
                        method("set${it.javaName.capitalize()}") {
                            if (it.remarks.isNotBlank() || !it.columnDef.isNullOrBlank())
                                javadoc {
                                    +"/**"
                                    +" * ${getParamRemark(it)}"
                                    +" */"
                                }
                            parameter {
                                type = it.javaType
                                name = it.javaName
                            }
                            +"this.${it.javaName} = ${it.javaName};"
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
                        +"return ${primaryKeys.joinToString(" + \"${keySep}\" + ") { it.javaName }};"
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