package com.example.nicemvvmproject.base

/**
 * @Description:
 * @Author yanxin
 * @Date:  2025/10/16
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class BaseFragment<VM : BaseViewModel, DB : ViewDataBinding> : Fragment() {

    protected lateinit var viewModel: VM
    protected lateinit var binding: DB

    abstract fun getLayoutId(): Int
    abstract fun getViewModelClass(): Class<VM>
    abstract fun initView()
    abstract fun initObserver()
    open fun initData() {}
    open fun getViewModelFactory(): ViewModelProvider.Factory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onFragmentCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = if (getViewModelFactory() != null) {
            ViewModelProvider(this, getViewModelFactory()!!)[getViewModelClass()]
        } else {
            ViewModelProvider(this)[getViewModelClass()]
        }
        initView()
        initObserver()
        initData()
        onFragmentViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        onFragmentStarted()
    }

    override fun onResume() {
        super.onResume()
        onFragmentResumed()
    }

    override fun onPause() {
        super.onPause()
        onFragmentPaused()
    }

    override fun onStop() {
        super.onStop()
        onFragmentStopped()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onFragmentViewDestroyed()
    }

    override fun onDestroy() {
        super.onDestroy()
        onFragmentDestroyed()
    }

    /** 生命周期钩子方法：子类可选择覆写 */
    open fun onFragmentCreated(savedInstanceState: Bundle?) {}
    open fun onFragmentViewCreated(view: View, savedInstanceState: Bundle?) {}
    open fun onFragmentStarted() {}
    open fun onFragmentResumed() {}
    open fun onFragmentPaused() {}
    open fun onFragmentStopped() {}
    open fun onFragmentViewDestroyed() {}
    open fun onFragmentDestroyed() {}

    /**
     * 协程：在 STARTED 生命周期内安全收集/执行
     */
    protected fun launchWhenStarted(block: suspend CoroutineScope.() -> Unit): Job {
        return viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                block()
            }
        }
    }

    /**
     * 协程：简单启动，跟随 viewLifecycleOwner 自动取消
     */
    protected fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return viewLifecycleOwner.lifecycleScope.launch { block() }
    }

    /**
     * Flow 收集：在指定生命周期内自动开始/停止
     */
    protected fun <T> collectFlow(
        flow: Flow<T>,
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        collector: suspend (T) -> Unit
    ): Job {
        return viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(minActiveState) {
                flow.collect(collector)
            }
        }
    }
}
