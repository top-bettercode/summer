package top.bettercode.summer.security

import jakarta.validation.constraints.NotBlank
import java.io.Serializable

/**
 * 资源，包含权限信息
 *
 * @author Peter Wu
 */
interface IResource : Serializable {
    /**
     * @return 资源描述, 以 ((method(|method)*)*:url(|url)*,?)+
     */
    val ress: @NotBlank String

    /**
     * @return 权限属性
     */
    val mark: @NotBlank String
}
