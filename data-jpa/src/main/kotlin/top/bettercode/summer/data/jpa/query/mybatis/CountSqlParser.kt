package top.bettercode.summer.data.jpa.query.mybatis

import net.sf.jsqlparser.expression.Alias
import net.sf.jsqlparser.expression.Function
import net.sf.jsqlparser.expression.Parenthesis
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.select.*
import org.springframework.util.StringUtils
import java.util.*

/**
 * sql解析类，提供更智能的count查询sql
 *
 * @author liuzh
 */
class CountSqlParser {
    //<editor-fold desc="聚合函数">
    private val skipFunctions = Collections.synchronizedSet(HashSet<String>())
    private val falseFunctions = Collections.synchronizedSet(HashSet<String>())

    /*
   * 获取智能的countSql
   */
    fun getSmartCountSql(sql: String?): String {
        return getSmartCountSql(sql, "0")
    }

    /*
   * 获取智能的countSql
   *
   */
    fun getSmartCountSql(sql: String?, countColumn: String): String {
        //解析SQL
        //特殊sql不需要去掉order by时，使用注释前缀
        if (sql!!.contains(KEEP_ORDERBY)) {
            return getSimpleCountSql(sql, countColumn)
        }
        val stmt: Statement = try {
            CCJSqlParserUtil.parse(sql)
        } catch (e: Throwable) {
            //无法解析的用一般方法返回count语句
            return getSimpleCountSql(sql, countColumn)
        }
        val select = stmt as Select
        val selectBody = select.selectBody
        try {
            //处理body-去order by
            processSelectBody(selectBody)
        } catch (e: Exception) {
            //当 sql 包含 group by 时，不去除 order by
            return getSimpleCountSql(sql, countColumn)
        }
        //处理with-去order by
        processWithItemsList(select.withItemsList)
        //处理为count查询
        sqlToCount(select, countColumn)
        return select.toString()
    }

    /**
     * 获取普通的Count-sql
     *
     * @param sql 原查询sql
     * @return 返回count查询sql
     */
    fun getSimpleCountSql(sql: String?): String {
        return getSimpleCountSql(sql, "0")
    }

    /**
     * 获取普通的Count-sql
     *
     * @param sql  原查询sql
     * @param name name
     * @return 返回count查询sql
     */
    fun getSimpleCountSql(sql: String?, name: String): String {
        return ("select count("
                + name
                + ") from ( \n"
                + sql
                + "\n ) tmp_count")
    }

    /*
   * 将sql转换为count查询
   */
    fun sqlToCount(select: Select, name: String) {
        val selectBody = select.selectBody
        // 是否能简化count查询
        val countItems: MutableList<SelectItem> = ArrayList()
        countItems.add(SelectExpressionItem(Column("count($name)")))
        if (selectBody is PlainSelect && isSimpleCount(selectBody)) {
            selectBody.selectItems = countItems
        } else {
            val plainSelect = PlainSelect()
            val subSelect = SubSelect()
            subSelect.selectBody = selectBody
            subSelect.alias = TABLE_ALIAS
            plainSelect.fromItem = subSelect
            plainSelect.selectItems = countItems
            select.selectBody = plainSelect
        }
    }

    /*
   * 是否可以用简单的count查询方式
   */
    fun isSimpleCount(select: PlainSelect): Boolean {
        //包含group by的时候不可以
        if (select.groupBy != null) {
            return false
        }
        //包含distinct的时候不可以
        if (select.distinct != null) {
            return false
        }
        //#606,包含having时不可以
        if (select.having != null) {
            return false
        }
        for (item in select.selectItems) {
            //select列中包含参数的时候不可以，否则会引起参数个数错误
            if (item.toString().contains("?")) {
                return false
            }
            //如果查询列中包含函数，也不可以，函数可能会聚合列
            if (item is SelectExpressionItem) {
                val expression = item.expression
                if (expression is Function) {
                    val name = expression.name
                    if (name != null) {
                        val name1 = name.uppercase(Locale.getDefault())
                        if (skipFunctions.contains(name1)) {
                            //go on
                        } else if (falseFunctions.contains(name1)) {
                            return false
                        } else {
                            for (aggregateFunction in AGGREGATE_FUNCTIONS) {
                                if (name1.startsWith(aggregateFunction)) {
                                    falseFunctions.add(name1)
                                    return false
                                }
                            }
                            skipFunctions.add(name1)
                        }
                    }
                } else if (expression is Parenthesis
                        && item.alias != null) {
                    //#555，当存在 (a+b) as c 时，c 如果出现了 order by 或者 having 中时，会找不到对应的列，
                    // 这里想要更智能，需要在整个SQL中查找别名出现的位置，暂时不考虑，直接排除
                    return false
                }
            }
        }
        return true
    }

    /*
   * 处理selectBody去除Order by
   */
    fun processSelectBody(selectBody: SelectBody?) {
        if (selectBody != null) {
            if (selectBody is PlainSelect) {
                processPlainSelect(selectBody)
            } else if (selectBody is WithItem) {
                if (selectBody.subSelect != null) {
                    processSelectBody(selectBody.subSelect.selectBody)
                }
            } else {
                val operationList = selectBody as SetOperationList
                if (operationList.selects != null && operationList.selects.size > 0) {
                    val plainSelects = operationList.selects
                    for (plainSelect in plainSelects) {
                        processSelectBody(plainSelect)
                    }
                }
                if (!orderByHashParameters(operationList.orderByElements)) {
                    operationList.orderByElements = null
                }
            }
        }
    }

