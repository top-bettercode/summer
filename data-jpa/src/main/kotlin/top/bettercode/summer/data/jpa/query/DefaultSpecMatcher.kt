package top.bettercode.summer.data.jpa.query

/**
 * @author Peter Wu
 */
open class DefaultSpecMatcher<T : Any?> protected constructor(matcherMode: SpecMatcherMode, probe: T?) : SpecMatcher<T, DefaultSpecMatcher<T>>(matcherMode, probe) {
    companion object {

        /**
         * 创建 SpecMatcher 实例
         *
         * @param <T> T
         * @return SpecMatcher 实例
        </T> */
        fun <T : Any?> matching(): SpecMatcher<T, DefaultSpecMatcher<T>> {
            return DefaultSpecMatcher(SpecMatcherMode.ALL, null)
        }

        fun <T : Any?> matching(probe: T): SpecMatcher<T, DefaultSpecMatcher<T>> {
            return DefaultSpecMatcher(SpecMatcherMode.ALL, probe)
        }

        /**
         * 创建 SpecMatcher 实例
         *
         * @param <T> T
         * @return SpecMatcher 实例
        </T> */
        fun <T : Any?> matchingAny(): SpecMatcher<T, DefaultSpecMatcher<T>> {
            return DefaultSpecMatcher(SpecMatcherMode.ANY, null)
        }

        fun <T : Any?> matchingAny(probe: T): SpecMatcher<T, DefaultSpecMatcher<T>> {
            return DefaultSpecMatcher(SpecMatcherMode.ANY, probe)
        }
    }
}
