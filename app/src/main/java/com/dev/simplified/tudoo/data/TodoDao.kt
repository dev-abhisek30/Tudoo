package com.dev.simplified.tudoo.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TodoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTodo(todo: Todo)

    @Query("SELECT * FROM todo_table ORDER BY id DESC")
    fun readAllTodo(): LiveData<List<Todo>>

    @Update
    suspend fun updateTodo(todo: Todo)

    @Query("SELECT * FROM todo_table WHERE status = 'COMPLETE' ORDER BY id DESC")
    fun readCompleteTasks(): LiveData<List<Todo>>

    @Query("SELECT * FROM todo_table WHERE status = 'ACTIVE' ORDER BY id DESC")
    fun readActiveTasks(): LiveData<List<Todo>>

    @Delete
    suspend fun deleteItem(todo: Todo)

    @Query("DELETE FROM todo_table")
    suspend fun deleteAllTodo()

    @Query("SELECT * FROM todo_table WHERE deadlineDate = (:date)  ORDER BY id DESC")
    fun readSelectedDateTodo(date:String) : LiveData<List<Todo>>

    //custom query to search data from database
    @Query("SELECT * FROM TODO_TABLE WHERE title LIKE :searchQuery")
    fun searchDatabase(searchQuery: String): LiveData<List<Todo>>

    //custom query to sort the data according to high priority in database
    @Query("SELECT * FROM todo_table ORDER BY CASE WHEN priority LIKE 'H%' THEN 1 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'L%' THEN 3 END")
    fun sortByHighPriority(): LiveData<List<Todo>>

    //custom query to sort the data according to low priority in database
    @Query("SELECT * FROM todo_table ORDER BY CASE WHEN priority LIKE 'L%' THEN 1 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'H%' THEN 3 END")
    fun sortByLowPriority(): LiveData<List<Todo>>
}