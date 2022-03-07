package com.dev.simplified.tudoo

import androidx.recyclerview.widget.DiffUtil
import com.dev.simplified.tudoo.data.Todo

class TodoDiffUtil (private val oldList: List<Todo>, private val newList: List<Todo>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] === newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
                && oldList[oldItemPosition].title == newList[newItemPosition].title
                && oldList[oldItemPosition].description == newList[newItemPosition].description
                && oldList[oldItemPosition].priority == newList[newItemPosition].priority
                && oldList[oldItemPosition].status == newList[newItemPosition].status
                && oldList[oldItemPosition].deadlineDate == newList[newItemPosition].deadlineDate
                && oldList[oldItemPosition].deadline == newList[newItemPosition].deadline
    }

}