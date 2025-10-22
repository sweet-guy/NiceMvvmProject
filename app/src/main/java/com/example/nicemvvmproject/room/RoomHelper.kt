package com.example.nicemvvmproject.room

import android.content.Context
import androidx.room.Room
import com.example.nicemvvmproject.base.BaseDao
import com.example.nicemvvmproject.base.BaseQueryDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @Description:
 * @Author yanxin
 * @Date:  2025/10/21
 */

object RoomHelper {

    @Volatile
    private var INSTANCE: AppDatabase? = null
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 初始化数据库
     */
    fun init(context: Context) {
        if (INSTANCE == null) {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
        }
    }

    fun getDatabase(): AppDatabase {
        return INSTANCE ?: throw IllegalStateException("RoomHelper 未初始化，请先调用 init()")
    }

    /**
     * 通用执行器
     */
    fun <T> execute(
        block: suspend () -> T,
        callback: (T) -> Unit = {}
    ) {
        ioScope.launch {
            val result = block()
            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }

    /**
     * 插入数据
     */
    fun <T> insert(
        dao: BaseDao<T>,
        entity: T,
        onSuccess: (Boolean, Long) -> Unit = { _, _ -> }
    ) {
        execute(
            block = {
                val id = dao.insert(entity)
                Pair(id > 0, id)
            },
            callback = { (success, id) -> onSuccess(success, id) }
        )
    }

    /**
     * 更新数据
     */
    fun <T> update(
        dao: BaseDao<T>,
        entity: T,
        onSuccess: (Boolean) -> Unit = {}
    ) {
        execute(
            block = {
                val rows = dao.update(entity)
                rows > 0
            },
            callback = onSuccess
        )
    }

    /**
     * 删除数据
     */
    fun <T> delete(
        dao: BaseDao<T>,
        entity: T,
        onSuccess: (Boolean) -> Unit = {}
    ) {
        execute(
            block = {
                val rows = dao.delete(entity)
                rows > 0
            },
            callback = onSuccess
        )
    }

    /**
     * 查询全部
     */
    fun <T> queryAll(
        dao: BaseQueryDao<T>,
        callback: (List<T>) -> Unit
    ) {
        execute(
            block = { dao.getAll() },
            callback = callback
        )
    }

    /**
     * 查询单条（根据ID）
     */
    fun <T> queryById(
        dao: BaseQueryDao<T>,
        id: Long,
        callback: (T?) -> Unit
    ) {
        execute(
            block = { dao.findById(id) },
            callback = callback
        )
    }

    // 数据库操作示例
    /**
     * val userDao = RoomHelper.getDatabase().userDao()
     *
     * // 插入
     * RoomHelper.insert(userDao, User(name = "张三", age = 20)) { success, id ->
     *     if (success) {
     *         Log.d("RoomDemo", "插入成功 ID=$id")
     *     } else {
     *         Log.d("RoomDemo", "插入失败")
     *     }
     * }
     *
     * // 更新
     * RoomHelper.update(userDao, User(id = 1, name = "李四", age = 25)) { success ->
     *     Log.d("RoomDemo", if (success) "更新成功" else "更新失败")
     * }
     *
     * // 删除
     * RoomHelper.delete(userDao, User(id = 1, name = "李四", age = 25)) { success ->
     *     Log.d("RoomDemo", if (success) "删除成功" else "删除失败")
     * }
     *
     * // 查询全部
     * RoomHelper.queryAll(userDao) { users ->
     *     Log.d("RoomDemo", "查询到 ${users.size} 条数据")
     * }
     *
     * // 按ID查询
     * RoomHelper.queryById(userDao, 1) { user ->
     *     Log.d("RoomDemo", "查询到用户: $user")
     * }
     */

}