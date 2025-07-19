package com.geinzz.geinzwork.DiffUtilClass

import androidx.recyclerview.widget.DiffUtil
import com.geinzz.geinzwork.model.dataclassHistorial

class DiffUtilHistorial(
    private val oldList: List<dataclassHistorial>,
    private val newList: List<dataclassHistorial>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].idPedido == newList[newItemPosition].idPedido }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}