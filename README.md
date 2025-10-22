# NiceMvvmProject 框架使用文档

## 📋 目录

- [项目概述](#项目概述)
- [架构设计](#架构设计)
- [快速开始](#快速开始)
- [基础组件](#基础组件)
- [网络层](#网络层)
- [协程管理](#协程管理)
- [数据库操作](#数据库操作)
- [常见问题](#常见问题)

---

## 🎯 项目概述

NiceMvvmProject 是一个基于 Android MVVM 架构的现代化开发框架，集成了以下核心功能：

- **MVVM 架构模式**：完整的 ViewModel + LiveData/StateFlow 数据绑定
- **协程支持**：完整的协程生命周期管理和异常处理
- **网络请求**：基于 Retrofit + OkHttp 的网络层封装
- **数据加密**：RSA + AES 混合加密传输
- **本地存储**：Room 数据库 + MMKV 轻量存储
- **UI 组件**：统一的 BaseActivity/BaseFragment 封装
- **状态栏处理**：沉浸式状态栏和自动适配

---

## 🏗️ 架构设计

### 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                             │
├─────────────────────────────────────────────────────────────┤
│  BaseActivity  │  BaseFragment  │  Custom Views            │
├─────────────────────────────────────────────────────────────┤
│                    ViewModel Layer                          │
├─────────────────────────────────────────────────────────────┤
│  BaseViewModel  │  Custom ViewModels  │  StateFlow/LiveData │
├─────────────────────────────────────────────────────────────┤
│                   Repository Layer                          │
├─────────────────────────────────────────────────────────────┤
│  BaseRepository  │  Custom Repositories  │  Data Sources    │
├─────────────────────────────────────────────────────────────┤
│                    Data Layer                               │
├─────────────────────────────────────────────────────────────┤
│  Network (Retrofit)  │  Database (Room)  │  Storage (MMKV)  │
└─────────────────────────────────────────────────────────────┘
```

### 核心组件

| 组件 | 功能 | 位置 |
|------|------|------|
| BaseActivity | Activity 基类，提供统一的生命周期管理 | `base/BaseActivity.kt` |
| BaseFragment | Fragment 基类，提供统一的视图管理 | `base/BaseFragment.kt` |
| BaseViewModel | ViewModel 基类，提供协程和状态管理 | `base/BaseViewModel.kt` |
| BaseRepository | Repository 基类，提供网络服务 | `base/BaseRepository.kt` |
| BaseDao | Room DAO 基类，提供基础 CRUD 操作 | `base/BaseDao.kt` |
| RetrofitClient | 网络客户端，统一网络配置 | `network/RetrofitClient.kt` |
| ApiService | API 接口定义 | `network/ApiService.kt` |

---

## 🚀 快速开始

### 1. 环境要求

- Android Studio Arctic Fox 或更高版本
- Kotlin 1.8.0 或更高版本
- Android API 24+ (Android 7.0)
- Gradle 8.0+

### 2. 依赖配置

项目已配置好所有必要的依赖，包括：

```kotlin
// 核心依赖
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5"
implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.8.5"
implementation "androidx.room:room-runtime:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"

// 网络依赖
implementation "com.squareup.retrofit2:retrofit:2.11.0"
implementation "com.squareup.retrofit2:converter-gson:2.11.0"
implementation "com.squareup.okhttp3:okhttp:4.12.0"
implementation "com.squareup.okhttp3:logging-interceptor:4.12.0"

// 协程依赖
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"

// 其他工具
implementation "com.tencent:mmkv-static:1.3.2"
implementation "com.jakewharton.timber:timber:5.0.1"
```

### 3. 初始化配置

在 `Application` 类中进行初始化：

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化日志
        if (BuildConfig.LOG_ENABLE) {
            Timber.plant(Timber.DebugTree())
        }
        
        // 初始化数据库
        RoomHelper.init(this)
        
        // 配置网络请求参数
        RetrofitClient.setRequestParamsProvider(object : RequestParamsProvider {
            override fun provideHeaders(): Map<String, String> {
                return mapOf(
                    "X-App-Platform" to "android",
                    "Authorization" to "Bearer your_token"
                )
            }
            
            override fun provideQueryParams(): Map<String, String> {
                return mapOf(
                    "appVersion" to BuildConfig.VERSION_NAME,
                    "timestamp" to System.currentTimeMillis().toString()
                )
            }
        })
    }
}
```

---

## 🧩 基础组件

### BaseActivity

`BaseActivity` 是所有 Activity 的基类，提供统一的生命周期管理和 UI 功能。

#### 核心功能

- **生命周期管理**：自动处理 onCreate、onStart 等生命周期
- **数据绑定**：自动初始化 ViewDataBinding
- **状态栏处理**：支持沉浸式状态栏和自动适配
- **返回键处理**：统一的返回键逻辑
- **协程支持**：生命周期感知的协程管理

#### 使用方法

```kotlin
class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {
    
    override fun getLayoutId(): Int = R.layout.activity_main
    
    override fun getViewModelClass(): Class<MainViewModel> = MainViewModel::class.java
    
    override fun initView() {
        // 初始化视图
        setupToolbar()
        setupRecyclerView()
    }
    
    override fun initObserver() {
        // 观察 ViewModel 数据
        viewModel.userData.observe(this) { user ->
            updateUI(user)
        }
    }
    
    override fun initData() {
        // 初始化数据
        viewModel.loadUserData()
    }
    
    // 可选配置
    override fun useImmersionBar(): Boolean = true
    override fun showBackButton(): Boolean = true
    override fun autoHandleStatusBarPadding(): Boolean = true
}
```

#### 配置选项

| 方法 | 默认值 | 说明 |
|------|--------|------|
| `useBaseContainer()` | `true` | 是否使用基础容器布局 |
| `useBaseToolbar()` | `true` | 是否显示基础工具栏 |
| `useImmersionBar()` | `false` | 是否启用沉浸式状态栏 |
| `showBackButton()` | `true` | 是否显示返回按钮 |
| `autoHandleStatusBarPadding()` | `true` | 是否自动处理状态栏高度 |

### BaseFragment

`BaseFragment` 是所有 Fragment 的基类，提供统一的视图管理。

#### 使用方法

```kotlin
class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {
    
    override fun getLayoutId(): Int = R.layout.fragment_home
    
    override fun getViewModelClass(): Class<HomeViewModel> = HomeViewModel::class.java
    
    override fun initView() {
        // 初始化视图
        setupRecyclerView()
    }
    
    override fun initObserver() {
        // 观察数据变化
        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }
    
    override fun initData() {
        // 加载数据
        viewModel.loadItems()
    }
}
```

### BaseViewModel

`BaseViewModel` 是所有 ViewModel 的基类，提供协程管理和状态处理。

#### 核心功能

- **协程管理**：统一的协程启动和异常处理
- **状态管理**：Loading 状态和错误信息管理
- **线程切换**：主线程和 IO 线程切换
- **生命周期管理**：自动取消协程任务

#### 使用方法

```kotlin
class MainViewModel : BaseViewModel() {
    //livedata 示例
    private val _userData = MutableLiveData<User>()
    val userData: LiveData<User> = _userData
    //flow 示例
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items
    
    fun loadUserData() {
        launchRequest(
            onError = { error ->
                Timber.e(error, "加载用户数据失败")
                _errorMessage.value = "加载失败: ${error.message}"
            }
        ) {
            _isLoading.value = true
            try {
                val user = withIOContext {
                    repository.getUserData()
                }
                _userData.value = user
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadItems() {
        launchWithHandler(
            taskName = "loadItems",
            onError = { error ->
                Timber.e(error, "加载列表数据失败")
            }
        ) {
            val items = withIOContext {
                repository.getItems()
            }
            _items.value = items
        }
    }
}
```

#### 协程方法

| 方法 | 说明 | 使用场景 |
|------|------|----------|
| `launchRequest()` | 基础协程启动 | 一般网络请求 |
| `launchWithHandler()` | 带异常处理的协程 | 需要详细错误处理的场景 |
| `safeDelay()` | 延迟执行 | 定时任务 |
| `withMainContext()` | 切换到主线程 | UI 更新 |
| `withIOContext()` | 切换到 IO 线程 | 网络请求、数据库操作 |

### BaseRepository

`BaseRepository` 是所有 Repository 的基类，提供网络服务。

#### 使用方法

```kotlin
class UserRepository : BaseRepository() {
    
    suspend fun getUserData(): User {
        return try {
            val response = apiService.getUserData()
            // 处理响应数据
            response.data
        } catch (e: Exception) {
            Timber.e(e, "获取用户数据失败")
            throw e
        }
    }
    
    suspend fun uploadFile(file: File, onProgress: ProgressListener): Boolean {
        return try {
            val requestBody = ProgressRequestBody(file, "image/*".toMediaTypeOrNull(), onProgress)
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val response = apiService.uploadFile(part)
            response.isSuccessful
        } catch (e: Exception) {
            Timber.e(e, "文件上传失败")
            false
        }
    }
}
```

---

## 🌐 网络层

### RetrofitClient

`RetrofitClient` 是网络请求的核心配置类。

#### 配置说明

```kotlin
object RetrofitClient {
    // 基础配置
    const val CURRENT_BASE_URL = "https://api.example.com/"
    private const val DEFAULT_TIMEOUT_SECONDS = 20L
    
    // 加密配置
    val rsaPublicKeyBase64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A..."
    
    // 客户端配置
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(CommonParamsInterceptor { requestParamsProvider })
            .addInterceptor(HybridEncryptDecryptInterceptor(rsaPublicKeyBase64))
            .addInterceptor(loggingInterceptor)
            .build()
    }
}
```

#### 拦截器说明

1. **CommonParamsInterceptor**：添加公共请求参数和请求头
2. **HybridEncryptDecryptInterceptor**：数据加密解密
3. **HttpLoggingInterceptor**：请求日志记录

### ApiService

定义网络请求接口。

```kotlin
interface ApiService {
    
    @GET("/api/user/profile")
    suspend fun getUserProfile(): ApiResponse<User>
    
    @POST("/api/user/update")
    suspend fun updateUser(@Body user: User): ApiResponse<Unit>
    
    @Multipart
    @POST("/api/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<Unit>
    
    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>
}
```

### ApiResponse

统一的响应数据格式。

```kotlin
data class ApiResponse<T>(
    val code: Int,
    val msg: String,
    val data: T
) {
    val isSuccess: Boolean get() = code == 200
}
```

---

## ⚡ 协程管理

### 协程生命周期

框架提供了完整的协程生命周期管理：

```kotlin
class MyViewModel : BaseViewModel() {
    
    fun performTask() {
        // 1. 基础协程启动
        launchRequest {
            val result = withIOContext {
                // 在 IO 线程执行耗时操作
                repository.fetchData()
            }
            
            withMainContext {
                // 在主线程更新 UI
                updateUI(result)
            }
        }
    }
    
    fun performTaskWithErrorHandling() {
        // 2. 带错误处理的协程
        launchWithHandler(
            taskName = "fetchUserData",
            onError = { error ->
                // 自定义错误处理
                handleError(error)
            }
        ) {
            val user = repository.getUser()
            _userData.value = user
        }
    }
    
    fun performDelayedTask() {
        // 3. 延迟执行
        safeDelay(3000) {
            showNotification()
        }
    }
}
```

### 线程切换

```kotlin
class MyViewModel : BaseViewModel() {
    
    fun loadData() {
        launchRequest {
            // 在 IO 线程执行网络请求
            val data = withIOContext {
                repository.fetchData()
            }
            
            // 切换到主线程更新 UI
            withMainContext {
                _data.value = data
            }
        }
    }
}
```

### 异常处理

```kotlin
class MyViewModel : BaseViewModel() {
    
    fun loadDataWithRetry() {
        launchRequest(
            onError = { error ->
                when (error) {
                    is SocketTimeoutException -> {
                        // 网络超时处理
                        showRetryDialog()
                    }
                    is HttpException -> {
                        // HTTP 错误处理
                        handleHttpError(error.code())
                    }
                    else -> {
                        // 其他错误处理
                        showErrorMessage(error.message ?: "未知错误")
                    }
                }
            }
        ) {
            val data = repository.fetchData()
            _data.value = data
        }
    }
}
```

---

## 💾 数据库操作

### Room 配置

```kotlin
@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

// DAO 接口
@Dao
interface UserDao : BaseDao<User> {
    suspend fun getUserById(id: Long): User?
    suspend fun getAllUsers(): List<User>
    suspend fun searchUsers(name: String): List<User>
}
```

### 数据实体

```kotlin
@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val name: String,
    val email: String,
    val avatar: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
```

### Repository 中的数据库操作

```kotlin
class UserRepository : BaseRepository() {
    
    suspend fun saveUser(user: User): Long {
        return withIOContext {
            RoomHelper.getDatabase().userDao().insert(user)
        }
    }
    
    suspend fun getUserById(id: Long): User? {
        return withIOContext {
            RoomHelper.getDatabase().userDao().getUserById(id)
        }
    }
    
    suspend fun getAllUsers(): List<User> {
        return withIOContext {
            RoomHelper.getDatabase().userDao().getAllUsers()
        }
    }
}
```

---

---

## ❓ 常见问题

### Q1: 如何处理网络请求失败？

A: 使用 `launchRequest` 或 `launchWithHandler` 的 `onError` 参数：

```kotlin
viewModel.loadData()
launchRequest(
    onError = { error ->
        when (error) {
            is SocketTimeoutException -> showTimeoutError()
            is HttpException -> showHttpError(error.code())
            else -> showGenericError(error.message)
        }
    }
) {
    val data = repository.fetchData()
    _data.value = data
}
```

### Q2: 如何实现下拉刷新？

A: 使用 SwipeRefreshLayout 配合 ViewModel 的刷新状态：

```kotlin
// ViewModel
private val _isRefreshing = MutableLiveData<Boolean>()
val isRefreshing: LiveData<Boolean> = _isRefreshing

fun refreshData() {
    launchRequest {
        _isRefreshing.value = true
        try {
            val data = repository.fetchData()
            _data.value = data
        } finally {
            _isRefreshing.value = false
        }
    }
}

// Activity
viewModel.isRefreshing.observe(this) { isRefreshing ->
    binding.swipeRefresh.isRefreshing = isRefreshing
}
```

### Q3: 如何处理分页加载？

A: 使用 StateFlow 和协程实现分页：

```kotlin
class MyViewModel : BaseViewModel() {
    
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items
    
    private var currentPage = 1
    private var isLoadingMore = false
    
    fun loadMoreItems() {
        if (isLoadingMore) return
        
        launchRequest {
            isLoadingMore = true
            try {
                val newItems = repository.getItems(currentPage)
                _items.value = _items.value + newItems
                currentPage++
            } finally {
                isLoadingMore = false
            }
        }
    }
}
```

### Q4: 如何实现数据缓存？

A: 在 Repository 中实现缓存策略：

```kotlin
class MyRepository : BaseRepository() {
    
    private val cache = mutableMapOf<String, Any>()
    
    suspend fun getData(forceRefresh: Boolean = false): Data {
        val cacheKey = "data_key"
        
        if (!forceRefresh && cache.containsKey(cacheKey)) {
            return cache[cacheKey] as Data
        }
        
        val data = apiService.getData()
        cache[cacheKey] = data
        return data
    }
}
```

### Q5: 如何处理文件上传进度？

A: 使用 ProgressRequestBody 监听上传进度：

```kotlin
class MyRepository : BaseRepository() {
    
    suspend fun uploadFile(file: File, onProgress: ProgressListener): Boolean {
        val requestBody = ProgressRequestBody(
            file, 
            "image/*".toMediaTypeOrNull(), 
            onProgress
        )
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val response = apiService.uploadFile(part)
        return response.isSuccessful
    }
}

// 使用
repository.uploadFile(file) { bytesWritten, contentLength, done ->
    val progress = (bytesWritten * 100 / contentLength).toInt()
    updateProgress(progress)
}
```

---


NiceMvvmProject 框架提供了完整的 MVVM 开发解决方案，包括：

- **统一的架构模式**：BaseActivity、BaseFragment、BaseViewModel
- **完善的网络层**：Retrofit + OkHttp + 加密传输
- **强大的协程支持**：生命周期管理 + 异常处理
- **本地数据存储**：Room 数据库 + MMKV 存储
- **丰富的 UI 功能**：状态栏处理 + 数据绑定

通过本文档的使用说明，您可以快速上手并构建 Android 应用。

---

## 🔗 相关链接

- [Android 官方 MVVM 指南](https://developer.android.com/jetpack/guide)
- [Kotlin 协程官方文档](https://kotlinlang.org/docs/coroutines-overview.html)
- [Retrofit 官方文档](https://square.github.io/retrofit/)
- [Room 数据库指南](https://developer.android.com/training/data-storage/room)
- [GitHub 仓库]([https://github.com/yourusername/NiceMvvmProject](https://github.com/sweet-guy/NiceMvvmProject))

---

*文档版本：v1.0*  
*最后更新：2025年10月*
