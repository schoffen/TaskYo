package site.felipeschoffen.taskyo.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import site.felipeschoffen.taskyo.Flag

@Entity(tableName = "tasks-table")
data class Task(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var description: String = "",
    var priority: Flag = Flag.WHITE,
    var listName: String = "unclassified",
    var complete: Boolean = false
    )
