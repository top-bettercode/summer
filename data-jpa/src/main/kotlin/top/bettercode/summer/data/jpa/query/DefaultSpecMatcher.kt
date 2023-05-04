package top.bettercode.summer.data.jpa.query

/**
 * @author Peter Wu
 */
open class DefaultSpecMatcher<T> protected constructor(matcherMode: SpecMatcherMode, probe: T?) : SpecMatcher<T, DefaultSpecMatcher<T>>(matcherMode, probe) {
    companion object {
        private const val serialVersionUID = 1L

        /**
         * 创建 SpecMatcher 实例
         *
         * @param <T> T
         * @return SpecMatcher 实例
        </T> */
        fun <T> matching(): SpecMatcher<T, DefaultSpecMatcher<T>> {
            return DefaultSpecMatcher(SpecMatcherMode.ALL, null)
        }

        fun <T> matching(probe: T): SpecMatcher<T, DefaultSpecMatcher<T>> {
            return DefaultSpecMatcher(SpecMatcherMode.ALL, probe)
        }

        /**
         * 创建 SpecMatcher 实例
         *
         * @param <T> T
         * @return SpecMatcher 实例
        </T> */
        fun <T> matchingAny(): SpecMatcher<T, DefaultSpecMatcher<T>> {
            return DefaultSpecMatcher(SpecMatcherMode.ANY, null)
        }

        fun <T> matchingAny(probe: T): SpecMatcher<T, DefaultSpecMatcher<T>> {
            return DefaultSpecMatcher(SpecMatcherMode.ANY, probe)
        }
    }
}
