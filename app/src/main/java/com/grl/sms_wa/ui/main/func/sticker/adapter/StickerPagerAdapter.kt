package com.grl.sms_wa.ui.main.func.sticker.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.grl.sms_wa.ui.main.func.sticker.childfragment.AllStickerFragment
import com.grl.sms_wa.ui.main.func.sticker.childfragment.YourStickerFragment

class StickerPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllStickerFragment()
            else -> YourStickerFragment()
        }
    }
}
