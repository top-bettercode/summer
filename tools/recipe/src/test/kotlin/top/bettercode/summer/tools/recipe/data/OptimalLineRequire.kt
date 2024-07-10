package top.bettercode.summer.tools.recipe.data

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/** 配方推优产品大类公司产线配方 对应数据库表名：p_optimal_line_require  */
class OptimalLineRequire {
    /** 配方推优产品大类公司产线配方主键  */
    /** 配方推优产品大类公司产线ID  */
    @JsonProperty("optimal_line_id")
    var optimalLineId: Long? = null
    /**
     * @return 配方推优ID
     */
    /** 配方推优ID  */
    @JsonProperty("optimal_id")
    var optimalId: Long? = null

    /**
     * @return 配方要求信息
     */
    /** 配方要求信息  */
    @JsonProperty("requirement")
    var requirement: String? = null

    /**
     * @return 创建时间 默认值：CURRENT_TIMESTAMP
     */
    /** 创建时间 默认值：CURRENT_TIMESTAMP  */
    @JsonProperty("created_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var createdDate: Date? = null
}
