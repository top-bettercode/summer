import top.bettercode.generator.dom.java.JavaType

/**
 * @author Peter Wu
 */
open class Dao : MModuleJavaGenerator() {

    override val type: JavaType
        get() = daoType


    override fun content() {
        interfaze {
            javadoc {
                +"/**"
                +" * $remarks 数据层"
                +" */"
            }
            annotation("@org.apache.ibatis.annotations.Mapper")
            if (hasPrimaryKey) {
                val superInterface =
                    JavaType("com.baomidou.mybatisplus.mapper.BaseMapper").typeArgument(entityType)
                implement(superInterface)
            }
        }
    }
}