package com.example.nicemvvmproject.base

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.example.nicemvvmproject.MainActivity
import com.example.nicemvvmproject.R
import com.example.nicemvvmproject.databinding.LayoutBaseContainerBinding
import com.gyf.immersionbar.ImmersionBar
import timber.log.Timber

abstract class BaseActivity<VM : BaseViewModel, DB : ViewDataBinding> : AppCompatActivity() {

    protected lateinit var viewModel: VM
    protected lateinit var binding: DB
    private lateinit var baseHeadLayout: LayoutBaseContainerBinding
    abstract fun getLayoutId(): Int
    abstract fun getViewModelClass(): Class<VM>
    abstract fun initView()
    abstract fun initObserver()
    open fun initData() {}
    open fun getViewModelFactory(): ViewModelProvider.Factory? = null
    private var lastBackPressTime = 0L
    private val exitInterval = 2000 // 2 秒内双击退出

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            getViewModelFactory() ?: defaultViewModelProviderFactory
        ).get(getViewModelClass())

        Timber.d("%s -> onCreate", javaClass.simpleName)
        val inflater = LayoutInflater.from(this)
        if (useBaseContainer()) {
            baseHeadLayout =
                DataBindingUtil.inflate(inflater, R.layout.layout_base_container, null, false)
            setContentView(baseHeadLayout.root)
            val childBinding = DataBindingUtil.inflate<DB>(
                inflater,
                getLayoutId(),
                null,
                false
            )
            baseHeadLayout.baseContent.addView(childBinding.root)
            binding = childBinding
        } else {
            binding = DataBindingUtil.setContentView(this, getLayoutId())
        }

        // 处理状态栏高度
        if (autoHandleStatusBarPadding()) {
            handleStatusBarPadding()
        }
        onNewBackFinish()
        if (useImmersionBar()) {
            initImmersionBar()
        }
        initView()
        initObserver()
        initData()
        if (useBaseToolbar()) {
            setupBaseHeader()
        }
        onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        Timber.d("%s -> onStart", javaClass.simpleName)
        onActivityStarted()
    }

    override fun onResume() {
        super.onResume()
        Timber.d("%s -> onResume", javaClass.simpleName)
        onActivityResumed()
    }

    override fun onPause() {
        super.onPause()
        Timber.d("%s -> onPause", javaClass.simpleName)
        onActivityPaused()
    }

    override fun onStop() {
        super.onStop()
        Timber.d("%s -> onStop", javaClass.simpleName)
        onActivityStopped()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("%s -> onDestroy", javaClass.simpleName)
        onActivityDestroyed()
    }

    /** 生命周期钩子方法：子类可选择覆写 */
    open fun onActivityCreated(savedInstanceState: Bundle?) {}
    open fun onActivityStarted() {}
    open fun onActivityResumed() {}
    open fun onActivityPaused() {}
    open fun onActivityStopped() {}
    open fun onActivityDestroyed() {}

    // 是否启用双击退出
    /**
     * Toolbar 统一封装：可选启用；默认查找布局中的 `MaterialToolbar` (id: toolbar) 并完成标题、返回、右侧按钮配置
     */
    open fun useBaseToolbar(): Boolean = true

    /** 是否使用 BaseActivity 的公共容器布局（含通用 Toolbar），关闭后使用子 Activity 自己的完整布局 */
    open fun useBaseContainer(): Boolean = true

    /** 是否启用沉浸式状态栏（ImmersionBar） */
    open fun useImmersionBar(): Boolean = false

    /** 状态栏深色字体（根据页面主题覆写） */
    open fun isStatusBarDarkFont(): Boolean = false

    /** 是否自动处理状态栏高度，确保内容不显示到状态栏上 */
    open fun autoHandleStatusBarPadding(): Boolean = true

    /** 返回是否显示左侧返回键，默认显示 */
    open fun showBackButton(): Boolean = true

    /** 右侧图标资源（优先生效，若为 null 则尝试使用 [rightActionText]） */
    open fun rightActionIconRes(): Int? = null

    /** 运行时控制返回键可见性（普通头部/Toolbar 兼容） */
    fun setBackButtonVisible(visible: Boolean) {
        if (useBaseContainer()) {
            baseHeadLayout.baseBack.apply {
                visibility = if (visible) View.VISIBLE else View.GONE
                if (visible) setOnClickListener { finish() } else setOnClickListener(
                    null
                )
            }
        }
    }

    /** 动态替换返回键图标（普通头部/Toolbar 兼容） */
    fun setNavigationIcon(@DrawableRes iconRes: Int) {
        if (useBaseContainer()) {
            baseHeadLayout.baseBack.apply {
                setImageResource(iconRes)
                visibility = View.VISIBLE
                setOnClickListener { finish() }
            }
        }
    }

    private fun setupBaseHeader() {
        // 普通头部
        if (useBaseContainer()) {
            if (showBackButton()) {
                baseHeadLayout.baseBack.visibility = View.VISIBLE
                baseHeadLayout.baseBack.setOnClickListener { finish() }
            } else {
                baseHeadLayout.baseBack.visibility = View.GONE
                baseHeadLayout.baseBack.setOnClickListener(null)
            }
            val rIcon = rightActionIconRes()
            if (rIcon != null) {
                baseHeadLayout.baseRightIcon.setImageResource(rIcon)
            }
            baseHeadLayout.baseRightIcon.visibility = View.VISIBLE
            baseHeadLayout.baseRightIcon.setOnClickListener { onRightActionClick() }
        }
    }

    /** 设置标题文字 */
    fun setTitleText(title: String) {
        if (useBaseContainer()) {
            baseHeadLayout.baseTitle.text = title
        }
    }

    /** 设置返回按钮图片 */
    fun setBackIcon(resId: Int) {
        if (useBaseContainer()) {
            baseHeadLayout.baseBack.setImageResource(resId)
        }
    }

    /** 设置右侧操作按钮 */
    fun setRightAction(resId: Int, onClick: (() -> Unit)?) {
        if (useBaseContainer()) {
            baseHeadLayout.baseRightIcon.apply {
                setImageResource(resId)
                visibility = View.VISIBLE
                setOnClickListener { onClick?.invoke() }
            }
        }
    }

    /** 隐藏标题栏 */
    fun hideTitleBar() {
        if (useBaseContainer()) {
            baseHeadLayout.baseHeader.visibility = View.GONE
        }
    }

    /**
     * 处理状态栏高度，确保内容不会显示到状态栏上
     */
    private fun handleStatusBarPadding() {
        if (useBaseContainer()) {
            // 使用WindowInsetsCompat获取状态栏高度
            ViewCompat.setOnApplyWindowInsetsListener(baseHeadLayout.root) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val statusBarHeight = insets.top
                // 为头部添加状态栏高度的padding
                val layoutParams = baseHeadLayout.toolBar.layoutParams
                layoutParams.height = statusBarHeight
                baseHeadLayout.toolBar.layoutParams = layoutParams
//                baseHeadLayout.toolBar.height=

                // 返回WindowInsetsCompat.CONSUMED表示已处理
                WindowInsetsCompat.CONSUMED
            }
        } else {
            // 如果不使用baseContainer，为根布局添加状态栏高度
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val statusBarHeight = insets.top

                // 为根布局添加状态栏高度的padding
                binding.root.setPadding(
                    binding.root.paddingLeft,
                    statusBarHeight,
                    binding.root.paddingRight,
                    binding.root.paddingBottom
                )

                WindowInsetsCompat.CONSUMED
            }
        }
    }

    /** 初始化沉浸式：使用 baseHeader 作为 titleBar，自动处理状态栏高度与内边距 */
    protected open fun initImmersionBar() {
        ImmersionBar.with(this)
            .transparentStatusBar()
//            .statusBarDarkFont(isStatusBarDarkFont())
            .statusBarDarkFont(true)
    }

    // 监听返回键最新写法
    fun onNewBackFinish() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (this@BaseActivity is MainActivity) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime > exitInterval) {
                        lastBackPressTime = currentTime
                        Toast.makeText(this@BaseActivity, "再按一次退出应用", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        finishAffinity()
                    }
                } else {
                    // 取消拦截，交给系统默认处理
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    /**
     * 公共跳转方法
     * @param targetActivity 目标 Activity 类
     * @param bundle 可选参数
     * @param finishCurrent 是否关闭当前页面
     * @param enterAnim 进入动画
     * @param exitAnim 退出动画
     */
    protected fun navigateTo(
        targetActivity: Class<out Activity>,
        bundle: Bundle? = null,
        finishCurrent: Boolean = false,
        enterAnim: Int? = null,
        exitAnim: Int? = null
    ) {
        val intent = Intent(this, targetActivity)
        bundle?.let { intent.putExtras(it) }

        if (enterAnim != null && exitAnim != null) {
            val options = ActivityOptions.makeCustomAnimation(this, enterAnim, exitAnim)
            startActivity(intent, options.toBundle())
        } else {
            startActivity(intent)
        }

        if (finishCurrent) {
            finish()
        }
    }

    /** 可选：无参的右侧点击回调，普通头部使用 */
    open fun onRightActionClick() {}

}
