package com.dev.simplified.tudoo.data

import androidx.lifecycle.LiveData

class TodoRepository(private val todoDao: TodoDao) {
    val readAllTodo: LiveData<List<Todo>> = todoDao.readAllTodo()
    val readActiveTodo: LiveData<List<Todo>> = todoDao.readActiveTasks()
    val readCompleteTodo: LiveData<List<Todo>> = todoDao.readCompleteTasks()
    val sortByHighPriority: LiveData<List<Todo>> = todoDao.sortByHighPriority()
    val sortByLowPriority: LiveData<List<Todo>> = todoDao.sortByLowPriority()
    fun readSelectedDateTodo(date: String): LiveData<List<Todo>> = todoDao.readSelectedDateTodo(date)

    suspend fun addTodo(todo: Todo) {
        todoDao.addTodo(todo)
    }

    suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo)
    }

    suspend fun deleteTodo(todo: Todo) {
        todoDao.deleteItem(todo)
    }

    suspend fun deleteAllTodo() {
        todoDao.deleteAllTodo()
    }

    fun searchDatabase(searchQuery: String): LiveData<List<Todo>> {
        return todoDao.searchDatabase(searchQuery)
    }
}