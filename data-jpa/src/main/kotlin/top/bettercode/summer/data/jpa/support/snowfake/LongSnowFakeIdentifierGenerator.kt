package top.bettercode.summer.data.jpa.support.snowfake

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator
import top.bettercode.summer.tools.lang.snowfake.Sequence
import java.io.Serializable

/**
 * @author Peter Wu
 */
open class LongSnowFakeIdentifierGenerator : IdentifierGenerator {
    override fun generate(session: SharedSessionContractImplementor, `object`: Any): Serializable {
        return WORKER.nextId()
    }

    companion object {
        private val WORKER = Sequence()
    }
}
