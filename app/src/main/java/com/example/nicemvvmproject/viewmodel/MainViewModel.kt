package com.example.nicemvvmproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.nicemvvmproject.base.BaseViewModel
import com.example.nicemvvmproject.bean.User
import com.example.nicemvvmproject.repository.MessageRepository
import com.example.nicemvvmproject.room.RoomHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(private val repository: MessageRepository = MessageRepository()) :
    BaseViewModel() {
    //flow 形式demo
    private val _userFlow = MutableStateFlow<User?>(null)
    val userFlow: StateFlow<User?> = _userFlow
    fun loadDataFromFlow() {
        launchRequest(onError = { e ->
            Timber.e(e, "加载数据失败")
        }) {
            val result = repository.loadWelcomeMessage()
            _userFlow.value = result
        }
    }

    //liveData 形式demo
    private val _userLiveData = MutableLiveData<User>()
    val userLiveData: LiveData<User> = _userLiveData
    fun loadDataFromLiveData() {
        launchRequest(onError = { e ->
            Timber.e(e, "加载数据失败")
        }) {
            val result = repository.loadWelcomeMessage()
            _userLiveData.value = result
        }
    }
//===================room 数据库操作示例=================================
    // 插入用户
    fun addUser(user: User) {
      viewModelScope.launch (Dispatchers.IO){
          val userData=RoomHelper.getDatabase().userDao().insert(user)
      }
    }

    private val _userRoomFlow = MutableStateFlow<List<User?>?>(null)
    val userRoomFlow: StateFlow<List<User?>?> = _userRoomFlow
    // 查询所有用户
    fun getUsers(){
        viewModelScope.launch (Dispatchers.IO){
            val userData=RoomHelper.getDatabase().userDao().getAll()
            _userRoomFlow.value=userData
        }
    }
}


