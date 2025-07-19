package com.geinzz.geinzwork.DiffUtilClass

import androidx.recyclerview.widget.DiffUtil
import com.geinzz.geinzwork.model.dataclasEncontrados

class difiultilsFiltrado (
    private val oldList: List<dataclasEncontrados>,
    private val newList: List<dataclasEncontrados>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].idPropietario == newList[newItemPosition].idPropietario
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}