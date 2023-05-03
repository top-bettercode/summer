package top.bettercode.summer.security

/**
 * @author Peter Wu
 */
interface IResourceService {
    /**
     * @return 所有资源
     */
    fun findAllResources(): List<IResource> {
        return emptyList()
    }

    fun supportsAnonymous(): Boolean {
        return true
    }
}
