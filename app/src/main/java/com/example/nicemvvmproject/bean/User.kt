package com.example.nicemvvmproject.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @Description:
 * @Author yanxin
 * @Date:  2025/10/20
 */
@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String
)
