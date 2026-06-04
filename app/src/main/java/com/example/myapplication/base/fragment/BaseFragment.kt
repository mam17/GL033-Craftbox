package com.example.myapplication.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.myapplication.base.activity.BaseActivity

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
        initView()
        initData()
        initObserver()
    }

    abstract fun initView()
    abstract fun initData()
    open fun initObserver() {}

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}