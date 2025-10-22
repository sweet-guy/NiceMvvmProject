package com.example.nicemvvmproject.bean

import androidx.fragment.app.Fragment

/**
 * @Description:
 * @Author yanxin
 * @Date:  2025/10/20
 */

class TabItem(
    val id: Int,// 对应 menu itemId
    val title: String,
    val iconRes: Int,
    val fragment: Fragment
) // 对应 Fragment 类