    /*
   * 处理PlainSelect类型的selectBody
   */
    fun processPlainSelect(plainSelect: PlainSelect) {
        if (!orderByHashParameters(plainSelect.orderByElements)) {
            plainSelect.orderByElements = null
        }
        if (plainSelect.fromItem != null) {
            processFromItem(plainSelect.fromItem)
        }
        if (plainSelect.joins != null && plainSelect.joins.size > 0) {
            val joins = plainSelect.joins
            for (join in joins) {
                if (join.rightItem != null) {
                    processFromItem(join.rightItem)
                }
            }
        }
    }

    /*
   * 处理WithItem
   */
    fun processWithItemsList(withItemsList: List<WithItem>?) {
        if (!withItemsList.isNullOrEmpty()) {
            for (item in withItemsList) {
                if (item.subSelect != null) {
                    processSelectBody(item.subSelect.selectBody)
                }
            }
        }
    }

    /*
   * 处理子查询
   */
    fun processFromItem(fromItem: FromItem?) {
        when (fromItem) {
            is SubJoin -> {
                if (fromItem.joinList != null && fromItem.joinList.size > 0) {
                    for (join in fromItem.joinList) {
                        if (join.rightItem != null) {
                            processFromItem(join.rightItem)
                        }
                    }
                }
                if (fromItem.left != null) {
                    processFromItem(fromItem.left)
                }
            }

            is SubSelect -> {
                if (fromItem.selectBody != null) {
                    processSelectBody(fromItem.selectBody)
                }
            }

            is ValuesList -> {
            }

            is LateralSubSelect -> {
                if (fromItem.subSelect != null) {
                    val subSelect = fromItem.subSelect
                    if (subSelect.selectBody != null) {
                        processSelectBody(subSelect.selectBody)
                    }
                }
            }
        }
        //Table时不用处理
    }

    /*
   * 判断Orderby是否包含参数，有参数的不能去
   */
    fun orderByHashParameters(orderByElements: List<OrderByElement>?): Boolean {
        if (orderByElements == null) {
            return false
        }
        for (orderByElement in orderByElements) {
            if (orderByElement.toString().contains("?")) {
                return true
            }
        }
        return false
    }

    companion object {
        const val KEEP_ORDERBY = "/*keep orderby*/"
        private val TABLE_ALIAS: Alias = Alias("table_count")

        /**
         * 聚合函数，以下列函数开头的都认为是聚合函数
         */
        private val AGGREGATE_FUNCTIONS: MutableSet<String> = HashSet(listOf(
                *("APPROX_COUNT_DISTINCT," +
                        "ARRAY_AGG," +
                        "AVG," +
                        "BIT_," +  //"BIT_AND," +
                        //"BIT_OR," +
                        //"BIT_XOR," +
                        "BOOL_," +  //"BOOL_AND," +
                        //"BOOL_OR," +
                        "CHECKSUM_AGG," +
                        "COLLECT," +
                        "CORR," +  //"CORR_," +
                        //"CORRELATION," +
                        "COUNT," +  //"COUNT_BIG," +
                        "COVAR," +  //"COVAR_POP," +
                        //"COVAR_SAMP," +
                        //"COVARIANCE," +
                        //"COVARIANCE_SAMP," +
                        "CUME_DIST," +
                        "DENSE_RANK," +
                        "EVERY," +
                        "FIRST," +
                        "GROUP," +  //"GROUP_CONCAT," +
                        //"GROUP_ID," +
                        //"GROUPING," +
                        //"GROUPING," +
                        //"GROUPING_ID," +
                        "JSON_," +  //"JSON_AGG," +
                        //"JSON_ARRAYAGG," +
                        //"JSON_OBJECT_AGG," +
                        //"JSON_OBJECTAGG," +
                        //"JSONB_AGG," +
                        //"JSONB_OBJECT_AGG," +
                        "LAST," +
                        "LISTAGG," +
                        "MAX," +
                        "MEDIAN," +
                        "MIN," +
                        "PERCENT_," +  //"PERCENT_RANK," +
                        //"PERCENTILE_CONT," +
                        //"PERCENTILE_DISC," +
                        "RANK," +
                        "REGR_," +
                        "SELECTIVITY," +
                        "STATS_," +  //"STATS_BINOMIAL_TEST," +
                        //"STATS_CROSSTAB," +
                        //"STATS_F_TEST," +
                        //"STATS_KS_TEST," +
                        //"STATS_MODE," +
                        //"STATS_MW_TEST," +
                        //"STATS_ONE_WAY_ANOVA," +
                        //"STATS_T_TEST_*," +
                        //"STATS_WSR_TEST," +
                        "STD," +  //"STDDEV," +
                        //"STDDEV_POP," +
                        //"STDDEV_SAMP," +
                        //"STDDEV_SAMP," +
                        //"STDEV," +
                        //"STDEVP," +
                        "STRING_AGG," +
                        "SUM," +
                        "SYS_OP_ZONE_ID," +
                        "SYS_XMLAGG," +
                        "VAR," +  //"VAR_POP," +
                        //"VAR_SAMP," +
                        //"VARIANCE," +
                        //"VARIANCE_SAMP," +
                        //"VARP," +
                        "XMLAGG").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

        //</editor-fold>
        init {
            TABLE_ALIAS.isUseAs = false
        }

        /*
   * 添加到聚合函数，可以是逗号隔开的多个函数前缀
   */
        fun addAggregateFunctions(functions: String) {
            if (StringUtils.hasText(functions)) {
                val funs = functions.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (`fun` in funs) {
                    AGGREGATE_FUNCTIONS.add(`fun`.uppercase(Locale.getDefault()))
                }
            }
        }
    }
}
