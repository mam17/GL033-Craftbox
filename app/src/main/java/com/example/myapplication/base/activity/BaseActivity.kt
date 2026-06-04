package com.example.myapplication.base.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.myapplication.R
import com.example.myapplication.ui.dialog.DialogLoading
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.SystemUtil
import javax.inject.Inject

abstract class BaseActivity<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater) -> VB
) : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    @Inject
    lateinit var spManager: SpManager

    private var dialogLoading: DialogLoading? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(SystemUtil.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = bindingInflater(layoutInflater)
        setContentView(binding.root)

        applySystemBarInsets()
//        setBaseDefault()
        setBaseStatusBar(isVisible = true, isLightIcons = true)
        setBaseHideNavigation()

        initView()
        initData()
        initObserver()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
    }

    /**
     * Mặc định: Hiển thị tràn viền, trong suốt thanh trạng thái và điều hướng.
     * Content sẽ nằm bên dưới thanh hệ thống (không bị che).
     */
    protected fun setBaseDefault() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.systemBars())
    }

    /**
     * Chế độ toàn màn hình: Ẩn cả thanh trạng thái và thanh điều hướng.
     */
    protected fun setBaseFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    /**
     * Ẩn thanh điều hướng (Navigation Bar) phía dưới.
     */
    protected fun setBaseHideNavigation() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    /**
     * Tùy chỉnh thanh trạng thái: Ẩn/Hiện và đổi màu icon (Sáng/Tối).
     */
    protected fun setBaseStatusBar(isVisible: Boolean, isLightIcons: Boolean) {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (isVisible) {
            controller.show(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        }
        controller.isAppearanceLightStatusBars = isLightIcons
    }

    /**
     * Chỉ đổi màu icon trên thanh trạng thái (Sáng/Tối).
     */
    protected fun setBaseStatusBarColor(isLightIcons: Boolean) {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = isLightIcons
    }

    /**
     * Helper để tự động áp dụng padding để không bị che bởi thanh hệ thống.
     * Thường dùng cho root view hoặc header.
     */
    protected fun applySystemBarInsets(targetView: View = binding.root) {
        ViewCompat.setOnApplyWindowInsetsListener(targetView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    abstract fun initView()
    abstract fun initData()
    open fun initObserver() {}

    fun <T : Activity> startActivityNewTask(clazz: Class<T>) {
        val intent = Intent(this, clazz)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    /**
     * Chuyển màn hình với hiệu ứng slide và truyền dữ liệu (Bundle).
     */
    fun <T : Activity> startNextActivity(
        clazz: Class<T>,
        bundle: Bundle? = null,
        isFinish: Boolean = false
    ) {
        val intent = Intent(this, clazz)
        bundle?.let { intent.putExtras(it) }
        startActivity(intent)

        val enterAnim = R.anim.slide_in_right
        val exitAnim = R.anim.slide_out_left

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                enterAnim,
                exitAnim
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(enterAnim, exitAnim)
        }

        if (isFinish) finish()
    }

    open fun onBack() {
        finish()
        val enterAnim = R.anim.slide_in_left
        val exitAnim = R.anim.slide_out_right

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                enterAnim,
                exitAnim
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(enterAnim, exitAnim)
        }
    }

    fun replaceFragment(
        containerId: Int,
        fragment: Fragment,
        backStack: String? = null,
        tag: String? = null,
        delay: Long = 0
    ) {
        Handler(Looper.getMainLooper()).postDelayed({
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            transaction.replace(containerId, fragment, tag)
            backStack?.let { transaction.addToBackStack(it) }
            transaction.commit()
        }, delay)
    }

    fun addFragment(
        containerId: Int,
        fragment: Fragment,
        backStack: String? = null,
        tag: String? = null,
        delay: Long = 0
    ) {
        Handler(Looper.getMainLooper()).postDelayed({
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            transaction.add(containerId, fragment, tag)
            backStack?.let { transaction.addToBackStack(it) }
            transaction.commit()
        }, delay)
    }

    fun hideFragment(fragment: Fragment, delay: Long = 0) {
        Handler(Looper.getMainLooper()).postDelayed({
            supportFragmentManager.beginTransaction().hide(fragment).commit()
        }, delay)
    }

    fun removeFragment(fragment: Fragment, delay: Long = 0) {
        Handler(Looper.getMainLooper()).postDelayed({
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }, delay)
    }

    open fun showLoading(message: String? = null, delay: Long = 0) {
        if (isFinishing || isDestroyed) return
        Handler(Looper.getMainLooper()).postDelayed({
            dialogLoading?.dismiss()
            dialogLoading = DialogLoading(this, message ?: getString(R.string.txt_loading))
            dialogLoading?.show()
        }, delay)
    }

    open fun hideLoading(delay: Long = 0) {
        if (isFinishing || isDestroyed) return
        Handler(Looper.getMainLooper()).postDelayed({
            dialogLoading?.dismiss()
            dialogLoading = null
        }, delay)
    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}