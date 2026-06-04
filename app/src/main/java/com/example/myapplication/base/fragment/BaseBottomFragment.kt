package com.example.myapplication.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.myapplication.base.activity.BaseActivity
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.let

abstract class BaseBottomFragment<VB : ViewBinding> : BottomSheetDialogFragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!
    var onClick: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = provideViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.isClickable = true
        initView()
        initData()
        initObserver()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ================== BottomSheet control ==================

    /**
     * Set chiều cao bottom sheet theo tỉ lệ màn hình (0f..1f).
     * @param ratio 1f = full, 0.5f = nửa màn hình, ...
     */
    fun setBottomSheetHeight(ratio: Float = 1f) {
        dialog?.setOnShowListener { d ->
            val bottomSheet =
                (d as? BottomSheetDialog)?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                val layoutParams = it.layoutParams
                val screenHeight = resources.displayMetrics.heightPixels
                val targetHeight = (screenHeight * ratio).toInt()

                layoutParams.height = targetHeight
                it.layoutParams = layoutParams

                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.peekHeight = targetHeight
            }
        }
    }

    /** ========== Forward các hàm hỗ trợ từ BaseActivity ========== */

    fun addFragment(
        id: Int,
        fragment: Fragment,
        backStack: String? = null,
        tag: String? = null,
        delay: Long = 0
    ) {
        (activity as? BaseActivity<*>)?.addFragment(id, fragment, backStack, tag, delay)
    }

    fun replaceFragment(
        id: Int,
        fragment: Fragment,
        backStack: String? = null,
        tag: String? = null,
        delay: Long = 0
    ) {
        (activity as? BaseActivity<*>)?.replaceFragment(id, fragment, backStack, tag, delay)
    }

    fun hideFragment(fragment: Fragment, delay: Long = 0) {
        (activity as? BaseActivity<*>)?.hideFragment(fragment, delay)
    }

    fun removeFragment(fragment: Fragment, delay: Long = 0) {
        (activity as? BaseActivity<*>)?.removeFragment(fragment, delay)
    }

    fun showLoading(message: String? = null, delay: Long = 0) {
        (activity as? BaseActivity<*>)?.showLoading(message, delay)
    }

    fun hideLoading(delay: Long = 0) {
        (activity as? BaseActivity<*>)?.hideLoading(delay)
    }

    fun showToast(mes: String, duration: Int = Toast.LENGTH_SHORT) {
        (activity as? BaseActivity<*>)?.showToast(mes, duration)
    }

    protected fun <T : android.app.Activity> startActivityNewTask(clazz: Class<T>) {
        (activity as? BaseActivity<*>)?.startActivityNewTask(clazz)
    }

    protected fun <T : android.app.Activity> startNextActivity(
        clazz: Class<T>,
        bundle: Bundle? = null,
        isFinish: Boolean = false
    ) {
        (activity as? BaseActivity<*>)?.startNextActivity(clazz, bundle, isFinish)
    }

    /** ========== Abstract / override ========== */

    abstract fun provideViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    open fun initView() {}
    open fun initData() {}
    open fun initObserver() {}
}
