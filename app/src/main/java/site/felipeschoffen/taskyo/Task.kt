package site.felipeschoffen.taskyo

data class Task(
    var id: Int,
    var description: String,
    var priority: Flag,
    var project: String
    )
