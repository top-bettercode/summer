package top.bettercode.summer.data.jpa.repository

import org.apache.ibatis.session.SqlSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Assert
import top.bettercode.summer.data.jpa.domain.User
import top.bettercode.summer.tools.lang.log.SqlAppender

/**
 * @author Peter Wu
 */
@Service
class Service {
    @Autowired
    lateinit var repository: UserRepository

    @Autowired
    lateinit var sqlSession: SqlSession

    @Transactional
    fun testService() {
        var all = repository.findAll()
        val size = all.size
        System.err.println(size)
        var byMybatis = repository.selectMybatisAll()
        val size1 = byMybatis!!.size
        System.err.println(size1)
        SqlAppender.disableAutoFlush()
        Assert.isTrue(size == size1, "查询结果不一致")
        val dave = User("Dave", "Matthews")
        repository.save(dave)
        val users1 = sqlSession
            .selectList<Any>(UserRepository::class.java.name + ".selectMybatisAll")
        System.err.println(users1.size)
        //开启日志后，自动flush
        Assert.isTrue(users1.size == 4, "查询结果不对")
        all = repository.findAll()
        val size2 = all.size
        System.err.println(size2)
        //开启日志后，自动flush
        Assert.isTrue(size2 != users1.size, "查询结果不一致")
        byMybatis = repository.selectMybatisAll()
        val size3 = byMybatis!!.size
        System.err.println(size3)
        Assert.isTrue(size2 == size3, "查询结果不一致")
    }
}
