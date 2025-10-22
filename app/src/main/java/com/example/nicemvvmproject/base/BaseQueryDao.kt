package com.example.nicemvvmproject.base

/**
 * @Description:
 * @Author yanxin
 * @Date:  2025/10/21
 */

interface BaseQueryDao<T> {
    fun getAll(): List<T>
    fun findById(id: Long): T?
}