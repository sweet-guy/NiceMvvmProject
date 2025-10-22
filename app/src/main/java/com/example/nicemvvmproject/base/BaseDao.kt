package com.example.nicemvvmproject.base

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

/**
 * @Description:
 * @Author yanxin
 * @Date:  2025/10/21
 */

interface BaseDao<T> {
    @Insert
    fun insert(entity: T): Long

    @Update
    fun update(entity: T): Int

    @Delete
    fun delete(entity: T): Int
}