package top.bettercode.summer.tools.generator.dom.java.element

import top.bettercode.summer.tools.generator.dom.java.JavaType
import top.bettercode.summer.tools.generator.dom.java.JavaTypeOperator
import java.util.*

abstract class InnerUnit(
    /**
     * Gets the type.
     *
     * @return Returns the type.
     */
    val type: JavaType
) : JavaElement() {

    /** The fields.  */
    val fields: MutableList<Field> = mutableListOf()

    /** The super interface types.  */
    val superInterfaceTypes: MutableSet<JavaType> = mutableSetOf()

    /** The methods.  */
    val methods: MutableList<Method> = mutableListOf()

    fun implement(implement: JavaTypeOperator.() -> Unit) {
        implement(JavaTypeOperator(superInterfaceTypes))
    }

    fun field(
        name: String,
        type: JavaType,
        initializationString: String? = null,
        isFinal: Boolean = false,
        visibility: JavaVisibility = JavaVisibility.PRIVATE
    ) {
        val field = Field()
        field.type = type
        field.name = name
        field.initializationString = initializationString
        field.isFinal = isFinal
        field.visibility = visibility
        fields.add(field)
    }

    fun field(
        name: String,
        type: JavaType,
        initializationString: String? = null,
        isFinal: Boolean = false,
        visibility: JavaVisibility = JavaVisibility.PRIVATE,
        closure: Field.() -> Unit
    ) {
        val field = Field()
        field.type = type
        field.name = name
        field.initializationString = initializationString
        field.isFinal = isFinal
        field.visibility = visibility
        closure(field)
        fields.add(field)
    }

    fun serialVersionUID() {
        field("serialVersionUID", JavaType.longPrimitiveInstance) {
            isStatic = true
            isFinal = true
            initializationString = "1L"
        }
    }

    fun constructor(
        vararg parameter: Parameter,
        visibility: JavaVisibility = JavaVisibility.PUBLIC
    ) {
        val method = Method()
        method.isConstructor = true
        method.name = type.shortName
        method.visibility = visibility
        method.parameter(*parameter)
        methods.add(method)
    }

    fun constructor(
        vararg parameter: Parameter,
        visibility: JavaVisibility = JavaVisibility.PUBLIC,
        closure: Method.() -> Unit
    ) {
        val method = Method()
        method.isConstructor = true
        method.name = type.shortName
        method.visibility = visibility
        method.parameter(*parameter)
        closure(method)
        methods.add(method)
    }

    fun method(
        name: String,
        returnType: JavaType = JavaType.voidPrimitiveInstance,
        vararg parameter: Parameter,
        visibility: JavaVisibility = JavaVisibility.PUBLIC
    ) {
        val method = Method()
        method.name = name
        method.visibility = visibility
        method.parameter(*parameter)
        method.returnType = returnType
        methods.add(method)
    }

    fun method(
        name: String,
        returnType: JavaType = JavaType.voidPrimitiveInstance,
        vararg parameter: Parameter,
        visibility: JavaVisibility = JavaVisibility.PUBLIC,
        closure: Method.() -> Unit
    ) {
        val method = Method()
        method.name = name
        method.visibility = visibility
        method.parameter(*parameter)
        method.returnType = returnType
        closure(method)
        methods.add(method)
    }

    /**
     * Adds the field.
     *
     * @param field
     * the field
     */
    fun field(field: Field) {
        fields.add(field)
    }

    /**
     * Adds the super interface.
     *
     * @param superInterface
     * the super interface
     */
    fun implement(vararg superInterface: JavaType) {
        superInterfaceTypes.addAll(superInterface)
    }

    /**
     * Adds the super interface.
     *
     * @param fullTypeSpecification
     * the super interface
     */
    fun implement(vararg fullTypeSpecification: String) {
        superInterfaceTypes.addAll(fullTypeSpecification.map { JavaType(it) })
    }

    /**
     * Adds the method.
     *
     * @param method
     * the method
     */
    fun method(method: Method) {
        methods.add(method)
    }

    /**
     * returns a unique set of "import xxx;" Strings for the set of types.
     *
     * @param importedTypes
     * the imported types
     * @return the sets the
     */
    open fun calculateImports(importedTypes: MutableSet<JavaType>): Set<String> {
        fields.forEach { field1 ->
            importedTypes.add(field1.type)
            field1.annotations.needImportedTypes.forEach {
                importedTypes.add(it)
            }
        }
        methods.forEach { method ->
            importedTypes.add(method.returnType)
            method.parameters.forEach { parameter ->
                importedTypes.add(parameter.type)
                parameter.annotations.needImportedTypes.forEach {
                    importedTypes.add(it)
                }
            }
            method.annotations.needImportedTypes.forEach {
                importedTypes.add(it)
            }
        }
        superInterfaceTypes.forEach {
            importedTypes.add(it)
        }
        super.annotations.needImportedTypes.forEach {
            importedTypes.add(it)
        }
        val sb = StringBuilder()
        val importStrings = TreeSet<String>()
        for (fqjt in importedTypes) {
            for (importString in fqjt.importList) {
                if (type.packageName != importString.substringBeforeLast('.')) {
                    sb.setLength(0)
                    sb.append("import ")
                    sb.append(importString)
                    sb.append(';')
                    importStrings.add(sb.toString())
                }
            }
        }

        return importStrings
    }

}
