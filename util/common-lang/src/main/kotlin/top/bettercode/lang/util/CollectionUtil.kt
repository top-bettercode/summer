package top.bettercode.lang.util

import java.lang.Integer.min

/**
 *
 * @author Peter Wu
 */
object CollectionUtil {

    @JvmStatic
    fun <T> partition(list: List<T>, maxLength: Int): List<List<T>> {
        val listSize = list.size
        return if (listSize > maxLength) {
            val result = mutableListOf<List<T>>()
            val size =
                if (listSize % maxLength == 0) listSize / maxLength else listSize / maxLength + 1
            for (i in 0 until size) {
                result.add(list.subList(i * maxLength, min((i + 1) * maxLength, listSize)))
            }
            result
        } else {
            listOf(list)
        }
    }

    /**
     * Returns an unmodifiable map containing a single mapping.
     * See [Unmodifiable Maps](#unmodifiable) for details.
     *
     * @param <K> the `Map`'s key type
     * @param <V> the `Map`'s value type
     * @param k1 the mapping's key
     * @param v1 the mapping's value
     * @return a `Map` containing the specified mapping
     * @throws NullPointerException if the key or the value is `null`
     *
     */
    @JvmStatic
    fun <K, V> mapOf(k1: K, v1: V): Map<K, V> {
        return mapOf(k1 to v1)
    }

    /**
     * Returns an unmodifiable map containing two mappings.
     * See [Unmodifiable Maps](#unmodifiable) for details.
     *
     * @param <K> the `Map`'s key type
     * @param <V> the `Map`'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @return a `Map` containing the specified mappings
     * @throws IllegalArgumentException if the keys are duplicates
     * @throws NullPointerException if any key or value is `null`
     *
     */
    @JvmStatic
    fun <K, V> mapOf(k1: K, v1: V, k2: K, v2: V): Map<K, V> {
        return kotlin.collections.mapOf(k1 to v1, k2 to v2)
    }

    /**
     * Returns an unmodifiable map containing three mappings.
     * See [Unmodifiable Maps](#unmodifiable) for details.
     *
     * @param <K> the `Map`'s key type
     * @param <V> the `Map`'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @return a `Map` containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is `null`
     *
     */
    @JvmStatic
    fun <K, V> mapOf(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V): Map<K, V> {
        return mapOf(k1 to v1, k2 to v2, k3 to v3)
    }

    /**
     * Returns an unmodifiable map containing four mappings.
     * See [Unmodifiable Maps](#unmodifiable) for details.
     *
     * @param <K> the `Map`'s key type
     * @param <V> the `Map`'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @return a `Map` containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is `null`
     *
     */
    @JvmStatic
    fun <K, V> mapOf(k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V): Map<K, V> {
        return kotlin.collections.mapOf(k1 to v1, k2 to v2, k3 to v3, k4 to v4)
    }

    /**
     * Returns an unmodifiable map containing five mappings.
     * See [Unmodifiable Maps](#unmodifiable) for details.
     *
     * @param <K> the `Map`'s key type
     * @param <V> the `Map`'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @param k5 the fifth mapping's key
     * @param v5 the fifth mapping's value
     * @return a `Map` containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is `null`
     *
     */
    @JvmStatic
    fun <K, V> mapOf(
        k1: K,
        v1: V,
        k2: K,
        v2: V,
        k3: K,
        v3: V,
        k4: K,
        v4: V,
        k5: K,
        v5: V
    ): Map<K, V> {
        return mapOf(k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5)
    }

    /**
     * Returns an unmodifiable map containing six mappings.
     * See [Unmodifiable Maps](#unmodifiable) for details.
     *
     * @param <K> the `Map`'s key type
     * @param <V> the `Map`'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @param k5 the fifth mapping's key
     * @param v5 the fifth mapping's value
     * @param k6 the sixth mapping's key
     * @param v6 the sixth mapping's value
     * @return a `Map` containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is `null`
     *
     */
    @JvmStatic
    fun <K, V> mapOf(
        k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V,
        k6: K, v6: V
    ): Map<K, V> {
        return kotlin.collections.mapOf(
            k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5,
            k6 to v6
        )
    }

