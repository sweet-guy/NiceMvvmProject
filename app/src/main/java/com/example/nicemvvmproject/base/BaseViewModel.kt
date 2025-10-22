package com.example.nicemvvmproject.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber

open class BaseViewModel : ViewModel() {

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage: MutableLiveData<String?> = MutableLiveData(null)
    val errorMessage: LiveData<String?> = _errorMessage

    // 用于管理协程任务
    private val supervisorJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + supervisorJob)

    /**
     * 在 ViewModelScope 中启动协程
     * 子类直接调用 launchRequest { ... }
     */
    protected fun launchRequest(
        onError: (Throwable) -> Unit = {},
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    /**
     * 启动协程并自动捕获异常，可方便定位协程任务出现的位置
     * @param taskName 任务名称，方便定位
     */
    protected fun launchWithHandler(
        taskName: String,
        block: suspend () -> Unit
    ) {
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            // 从 context 中取出任务名
            val name = context[CoroutineName]?.name ?: "UnknownTask"
            Timber.e(throwable, "协程任务 [$name] 出现异常: ${throwable.message}")
            throwable.printStackTrace()
        }

        viewModelScope.launch(CoroutineName(taskName) + exceptionHandler) {
            block()
        }
    }

    /**
     * 延迟执行
     */
    protected fun safeDelay(
        delayTime: Long,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return viewModelScope.launch {
            delay(delayTime)
            block()
        }
    }

    /**
     * 切换到主线程执行
     */
    protected suspend fun <T> withMainContext(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.Main) {
            block()
        }
    }

    /**
     * 切换到IO线程执行
     */
    protected suspend fun <T> withIOContext(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.IO) {
            block()
        }
    }
    /**
     * 带超时的协程启动
     */
    protected fun launchWithTimeout(
        timeoutMs: Long = 30000,
        onError: (Throwable) -> Unit = {},
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                withTimeout(timeoutMs) {
                    block()
                }
            } catch (e: TimeoutCancellationException) {
                onError(e)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("${this::class.simpleName} -> onCleared")
    }
}


