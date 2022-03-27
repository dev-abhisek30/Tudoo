package com.dev.simplified.tudoo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dev.simplified.tudoo.data.Todo
import com.dev.simplified.tudoo.data.TodoDatabase
import com.dev.simplified.tudoo.data.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TodoViewModel (application: Application) : AndroidViewModel(application) {
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
}