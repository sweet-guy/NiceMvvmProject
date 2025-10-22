package com.example.nicemvvmproject.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nicemvvmproject.bean.User
import com.example.nicemvvmproject.dao.UserDao

/**
 * @Description:
 * @Author yanxin
 * @Date:  2025/10/21
 */

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}