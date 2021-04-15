import cn.bestwu.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
open class MMixIn : MModuleJavaGenerator() {

    override val type: JavaType
        get() = mixInType


    override fun content() {
        interfaze {
            javadoc {
                +"/**"
                +" * $remarks"
                +" */"
            }
            val serializationViews = JavaType("$basePackageName.web.SerializationViews")
            implement(JavaType("cn.bestwu.simpleframework.web.serializer.MixIn").typeArgument(entityType), methodInfoType, serializationViews)

            columns.forEach {
                //getter
                if (it.isPrimary || (it.jsonViewIgnored))
                    method("get${it.javaName.capitalize()}", it.javaType) {
                        if (it.jsonViewIgnored)
                            annotation("@com.fasterxml.jackson.annotation.JsonIgnore")
                        else if (it.isPrimary)
                            annotation("@com.fasterxml.jackson.annotation.JsonView(Object.class)")
                        annotation("@Override")
                    }
            }
        }
    }
}