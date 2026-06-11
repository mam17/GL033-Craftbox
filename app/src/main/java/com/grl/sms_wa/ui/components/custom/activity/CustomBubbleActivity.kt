package com.grl.sms_wa.ui.components.custom.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.grl.sms_wa.databinding.ActivityCustomBubbleBinding

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.grl.sms_wa.R
import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.ui.components.custom.viewmodel.CustomBubbleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomBubbleActivity : BaseActivity<ActivityCustomBubbleBinding>(ActivityCustomBubbleBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_custom_bubble)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private val viewMode: CustomBubbleViewModel by viewModels()

    override fun initView() {

    }

    override fun initData() {

    }
}