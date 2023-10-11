package com.ihsan.memorieswithimagevideo.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ihsan.memorieswithimagevideo.fragments.MemoriesFragment

class ViewPagerAdapter(manager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(manager, lifecycle) {

    companion object {
        val fragmentList = listOf(
            MemoriesFragment(),
            //ImageMemoryFragment()
        )
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    /*    fun addBundle(
            fragment: Fragment,
            imageUris: MutableList<Uri>,
            value: Int
        ): Fragment {
            val imagesString=imageUris.map {
                it.toString()
            }
            val bundle = Bundle()
            bundle.putStringArrayList("imageUris", imagesString as ArrayList<String>)
            bundle.putInt("index", value)
            fragment.arguments = bundle
            return fragment
        }*/

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}