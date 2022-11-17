package site.felipeschoffen.taskyo

import site.felipeschoffen.taskyo.database.Task

interface OnItemClickListener {
    fun onClick(task: Task)
}