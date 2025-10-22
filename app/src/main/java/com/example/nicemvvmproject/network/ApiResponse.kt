package com.example.nicemvvmproject.network

/**
 * @Description:
 * @Author yanxin
 * @Date:  2025/10/21
 */

data class ApiResponse<T>(
    val code: Int,
    val msg: String,
    val data: T
) {
    /**
     * 判断请求是否在业务上成功
     * 很多公司的后端会用 200 表示成功，这里可以根据实际情况修改
     */
    val isSuccess: Boolean
        get() = code == 200
}
