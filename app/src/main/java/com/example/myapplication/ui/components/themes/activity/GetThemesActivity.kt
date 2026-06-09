package com.example.myapplication.ui.components.themes.activity

import android.os.Build
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityGetThemeBinding
import com.example.myapplication.domain.layer.ThemeMessModel
import com.example.myapplication.ui.components.themes.adapter.ChooseThemePagerAdapter
import com.example.myapplication.ui.components.themes.fragment.UnlockThemeFragment
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.ZoomOutPageTransformer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GetThemesActivity :
    BaseActivity<ActivityGetThemeBinding>(ActivityGetThemeBinding::inflate) {

    private val chooseThemePagerAdapter = ChooseThemePagerAdapter()
    private var themeList: List<ThemeMessModel> = emptyList()
    private var selectedPosition = 0
    private var downloadingPosition: Int? = null

    override fun initView() {
        binding.toolbarGetTheme.btnBack.setOnClickListener { onBack() }
        binding.toolbarGetTheme.btnAction.visibility = View.GONE
        binding.toolbarGetTheme.btnSelect.visibility = View.GONE

        setupViewPager()
        setupDownloadAction()
    }

    override fun initData() {
        binding.toolbarGetTheme.tvTitle.text =
            intent.getStringExtra(Constant.EXTRA_THEME_CATEGORY_NAME) ?: getString(R.string.txt_themes)

        themeList = readThemeList()
        selectedPosition = intent.getIntExtra(Constant.EXTRA_THEME_SELECTED_POSITION, 0)

        if (themeList.isEmpty()) {
            updateNoDataState(isEmpty = true)
            return
        }

        updateNoDataState(isEmpty = false)
        chooseThemePagerAdapter.setData(themeList)
        chooseThemePagerAdapter.setDownloadedThemeKeys(spManager.getDownloadedThemeKeys().toSet())
        binding.vpChooseTheme.setCurrentItem(selectedPosition.coerceIn(themeList.indices), false)
    }

    private fun setupViewPager() {
        val horizontalPeek = resources.getDimensionPixelOffset(R.dimen.size32)
        binding.vpChooseTheme.apply {
            adapter = chooseThemePagerAdapter
            offscreenPageLimit = 3
            clipToPadding = false
            clipChildren = false
            setPadding(horizontalPeek, 0, horizontalPeek, 0)
            setPageTransformer(
                ZoomOutPageTransformer(resources.getDimensionPixelOffset(R.dimen.size24))
            )
            (getChildAt(0) as? RecyclerView)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun setupDownloadAction() {
        chooseThemePagerAdapter.onDownloadClick = { theme, position ->
            val isThemeDownloaded = spManager.isThemeDownloaded(theme)
            val isDownloading = downloadingPosition != null

            if (isThemeDownloaded) {
                showUnlockThemeFragment(theme)
            } else if (!isDownloading) {
                downloadingPosition = position
                chooseThemePagerAdapter.showLoading(position)
                binding.root.postDelayed({
                    spManager.setThemeDownloaded(theme, true)
                    downloadingPosition = null
                    chooseThemePagerAdapter.hideLoading(position)
                    chooseThemePagerAdapter.setDownloadedThemeKeys(spManager.getDownloadedThemeKeys().toSet())
                }, DOWNLOAD_DELAY_MS)
            }
        }
    }

    override fun onBack() {
        val unlockFragment =
            supportFragmentManager.findFragmentByTag(UNLOCK_THEME_TAG) as? UnlockThemeFragment
        if (unlockFragment != null && unlockFragment.isAdded) {
            hideUnlockThemeFragment(unlockFragment)
            return
        }
        super.onBack()
    }

    private fun updateNoDataState(isEmpty: Boolean) {
        binding.layoutNoData.root.isVisible = isEmpty
        binding.layoutNoData.prLoading.isVisible = false
        binding.layoutNoData.llNoData.isVisible = isEmpty
        binding.layoutNoData.tvBodyNoData.text = getString(R.string.txt_no_theme_data_available)
        binding.vpChooseTheme.isVisible = !isEmpty
    }

    private fun readThemeList(): ArrayList<ThemeMessModel> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(
                Constant.EXTRA_THEME_LIST,
                ThemeMessModel::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra(Constant.EXTRA_THEME_LIST)
        } ?: arrayListOf()
    }

    private fun showUnlockThemeFragment(theme: ThemeMessModel) {
        binding.frUnlock.isVisible = true
        val fragment = UnlockThemeFragment.newInstance(theme).apply {
            onUnlockAllClick = { selectedTheme ->
                spManager.saveCurrentTheme(selectedTheme)
                showToast(getString(R.string.txt_theme_applied))
                startActivityNewTask(MainActivity::class.java)
            }
        }
        replaceFragment(
            containerId = R.id.frUnlock,
            fragment = fragment,
            tag = UNLOCK_THEME_TAG
        )
    }

    private fun hideUnlockThemeFragment(fragment: UnlockThemeFragment) {
        binding.frUnlock.isVisible = false
        removeFragment(fragment)
    }

    companion object {
        private const val DOWNLOAD_DELAY_MS = 2000L
        private const val UNLOCK_THEME_TAG = "UnlockThemeFragment"
    }
}