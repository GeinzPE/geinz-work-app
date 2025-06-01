package com.example.geinzwork.adapterViewholder

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.geinzwork.fragmentos.guardados.guardado_noticia
import com.example.geinzwork.fragmentos.guardados.guardado_trabajador

class adapter_viewpager_guarda(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = listOf(
        guardado_trabajador(),
        guardado_noticia()



    )


    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

}