package top.bettercode.summer.data.jpa.support

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import top.bettercode.summer.data.jpa.BaseService
import top.bettercode.summer.data.jpa.domain.User
import top.bettercode.summer.data.jpa.repository.UserRepository
import javax.transaction.Transactional

/**
 *
 * @author Peter Wu
 */
@Service
class UserService(repository: UserRepository) : BaseService<User, Int, UserRepository>(repository) {


    @Transactional
    fun findSave() {
        val first = findFirst(null).orElse(null)
        first.firstName = "wu"
    }

    @Transactional
    fun findAllSave() {
        findAll().forEach {
            it.firstName = "wu"
        }
    }

    @Transactional
    fun findMybatisSave(firstName: String) {
        val user = repository.selectOneByMybatis(firstName)
        user?.firstName = "wu"
    }

    @Transactional
    fun findMybatisAllSave(firstName: String) {
        val user = repository.findByFirstName(firstName, Pageable.unpaged())
        user?.forEach {
            it?.firstName = "wu"
        }
    }

    @Transactional
    fun findMybatisAllSizeSave() {
        val user = repository.selectByMybatisSize(Size.of(2))
        user?.forEach {
            it?.firstName = "wu"
        }
    }
}