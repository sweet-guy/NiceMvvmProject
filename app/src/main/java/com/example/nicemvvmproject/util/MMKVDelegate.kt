package com.example.nicemvvmproject.util

import com.tencent.mmkv.MMKV
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @Description:MVVKV 委托类
 * @Author yanxin
 * @Date:  2025/10/22
 */

class MMKVDelegate<T>(
    private val key: String,
    private val default: T,
    private val mmkv: MMKV = MMKV.defaultMMKV(MMKV.SINGLE_PROCESS_MODE, "my_secret_key")
) : ReadWriteProperty<Any?, T> {

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (default) {
            is String -> mmkv.decodeString(key, default) as T
            is Int -> mmkv.decodeInt(key, default) as T
            is Boolean -> mmkv.decodeBool(key, default) as T
            is Float -> mmkv.decodeFloat(key, default) as T
            is Long -> mmkv.decodeLong(key, default) as T
            is Double -> mmkv.decodeDouble(key, default) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        when (value) {
            is String -> mmkv.encode(key, value)
            is Int -> mmkv.encode(key, value)
            is Boolean -> mmkv.encode(key, value)
            is Float -> mmkv.encode(key, value)
            is Long -> mmkv.encode(key, value)
            is Double -> mmkv.encode(key, value)
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }
}