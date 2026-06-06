package com.wo.clipnote.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

// 继承自 Android 底层的 Service 类，相当于在后台跑一个独立线程
class OverlayService : Service() {

    // 声明一个窗口大管家，专门用来把东西画在屏幕最顶层
    private lateinit var windowManager: WindowManager

    // 生命周期钩子：服务第一次启动时调用
    override fun onCreate() {
        super.onCreate()

        // 1. 获取系统的窗口大管家服务
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 2. 核心黑魔法：配置悬浮窗的“物理属性”
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, // 宽度：根据里面的内容自适应
            WindowManager.LayoutParams.WRAP_CONTENT, // 高度：根据里面的内容自适应
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // 权限极高的类型：盖在所有 App 之上
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // 关键：不抢占焦点，这样你点悬浮球时，依然能滑动底层的抖音或网页
            PixelFormat.TRANSLUCENT // 允许背景透明
        ).apply {
            // 设置坐标系起点在屏幕左上角
            gravity = Gravity.TOP or Gravity.START
            x = 0   // 初始横坐标
            y = 500 // 初始纵坐标
        }

        // 3. 启动我们为 Compose 准备的“伪造环境”
        val lifecycleOwner = MyLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        // 4. 创建 Compose 视图引擎
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            val viewModelStore = ViewModelStore()
            setViewTreeViewModelStoreOwner(object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore get() = viewModelStore
            })

            // 5. 开始用 Compose 画真正的 UI！
            setContent {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6650a4)), // 我们的主题紫
                    contentAlignment = Alignment.Center
                ) {
                    Text("记", color = Color.White)
                }
            }
        }

        // 6. 正式把视图塞进系统的窗口大管家
        windowManager.addView(composeView, params)

        // 告诉 Compose：你已经活过来了，开始渲染吧！
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        Toast.makeText(this, "悬浮后台服务已启动！", Toast.LENGTH_SHORT).show()
    }

    // 生命周期钩子：服务被销毁时调用
    override fun onDestroy() {
        super.onDestroy()
        // TODO: 在这里移除悬浮窗，防止内存泄漏（我们后面实现关闭功能时再写）
    }

    // 这是 Service 必须重写的方法，直接返回 null 即可
    override fun onBind(intent: Intent?): IBinder? = null
}

// ==============================================================
// --- 以下是为 Compose 伪造的生命周期环境，放在文件最底部即可 ---
// ==============================================================
class MyLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }

    fun performRestore(savedState: Bundle?) {
        savedStateRegistryController.performRestore(savedState)
    }
}