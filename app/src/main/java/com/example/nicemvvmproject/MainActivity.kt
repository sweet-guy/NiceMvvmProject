package com.example.nicemvvmproject

import android.graphics.Color
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.nicemvvmproject.base.BaseActivity
import com.example.nicemvvmproject.bean.TabItem
import com.example.nicemvvmproject.databinding.ActivityMainBinding
import com.example.nicemvvmproject.ui.dashboard.DashboardFragment
import com.example.nicemvvmproject.ui.home.HomeFragment
import com.example.nicemvvmproject.ui.notifications.NotificationsFragment
import com.example.nicemvvmproject.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {
    private val fragmentMap = mutableMapOf<Int, Fragment>()
    private var currentTabId: Int = 0
    val tabs = mutableListOf<TabItem>()

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun getViewModelClass(): Class<MainViewModel> = MainViewModel::class.java

    // 启用自动状态栏高度处理
    override fun autoHandleStatusBarPadding(): Boolean = true

    // 启用沉浸式状态栏
    override fun useImmersionBar(): Boolean = true

    // 状态栏深色字体
    override fun isStatusBarDarkFont(): Boolean = true

    override fun useBaseContainer(): Boolean = true

    override fun showBackButton(): Boolean {
        return true
    }

    override fun onRightActionClick() {
        Toast.makeText(this, "点击了右侧操作", Toast.LENGTH_SHORT).show()
    }

    override fun initView() {
        // 设置标题
        setTitleText("MVVM示例")
        initTabItems()
        changeTab(R.id.navigation_home)
        binding.hiddenBottomNav.setOnItemSelectedListener { item ->
            changeTab(item.itemId)
            true
        }
    }

    override fun initObserver() {
        //使用flow 形式更新ui
        lifecycleScope.launch {
            viewModel.userFlow.collect { user ->
                user?.let {
                    //更新Ui
                }
            }
        }
        viewModel.userLiveData.observe(this) { user ->
            user?.let {
                //更新Ui
            }
        }
        //lifecycleScope  launch启动协程不接收结果
        lifecycleScope.launch {
            delay(3000)
            lifecycleScope.launch(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "3秒后显示", Toast.LENGTH_SHORT).show()
            }
        }
        //lifecycleScope  async启动协程 接收结果
        val data=lifecycleScope.async {
                delay(3000)
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "3秒后显示", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun initData() {
        // 初始化时加载数据
        viewModel.loadDataFromFlow()
        viewModel.loadDataFromLiveData()
    }

    private fun changeTab(tabId: Int) {
        switchFragment(tabId)
        highLightTab(tabId)
    }

    private fun initTabItems() {
        tabs.clear()
        tabs.addAll(
            listOf(
                TabItem(
                    R.id.navigation_home,
                    "首页",
                    R.drawable.ic_home_black_24dp,
                    HomeFragment()
                ),
                TabItem(
                    R.id.navigation_dashboard,
                    "我的",
                    R.drawable.ic_dashboard_black_24dp,
                    DashboardFragment()
                ),
                TabItem(
                    R.id.navigation_notifications,
                    "通知",
                    R.drawable.ic_notifications_black_24dp,
                    NotificationsFragment()
                )
            )
        )
        setupCustomBottomNav(tabs)
    }

    private fun setupCustomBottomNav(tabList: List<TabItem>) {
        val navLayout = binding.customBottomNav
        navLayout.removeAllViews()
        tabList.forEach { tab ->
            val tabView = LayoutInflater.from(this).inflate(R.layout.item_tab, navLayout, false)
            tabView.findViewById<ImageView>(R.id.icon).setImageResource(tab.iconRes)
            tabView.findViewById<TextView>(R.id.title).text = tab.title
            tabView.setOnClickListener {
                binding.hiddenBottomNav.selectedItemId = tab.id
            }
            navLayout.addView(tabView)
        }
    }

    private fun switchFragment(tabId: Int) {
        if (currentTabId == tabId) return
        val transaction = supportFragmentManager.beginTransaction()
        fragmentMap[currentTabId]?.let { transaction.hide(it) }
        val targetFragment = fragmentMap.getOrPut(tabId) {
            tabs.find { it.id == tabId }?.fragment ?: Fragment()
        }
        if (targetFragment.isAdded) {
            transaction.show(targetFragment)
        } else {
            transaction.add(R.id.fragmentContainer, targetFragment)
        }
        transaction.commit()
        currentTabId = tabId
    }

    private fun highLightTab(tabId: Int) {
        val navLayout = binding.customBottomNav
        for (i in 0 until navLayout.childCount) {
            val child = navLayout.getChildAt(i)
            val titleView = child.findViewById<TextView>(R.id.title)
            val config = tabs.get(i)
            if (config.id == tabId) {
                titleView.setTextColor(Color.BLUE)
            } else {
                titleView.setTextColor(Color.GRAY)
            }
        }
    }
}