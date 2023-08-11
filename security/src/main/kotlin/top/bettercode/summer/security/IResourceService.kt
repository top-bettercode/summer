package top.bettercode.summer.security

/**
 * @author Peter Wu
 */
interface IResourceService {
    /**
     * @return 所有资源
     */
    @JvmDefault
    fun findAllResources(): List<IResource> {
        return emptyList()
    }

    @JvmDefault
    fun supportsAnonymous(): Boolean {
        return true
    }
}
