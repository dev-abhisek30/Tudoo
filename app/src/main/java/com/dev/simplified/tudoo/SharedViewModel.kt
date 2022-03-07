package com.dev.simplified.tudoo

import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.dev.simplified.tudoo.data.Priority
import com.dev.simplified.tudoo.data.Todo
import com.dev.simplified.tudoo.data.TodoDatabase
import com.dev.simplified.tudoo.data.TodoRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    val readAllTodo: LiveData<List<Todo>>
    val readActiveTodo: LiveData<List<Todo>>
    val readCompleteTodo: LiveData<List<Todo>>
    var readSelectedDateTodo: LiveData<List<Todo>>
    val sortByHighPriority: LiveData<List<Todo>>
    val sortByLowPriority: LiveData<List<Todo>>
    //var todaysDate = Calendar.getInstance()
    var todaysDate = Calendar.getInstance().time
    private val repository: TodoRepository
    //var rand : Boolean
    val emptyDatabase: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        val todoDao = TodoDatabase.getDatabase(application).todoDao()
        repository = TodoRepository(todoDao)
        readAllTodo = repository.readAllTodo
        readActiveTodo = repository.readActiveTodo
        readCompleteTodo = repository.readCompleteTodo
        sortByHighPriority = repository.sortByHighPriority
        sortByLowPriority = repository.sortByLowPriority
        //todaysDate.set(2022,2,1,14,28,0)
        val timeFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val dateInString = timeFormat.format(todaysDate.time)
        readSelectedDateTodo = repository.readSelectedDateTodo(dateInString)
    }

    fun checkIfDatabaseEmpty(toDoData: List<Todo>){
        emptyDatabase.value = toDoData.isEmpty()
    }

    /*fun checkIfSelectedDateTodoEmpty(toDoData: List<Todo>){
        emptyDatabase.value = toDoData.isEmpty()
    }*/

    fun selectedDateTodo(dateInString: String) {
        readSelectedDateTodo = repository.readSelectedDateTodo(dateInString)
    }

    fun addTodo(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTodo(todo)
        }
    }

    fun updateTodo(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTodo(todo)
        }
    }

    fun deleteTodo(todo : Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTodo(todo)
        }
    }

    fun deleteAllTodo() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllTodo()
        }
    }

    //searchDatabase function
    fun searchDatabase(searchQuery: String): LiveData<List<Todo>> {
        return repository.searchDatabase(searchQuery)
    }

    fun parsePriority(selectedPriority: String): Priority {
        return when (selectedPriority) {
            "High" -> {
                Priority.HIGH
            }
            "Medium" -> {
                Priority.MEDIUM
            }
            "Low" -> {
                Priority.LOW
            }
            else -> Priority.LOW
        }
    }

    fun verifyDataFromUser(title: String, description: String): Boolean {
        //if fields are empty return false else true
        return if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) {
            false
        } else !(title.isEmpty() || description.isEmpty())
    }

    suspend fun Context.openDateTimePicker(calendar: Calendar = Calendar.getInstance()): Instant =
        suspendCoroutine { continuation ->
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    calendar
                        .apply { set(year, month, day, hour, minute) }
                        .run { Instant.ofEpochMilli(timeInMillis).truncatedTo(ChronoUnit.MINUTES) }
                        .let { continuation.resume(it) }
                }

                TimePickerDialog(
                    this,
                    timeSetListener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            }

            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

    suspend fun setNewDateTime(context : Context) : Date {
        val date = context.openDateTimePicker()
        return Date.from(date)
    }
}