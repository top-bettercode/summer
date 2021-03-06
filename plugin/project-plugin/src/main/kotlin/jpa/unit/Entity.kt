import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.JavaVisibility
import top.bettercode.generator.dom.java.element.Parameter
import top.bettercode.generator.dom.java.element.TopLevelClass
import top.bettercode.lang.capitalized
import java.util.*

/**
 * @author Peter Wu
 */
//entityClass
val entity: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        import("java.util.Objects")
        import("org.springframework.data.jpa.domain.support.AuditingEntityListener")
        if (columns.any { it.autoIncrement || it.idgenerator.isNotBlank() || it.sequence.isNotBlank() }) {
            import("javax.persistence.GenerationType")
        }
        if (enable("dynamicInsert", false))
            annotation("@org.hibernate.annotations.DynamicInsert")
        annotation("@org.hibernate.annotations.DynamicUpdate")
        annotation("@javax.persistence.Entity")
        annotation("@javax.persistence.Table(name = ${className}.TABLE_NAME)")
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
            if (isCompositePrimaryKey)
                +"this.$primaryKeyName = new ${primaryKeyClassName}();"
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
                if (primaryKey.remark.isNotBlank() || primaryKey.columnDef != null)
                    javadoc {
                        +"/**"
                        +" * ${primaryKey.docRemark}"
                        +" */"
                    }

                annotation("@javax.persistence.Id")
                if (primaryKey.autoIncrement) {
                    annotation("@javax.persistence.GeneratedValue(strategy = GenerationType.IDENTITY)")
                } else if (primaryKey.idgenerator.isNotBlank()) {
                    val generatorStrategy =
                        (setting(primaryKey.idgenerator.lowercase(Locale.getDefault())) as? String)
                            ?: "uuid2"
                    val generator = generatorStrategy.substringAfterLast(".")
                        .substringBeforeLast("Generator")
                        .capitalized()
                    annotation("@javax.persistence.GeneratedValue(strategy = GenerationType.AUTO, generator = \"$entityName$generator\")")
                    annotation("@org.hibernate.annotations.GenericGenerator(name = \"$entityName$generator\", strategy = \"$generatorStrategy\")")
                } else if (primaryKey.sequence.isNotBlank()) {
                    annotation("@javax.persistence.GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"${entityName}Sequence\")")
                    annotation("@javax.persistence.SequenceGenerator(name = \"${entityName}Sequence\", sequenceName = \"${primaryKey.sequence}\", allocationSize = 1)")
                }
                var columnAnnotation =
                    "@javax.persistence.Column(name = \"${primaryKey.columnName}\", columnDefinition = \"${primaryKey.typeDesc}${primaryKey.defaultDesc}${if (primaryKey.extra.isBlank()) "" else " ${primaryKey.extra}"}\""
                if (primaryKey.columnSize > 0 && primaryKey.columnSize != 255 || !primaryKey.nullable) {
                    if (primaryKey.columnSize > 0 && primaryKey.columnSize != 255) {
                        columnAnnotation += ", length = ${primaryKey.columnSize}"
                    }
                    if (!primaryKey.nullable) {
                        columnAnnotation += ", nullable = false"
                    }
                }
                columnAnnotation += ")"
                annotation(columnAnnotation)
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
        method(
            "get${primaryKeyName.capitalized()}",
            primaryKeyType
        ) {
            javadoc {
                +"/**"
                +" * ${remarks}主键"
                +" */"
            }
            +"return ${primaryKeyName};"
        }
        //primaryKey setter
        method(
            "set${primaryKeyName.capitalized()}",
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

        if (isCompositePrimaryKey) {
            primaryKeys.forEach {
                //getter
                method(
                    "get${it.javaName.capitalized()}",
                    it.javaType
                ) {
                    if (it.remark.isNotBlank() || it.columnDef != null)
                        javadoc {
                            +"/**"
                            +" * ${it.returnRemark}"
                            +" */"
                        }
                    +"return this.${primaryKeyName}.${name}();"
                }
                //setter
                method(
                    "set${it.javaName.capitalized()}",
                    entityType,
                    Parameter(it.javaName, it.javaType)
                ) {
                    javadoc {
                        +"/**"
                        +" * ${it.paramRemark}"
                        +" * @return ${remarks}实例"
                        +" */"
                    }
                    +"this.${primaryKeyName}.${name}(${it.javaName});"
                    +"return this;"
                }
            }
        }

        otherColumns.forEach {
            //field
            field(it.javaName, it.javaType) {
                if (it.remark.isNotBlank() || it.columnDef != null)
                    javadoc {
                        +"/**"
                        +" * ${it.docRemark}"
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
                if (it.softDelete) {
                    annotation("@top.bettercode.simpleframework.data.jpa.SoftDelete")
                }
                if (it.numericBooleanType) {
                    annotation("@org.hibernate.annotations.Type(type = \"org.hibernate.type.NumericBooleanType\")")
                }
            }

            //getter
            method(
                "get${it.javaName.capitalized()}",
                it.javaType
            ) {
                if (it.remark.isNotBlank() || it.columnDef != null)
                    javadoc {
                        +"/**"
                        +" * ${it.returnRemark}"
                        +" */"
                    }
                +"return ${it.javaName};"
            }
            //setter
            method(
                "set${it.javaName.capitalized()}",
                entityType,
                Parameter(it.javaName, it.javaType)
            ) {
                javadoc {
                    +"/**"
                    +" * ${it.paramRemark}"
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
            +"if (${primaryKeyName} != that.get${
                primaryKeyName.capitalized()
            }()) {"
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
            if (isCompositePrimaryKey) {
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
        import("top.bettercode.lang.util.StringUtil")
        method("toString", JavaType.stringInstance) {
            annotation("@Override")
            +"return StringUtil.json(this);"
        }
    }
}