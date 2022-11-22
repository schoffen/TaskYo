package site.felipeschoffen.taskyo.database

import androidx.room.*

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM `tasks-table`")
    fun fetchAllTasks(): List<Task>

    @Query("SELECT * FROM `tasks-table` WHERE listName = :listName")
    fun fetchTasksByListName(listName: String): List<Task>

    @Query("DELETE FROM `tasks-table` WHERE listName = :listName")
    fun deleteTasksByTaskList(listName: String)
}