    /**
     * Returns an unmodifiable map containing seven mappings.
     * See [Unmodifiable Maps](#unmodifiable) for details.
     *
     * @param <K> the `Map`'s key type
     * @param <V> the `Map`'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @param k5 the fifth mapping's key
     * @param v5 the fifth mapping's value
     * @param k6 the sixth mapping's key
     * @param v6 the sixth mapping's value
     * @param k7 the seventh mapping's key
     * @param v7 the seventh mapping's value
     * @return a `Map` containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is `null`
     *
     */
    @JvmStatic
    fun <K, V> mapOf(
        k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V,
        k6: K, v6: V, k7: K, v7: V
    ): Map<K, V> {
        return mapOf(
            k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5,
            k6 to v6, k7 to v7
        )
    }

    /**
     * Returns an unmodifiable map containing eight mappings.
     * See [Unmodifiable Maps](#unmodifiable) for details.
     *
     * @param <K> the `Map`'s key type
     * @param <V> the `Map`'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @param k5 the fifth mapping's key
     * @param v5 the fifth mapping's value
     * @param k6 the sixth mapping's key
     * @param v6 the sixth mapping's value
     * @param k7 the seventh mapping's key
     * @param v7 the seventh mapping's value
     * @param k8 the eighth mapping's key
     * @param v8 the eighth mapping's value
     * @return a `Map` containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is `null`
     *
     */
    @JvmStatic
    fun <K, V> mapOf(
        k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V,
        k6: K, v6: V, k7: K, v7: V, k8: K, v8: V
    ): Map<K, V> {
        return kotlin.collections.mapOf(
            k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5,
            k6 to v6, k7 to v7, k8 to v8
        )
    }

    /**
     * Returns an unmodifiable map containing nine mappings.
     * See [Unmodifiable Maps](#unmodifiable) for details.
     *
     * @param <K> the `Map`'s key type
     * @param <V> the `Map`'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @param k5 the fifth mapping's key
     * @param v5 the fifth mapping's value
     * @param k6 the sixth mapping's key
     * @param v6 the sixth mapping's value
     * @param k7 the seventh mapping's key
     * @param v7 the seventh mapping's value
     * @param k8 the eighth mapping's key
     * @param v8 the eighth mapping's value
     * @param k9 the ninth mapping's key
     * @param v9 the ninth mapping's value
     * @return a `Map` containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is `null`
     *
     */
    @JvmStatic
    fun <K, V> mapOf(
        k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V,
        k6: K, v6: V, k7: K, v7: V, k8: K, v8: V, k9: K, v9: V
    ): Map<K, V> {
        return mapOf(
            k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5,
            k6 to v6, k7 to v7, k8 to v8, k9 to v9
        )
    }

    /**
     * Returns an unmodifiable map containing ten mappings.
     * See [Unmodifiable Maps](#unmodifiable) for details.
     *
     * @param <K> the `Map`'s key type
     * @param <V> the `Map`'s value type
     * @param k1 the first mapping's key
     * @param v1 the first mapping's value
     * @param k2 the second mapping's key
     * @param v2 the second mapping's value
     * @param k3 the third mapping's key
     * @param v3 the third mapping's value
     * @param k4 the fourth mapping's key
     * @param v4 the fourth mapping's value
     * @param k5 the fifth mapping's key
     * @param v5 the fifth mapping's value
     * @param k6 the sixth mapping's key
     * @param v6 the sixth mapping's value
     * @param k7 the seventh mapping's key
     * @param v7 the seventh mapping's value
     * @param k8 the eighth mapping's key
     * @param v8 the eighth mapping's value
     * @param k9 the ninth mapping's key
     * @param v9 the ninth mapping's value
     * @param k10 the tenth mapping's key
     * @param v10 the tenth mapping's value
     * @return a `Map` containing the specified mappings
     * @throws IllegalArgumentException if there are any duplicate keys
     * @throws NullPointerException if any key or value is `null`
     *
     */
    @JvmStatic
    fun <K, V> mapOf(
        k1: K, v1: V, k2: K, v2: V, k3: K, v3: V, k4: K, v4: V, k5: K, v5: V,
        k6: K, v6: V, k7: K, v7: V, k8: K, v8: V, k9: K, v9: V, k10: K, v10: V
    ): Map<K, V> {
        return kotlin.collections.mapOf(
            k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5,
            k6 to v6, k7 to v7, k8 to v8, k9 to v9, k10 to v10
        )
    }

}