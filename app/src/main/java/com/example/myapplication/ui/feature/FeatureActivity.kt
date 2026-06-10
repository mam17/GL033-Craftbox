package com.example.myapplication.ui.feature

import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityFeatureBinding
import com.example.myapplication.domain.layer.FeatureModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeatureActivity : BaseActivity<ActivityFeatureBinding>(ActivityFeatureBinding::inflate) {
    private var mFeatureAdapter = FeatureAdapter()

    override fun initView() {
        binding.rcvFeature.adapter = mFeatureAdapter
        mFeatureAdapter.onItemSelected = { isSelected ->
            binding.btnNext.isSelected = isSelected
        }
        binding.btnNext.setOnClickListener {
            if (binding.btnNext.isSelected) {
//                showInterFeature {
//                    startActivityWithAnimation(ThemeStart2Activity::class.java)
//                    finish()
//                }
            }
        }
    }

    override fun initData() {
        val listFeature = listOf<FeatureModel>(
            FeatureModel(R.drawable.img_feature1, R.string.txt_background),
            FeatureModel(R.drawable.img_feature2, R.string.txt_font_size),
            FeatureModel(R.drawable.img_feature3, R.string.txt_bubble),
        )
        mFeatureAdapter.setData(listFeature)
    }

}