package jpa.unit

import ProjectGenerator
import top.bettercode.generator.database.entity.Column
import top.bettercode.generator.dom.java.JavaType
import top.bettercode.generator.dom.java.element.Parameter
import top.bettercode.generator.dom.java.element.TopLevelClass
import top.bettercode.lang.capitalized
import top.bettercode.lang.decapitalized

/**
 * @author Peter Wu
 */
val form: ProjectGenerator.(TopLevelClass) -> Unit = { unit ->
    unit.apply {
        javadoc {
            +"/**"
            +" * $remarks 表单"
            +" */"
        }

        val entityType = if (isFullComposite) primaryKeyType else entityType
        field(name = "entity", type = entityType, isFinal = true) {
            annotation("@com.fasterxml.jackson.annotation.JsonIgnore")
        }

        //constructor no args
        constructor {
            +"this.entity = new ${entityType.shortName}();"
        }

        //constructor with entity
        val entityName = entityType.shortName.decapitalized()
        constructor(Parameter(entityName, entityType)) {
            +"this.entity = $entityName;"
        }

        if (!isFullComposite) {
            //constructor with id
            constructor(Parameter(primaryKeyName, primaryKeyType)) {
                +"this.entity = new $className(${primaryKeyName});"
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
                if (!isCompositePrimaryKey && !primaryKey.autoIncrement && primaryKey.idgenerator.isBlank() && primaryKey.sequence.isBlank()) {
                    if (primaryKey.columnSize > 0 && primaryKey.javaType == JavaType.stringInstance) {
                        import("javax.validation.groups.Default")
                        annotation("@org.hibernate.validator.constraints.Length(max = ${primaryKey.columnSize}, groups = Default.class)")
                    }
                }
                val autoIncrement = !isCompositePrimaryKey && primaryKey.autoIncrement
                if (autoIncrement)
                    import("top.bettercode.simpleframework.web.validator.UpdateConstraint")
                if (primaryKeyType == JavaType.stringInstance) {
                    annotation("@javax.validation.constraints.NotBlank${if (autoIncrement) "(groups = UpdateConstraint.class)" else ""}")
                } else {
                    annotation("@javax.validation.constraints.NotNull${if (autoIncrement) "(groups = UpdateConstraint.class)" else ""}")
                }
                +"return this.entity.get${
                    primaryKeyName.capitalized()
                }();"
            }
        }

        method("getEntity", entityType) {
            +"return entity;"
        }

        val filterColumns =
            columns.filter { it.javaName != primaryKeyName && !it.version && !it.jsonViewIgnored && it.javaName != "createdDate" && it.javaName != "lastModifiedDate" }
        filterColumns
            .forEach {
                //getter
                getter(this, it)
            }

        if (!isFullComposite) {
            //primaryKey setter
            method("set${primaryKeyName.capitalized()}") {
                javadoc {
                    +"/**"
                    +" * ${remarks}主键"
                    +" */"
                }
                parameter {
                    type = primaryKeyType
                    name = primaryKeyName
                }
                +"this.entity.set${
                    primaryKeyName.capitalized()
                }(${primaryKeyName});"
            }
        }
        filterColumns.forEach {
            //setter
            setter(this, it)
        }

    }
}

private val getter: ProjectGenerator.(TopLevelClass, Column) -> Unit = { clazz, it ->
    clazz.apply {
        //getter
        method(
            "get${it.javaName.capitalized()}",
            it.javaType
        ) {
            if (it.columnSize > 0 && it.javaType == JavaType.stringInstance) {
                import("javax.validation.groups.Default")
                annotation("@org.hibernate.validator.constraints.Length(max = ${it.columnSize}, groups = Default.class)")
            }
            if (!it.nullable) {
                import("top.bettercode.simpleframework.web.validator.CreateConstraint")
                if (it.javaType == JavaType.stringInstance) {
                    annotation("@javax.validation.constraints.NotBlank(groups = CreateConstraint.class)")
                } else {
                    annotation("@javax.validation.constraints.NotNull(groups = CreateConstraint.class)")
                }
            }
            +"return this.entity.get${
                it.javaName.capitalized()
            }();"
        }
    }
}

private val setter: ProjectGenerator.(TopLevelClass, Column) -> Unit = { clazz, it ->
    clazz.apply {
        method("set${it.javaName.capitalized()}") {
            parameter {
                type = it.javaType
                name = it.javaName
            }
            +"this.entity.set${
                it.javaName.capitalized()
            }(${it.javaName});"
        }
    }
}