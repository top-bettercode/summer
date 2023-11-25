package top.bettercode.summer.data.jpa.support

import org.springframework.stereotype.Service
import top.bettercode.summer.data.jpa.BaseService
import top.bettercode.summer.data.jpa.domain.User
import top.bettercode.summer.data.jpa.repository.UserRepository

/**
 *
 * @author Peter Wu
 */
@Service
class UserService(repository: UserRepository) : BaseService<User, Int, UserRepository>(repository) {
}