package site.felipeschoffen.taskyo.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT * FROM `tasks-table` where listName = :listName")
    fun fetchTasksByListName(listName: String): List<Task>
}