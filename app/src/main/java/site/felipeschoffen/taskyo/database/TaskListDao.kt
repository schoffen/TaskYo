package site.felipeschoffen.taskyo.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskListDao {
    @Insert
    suspend fun insert(taskList: TaskList)

    @Update
    suspend fun update(taskList: TaskList)

    @Delete
    suspend fun delete(taskList: TaskList)

    @Query("SELECT * FROM `task-list-table`")
    fun fetchAllTasks(): List<TaskList>
}