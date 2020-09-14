/*
 * Copyright 2004, 2005, 2006 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.bestwu.autodoc.gen

import java.util.*

/**
 *
 *
 * Adapter that wraps an `Enumeration` around a Java 2 collection
 * `Iterator`.
 *
 *
 *
 * Constructors are provided to easily create such wrappers.
 *
 *
 *
 * This class is based on code in Apache Tomcat.
 *
 *
 * @author Craig McClanahan
 * @author Andrey Grebnev
 */
class Enumerator<T> : Enumeration<T> {
    // ~ Instance fields
    // ================================================================================================
    /**
     * The `Iterator` over which the `Enumeration` represented by
     * this class actually operates.
     */
    private var iterator: Iterator<T>? = null
    // ~ Constructors
    // ===================================================================================================
    /**
     * Return an Enumeration over the values of the specified Collection.
     *
     * @param collection Collection whose values should be enumerated
     */
    constructor(collection: Collection<T>) : this(collection.iterator()) {}

    /**
     * Return an Enumeration over the values of the specified Collection.
     *
     * @param collection Collection whose values should be enumerated
     * @param clone true to clone iterator
     */
    constructor(collection: Collection<T>, clone: Boolean) : this(collection.iterator(), clone) {}

    /**
     * Return an Enumeration over the values returned by the specified Iterator.
     *
     * @param iterator Iterator to be wrapped
     */
    constructor(iterator: Iterator<T>?) {
        this.iterator = iterator
    }

    /**
     * Return an Enumeration over the values returned by the specified Iterator.
     *
     * @param iterator Iterator to be wrapped
     * @param clone true to clone iterator
     */
    constructor(iterator: Iterator<T>, clone: Boolean) {
        if (!clone) {
            this.iterator = iterator
        } else {
            val list: MutableList<T> = ArrayList()
            while (iterator.hasNext()) {
                list.add(iterator.next())
            }
            this.iterator = list.iterator()
        }
    }

    /**
     * Return an Enumeration over the values of the specified Map.
     *
     * @param map Map whose values should be enumerated
     */
    constructor(map: Map<*, T>) : this(map.values.iterator()) {}

    /**
     * Return an Enumeration over the values of the specified Map.
     *
     * @param map Map whose values should be enumerated
     * @param clone true to clone iterator
     */
    constructor(map: Map<*, T>, clone: Boolean) : this(map.values.iterator(), clone) {}
    // ~ Methods
    // ========================================================================================================
    /**
     * Tests if this enumeration contains more elements.
     *
     * @return `true` if and only if this enumeration object contains at least
     * one more element to provide, `false` otherwise
     */
    override fun hasMoreElements(): Boolean {
        return iterator!!.hasNext()
    }

    /**
     * Returns the next element of this enumeration if this enumeration has at least one
     * more element to provide.
     *
     * @return the next element of this enumeration
     *
     * @exception NoSuchElementException if no more elements exist
     */
    @Throws(NoSuchElementException::class)
    override fun nextElement(): T {
        return iterator!!.next()
    }
}