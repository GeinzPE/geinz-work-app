package com.example.geinzwork.DiffUtilClass

import androidx.recyclerview.widget.DiffUtil
import com.example.geinzwork.dataclass.dataClasSeguirTrabajdores_info
import com.example.geinzwork.dataclass.dataclasCaterogirasFiltrado

class difiutl_adapter_seguir_trabajadores(
    private val oldList: List<dataClasSeguirTrabajdores_info>,
    private val newList: List<dataClasSeguirTrabajdores_info>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}