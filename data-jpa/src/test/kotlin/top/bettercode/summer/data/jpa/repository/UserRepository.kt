package top.bettercode.summer.data.jpa.repository

import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.SelectProvider
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.Query
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.query.Param
import top.bettercode.summer.data.jpa.BaseRepository
import top.bettercode.summer.data.jpa.domain.User
import top.bettercode.summer.data.jpa.querydsl.RecycleQuerydslPredicateExecutor
import top.bettercode.summer.data.jpa.resp.AUser
import top.bettercode.summer.data.jpa.resp.CUser
import top.bettercode.summer.data.jpa.resp.CUsers
import top.bettercode.summer.data.jpa.support.QueryFirst
import top.bettercode.summer.data.jpa.support.Size
import java.util.stream.Stream
import javax.transaction.Transactional

interface UserRepository : BaseRepository<User, Int>, QuerydslPredicateExecutor<User>, RecycleQuerydslPredicateExecutor<User> {
    fun findByLastName(lastName: String?): List<User?>?
    fun findByFirstName(lastName: String?, pageable: Pageable?): Page<User?>?

    @Query(value = "select * from t_user where first_name = ?1", nativeQuery = true)
    fun selectNativeSql(@Suppress("LocalVariableName") first_name: String?, pageable: Pageable?): Page<User?>

    @Transactional
    fun deleteByLastName(lastName: String?)

    //--------------------------------------------
    fun selectResultMap(user: User?): List<CUser?>?
    fun selectResultMap2(user: User?): List<CUsers?>?

    //  Page<CUsers> selectResultMap2(User user, Pageable pageable);
    fun selectResultMap3(user: User?): List<AUser?>?
    fun userResultWithSelect(user: User?): List<AUser?>?
    fun selectResultOne3(user: User?): AUser?

    @QueryFirst
    fun selectResultFirst3(user: User?): AUser?
    fun selectResultMap3(user: User?, pageable: Pageable?): Page<AUser?>?
    fun selectMybatisAll(): List<User?>?
    fun selectMybatisAllVal(): List<User?>?
    fun selectMybatisAllVal2(): List<User?>?
    fun selectMybatisMapList(): List<Map<String?, String?>?>?
    fun selectMybatisMap(): Map<String?, String?>?
    fun selectMybatisAll(pageable: Pageable?): Page<User?>?

    @Select("select * from t_user where deleted = 0")
    fun selectByMybatisSize(size: Size?): List<User?>?

    @SelectProvider(type = UserSqlProvider::class, method = "selectByMybatisSize")
    fun selectByMybatisProviderSize(size: Size?): List<User?>?

    class UserSqlProvider {
        @Suppress("UNUSED_PARAMETER")
        fun selectByMybatisSize(size: Size?): String {
            // language=SQL
            return "select * from t_user where deleted = 0"
        }
    }

    fun selectMybatisIfParam(firstName: String?, lastName: String?): List<User?>?
    fun selectMybatisStream(firstName: String?, lastName: String?): Stream<User?>
    fun selectByMybatisMap(param: Map<String, String>?): List<User?>?
    fun selectByMybatisMap(pageable: Pageable?, param: Map<String, String>?): Page<User?>
    fun selectByMybatisEntity(@Param("user") @org.apache.ibatis.annotations.Param("user") user: User?, pageable: Pageable?): List<User?>?
    fun selectByMybatisSort(firstName: String?, sort: Sort?): List<User?>?
    fun selectByMybatisSort(firstName: String?, pageable: Pageable?): Page<User?>?
    @QueryFirst
    fun selectOneByMybatis(firstName: String?): User?

    @Transactional
    fun insert(firstName: String?, lastName: String?): Int

    @Transactional
    fun update(id: Int?, lastName: String?): Int

    @Transactional
    fun updateNoReturn(id: Int?, lastName: String?)

    @Transactional
    fun deleteMybatis(id: Int?): Int

    @Transactional
    fun deleteMybatisNoResturn(id: Int?)
}