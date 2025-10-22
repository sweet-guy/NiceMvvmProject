package com.example.nicemvvmproject.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.nicemvvmproject.base.BaseDao
import com.example.nicemvvmproject.base.BaseQueryDao
import com.example.nicemvvmproject.bean.User

/**
 * @Description:
 * @Author yanxin
 * @Date:  2025/10/21
 */
@Dao
interface UserDao : BaseDao<User>, BaseQueryDao<User> {
    @Query("SELECT * FROM User")
    override fun getAll(): List<User>

    @Query("SELECT * FROM User WHERE id = :id")
    override fun findById(id: Long): User?
    override fun insert(entity: User): Long
    override fun delete(entity: User): Int
    override fun update(entity: User): Int
}