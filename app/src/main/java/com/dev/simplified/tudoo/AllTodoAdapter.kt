package com.dev.simplified.tudoo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dev.simplified.tudoo.data.Priority
import com.dev.simplified.tudoo.data.Todo
import kotlinx.android.synthetic.main.custom_row_layout.view.*
import java.text.SimpleDateFormat
import java.util.*

class AllTodoAdapter : RecyclerView.Adapter<AllTodoAdapter.MyViewHolder>() {

    var todoList = emptyList<Todo>()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.custom_row_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = todoList[position]
        holder.itemView.textView2.text = currentItem.title.toString()
        holder.itemView.textView3.text = currentItem.description.toString()
        holder.itemView.priority_txt.text = currentItem.priority.toString()
        holder.itemView.deadlineTxtview.text = getDeadline(currentItem.deadline)
        val i = parsePriorityToColor(currentItem.priority)
        holder.itemView.priority_btn.background.setTint(ContextCompat.getColor(holder.itemView.context,i))

        holder.itemView.rowLayout.setOnClickListener {
            val action = AllTodoFragmentDirections.actionAllTodoFragmentToUpdateFragment(currentItem,1)
            holder.itemView.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    fun setData(todo: List<Todo>) {
        val todoDiffUtil = TodoDiffUtil(todoList, todo)
        val todoDiffUtilResult = DiffUtil.calculateDiff(todoDiffUtil)
        this.todoList = todo
        todoDiffUtilResult.dispatchUpdatesTo(this)
    }

    fun getDeadline(date: Date): String {
        var deadline = "No Deadline"
        val timeFormat = SimpleDateFormat("EEE, dd-MM-yyyy, hh:mm a", Locale.ENGLISH)
        val time = timeFormat.format(date.time)
        deadline = time.toString()
        return deadline
    }

    private fun parsePriorityToColor(selectedPriority: Priority) : Int{
        var color = R.color.black
        when (selectedPriority.toString()) {
            "HIGH" -> {
                color = R.color.red
            }
            "MEDIUM" -> {
                color = R.color.yellow
            }
            "LOW" -> {
                color = R.color.green
            }
            else -> color = R.color.black
        }
        return color
    }
}