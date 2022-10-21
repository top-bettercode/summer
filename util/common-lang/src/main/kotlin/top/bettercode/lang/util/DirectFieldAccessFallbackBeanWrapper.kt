/*
 * Copyright 2012-2021 the original author or authors.
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
package top.bettercode.lang.util

import org.springframework.beans.BeanWrapperImpl
import org.springframework.beans.NotReadablePropertyException
import org.springframework.beans.NotWritablePropertyException
import org.springframework.util.ReflectionUtils

/**
 * Custom extension of [BeanWrapperImpl] that falls back to direct field access in case the object or type being
 * wrapped does not use accessor methods.
 */
class DirectFieldAccessFallbackBeanWrapper : BeanWrapperImpl {
    constructor(entity: Any) : super(entity)
    constructor(type: Class<*>) : super(type)

    /*
	 * (non-Javadoc)
	 * @see org.springframework.beans.BeanWrapperImpl#getPropertyValue(java.lang.String)
	 */
    override fun getPropertyValue(propertyName: String): Any? {
        return try {
            super.getPropertyValue(propertyName)
        } catch (e: NotReadablePropertyException) {
            val field = ReflectionUtils.findField(wrappedClass, propertyName)
                ?: throw NotReadablePropertyException(
                    wrappedClass, propertyName,
                    "Could not find field for property during fallback access!"
                )
            ReflectionUtils.makeAccessible(field)
            ReflectionUtils.getField(field, wrappedInstance)
        }
    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.beans.BeanWrapperImpl#setPropertyValue(java.lang.String, java.lang.Object)
	 */
    override fun setPropertyValue(propertyName: String, value: Any?) {
        try {
            super.setPropertyValue(propertyName, value)
        } catch (e: NotWritablePropertyException) {
            val field = ReflectionUtils.findField(wrappedClass, propertyName)
                ?: throw NotWritablePropertyException(
                    wrappedClass, propertyName,
                    "Could not find field for property during fallback access!", e
                )
            ReflectionUtils.makeAccessible(field)
            ReflectionUtils.setField(field, wrappedInstance, value)
        }
    }
}