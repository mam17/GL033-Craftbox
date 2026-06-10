package com.grl.sms_wa.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.grl.sms_wa.R
import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.utils.SpManager
import com.grl.sms_wa.utils.ThemeUiHelper
import com.grl.sms_wa.utils.ViewEx.applyThemeFont
import com.grl.sms_wa.utils.ViewEx.applyThemeTextColor
import com.grl.sms_wa.views.CustomBackgroundView

abstract class BaseFragment<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyBaseThemeBackground()
        applyBaseThemeFont()
        applyBaseThemeTextColor()
        initView()
        initData()
        initObserver()
    }

    abstract fun initView()
    abstract fun initData()
    open fun initObserver() {}

    override fun onResume() {
        super.onResume()
        applyBaseThemeBackground()
        applyBaseThemeFont()
        applyBaseThemeTextColor()
    }

    protected fun replaceFragment(
        containerId: Int,
        fragment: Fragment,
        backStack: String? = null,
        tag: String? = null,
        delay: Long = 0
    ) {
        (activity as? BaseActivity<*>)?.replaceFragment(
            containerId,
            fragment,
            backStack,
            tag,
            delay
        )
    }

    protected fun addFragment(
        containerId: Int,
        fragment: Fragment,
        backStack: String? = null,
        tag: String? = null,
        delay: Long = 0
    ) {
        (activity as? BaseActivity<*>)?.addFragment(containerId, fragment, backStack, tag, delay)
    }

    protected fun hideFragment(fragment: Fragment, delay: Long = 0) {
        (activity as? BaseActivity<*>)?.hideFragment(fragment, delay)
    }

    protected fun removeFragment(fragment: Fragment, delay: Long = 0) {
        (activity as? BaseActivity<*>)?.removeFragment(fragment, delay)
    }

    fun showLoading(message: String? = null, delay: Long = 0) {
        (activity as? BaseActivity<*>)?.showLoading(message, delay)
    }

    fun hideLoading(delay: Long = 0) {
        (activity as? BaseActivity<*>)?.hideLoading(delay)
    }

    fun showToast(message: String, duration: Int = android.widget.Toast.LENGTH_SHORT) {
        (activity as? BaseActivity<*>)?.showToast(message, duration)
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

    private fun applyBaseThemeBackground() {
        val context = context ?: return
        val backgroundView = binding.root.findCustomBackgroundView() ?: return
        ThemeUiHelper.bindBackground(backgroundView, SpManager.get(context).getCurrentTheme())
    }

    private fun applyBaseThemeFont() {
        val context = context ?: return
        binding.root.applyThemeFont(SpManager.get(context).getCurrentTheme()?.font)
    }

    private fun applyBaseThemeTextColor() {
        val context = context ?: return
        binding.root.applyThemeTextColor(SpManager.get(context).getCurrentTheme()?.colMain)
    }

    private fun View.findCustomBackgroundView(): CustomBackgroundView? {
        if (id == R.id.backgroundTheme && this is CustomBackgroundView) {
            return this
        }
        if (this !is ViewGroup) return null
        repeat(childCount) { index ->
            getChildAt(index).findCustomBackgroundView()?.let { return it }
        }
        return null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
