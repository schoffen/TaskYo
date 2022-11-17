package site.felipeschoffen.taskyo.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task-list-table")
data class TaskList(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
