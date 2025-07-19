package com.geinzz.geinzwork.DiffUtilClass

import androidx.recyclerview.widget.DiffUtil
import com.geinzz.geinzwork.model.dataclass_seguidores_seguidos

class difultil_Seguidores_seguidos(
    private val old_list: List<dataclass_seguidores_seguidos>,
    private val new_list: List<dataclass_seguidores_seguidos>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = old_list.size

    override fun getNewListSize(): Int = new_list.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old_list[oldItemPosition].id_trabajador == new_list[newItemPosition].id_trabajador
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
       return old_list[oldItemPosition]==new_list[newItemPosition]
    }

}