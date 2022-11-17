package site.felipeschoffen.taskyo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import site.felipeschoffen.taskyo.databinding.ActivityMainBinding
import site.felipeschoffen.taskyo.databinding.TaskItemBinding
import android.view.WindowManager.LayoutParams
import androidx.lifecycle.lifecycleScope
import site.felipeschoffen.taskyo.database.Task
import site.felipeschoffen.taskyo.databinding.CreateTaskDialogBinding
import kotlinx.coroutines.*
import site.felipeschoffen.taskyo.database.TaskDao
import site.felipeschoffen.taskyo.database.TaskList
import site.felipeschoffen.taskyo.database.TaskListDao
import site.felipeschoffen.taskyo.databinding.CreateTaskListDialogBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var rvTasks: RecyclerView
    private var selectedList: String = "unclassified"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showTasksFromSelectedList()

        binding.btnCreateTask.setOnClickListener {
            showCreateTaskDialog()
        }
    }

    private fun showCreateTaskListDialog() {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = CreateTaskListDialogBinding.inflate(LayoutInflater.from(dialog.context))
        dialog.setContentView(dialogBinding.root)
        dialog.create()
        dialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
        dialogBinding.btnSave.setOnClickListener {
            val taskList = TaskList(name = dialogBinding.etTaskList.text.toString())
            recordTaskList(taskList)
            dialog.dismiss()
        }
    }

    private fun recordTaskList(taskList: TaskList) {
        lifecycleScope.launch {
            getTaskListDao().insert(taskList)
        }
    }

    private fun showCreateTaskDialog() {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = CreateTaskDialogBinding.inflate(LayoutInflater.from(dialog.context))
        dialog.setContentView(dialogBinding.root)
        dialog.create()
        dialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
        dialogBinding.btnSave.setOnClickListener {
            val task = Task(description = dialogBinding.etTask.text.toString())
            recordTask(task)
            dialog.dismiss()
        }
    }

    private fun deleteTask(task: Task) {
        lifecycleScope.launch {
            getTaskDao().delete(task)
            showTasksFromSelectedList()
        }
    }

    private fun recordTask(task: Task) {
        lifecycleScope.launch {
            getTaskDao().insert(task)
            showTasksFromSelectedList()
        }
    }

    private fun showTasksFromSelectedList() {
        Thread {
            val tasks = getTaskDao().fetchTasksByListName(selectedList)

            runOnUiThread {
                updateRecyclerView(tasks)
            }
        }.start()
    }

    private fun getTaskDao() : TaskDao {
        val app = application as App
        return app.db.taskDao()
    }

    private fun getTaskListDao() : TaskListDao {
        val app = application as App
        return app.db.taskListDao()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView(tasks: List<Task>) {
        rvTasks = binding.rvTasks
        rvTasks.adapter = TasksAdapter(tasks)
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter?.notifyDataSetChanged()
    }

    //TODO create inner class TaskListAdapter

    private inner class TasksAdapter(private val taskList: List<Task>) :
        RecyclerView.Adapter<TasksAdapter.TaskListViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListViewHolder {
            val binding =
                TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TaskListViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TaskListViewHolder, position: Int) {
            val itemCurrent = taskList[position]
            holder.bind(itemCurrent)
        }

        override fun getItemCount(): Int {
            return taskList.size
        }

        private inner class TaskListViewHolder(val binding: TaskItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(task: Task) {

                binding.tvTask.text = task.description

                when (task.priority) {
                    Flag.WHITE -> binding.ivFlag.setImageResource(R.drawable.ic_white_flag)
                    Flag.GREEN -> binding.ivFlag.setImageResource(R.drawable.ic_green_flag)
                    Flag.ORANGE -> binding.ivFlag.setImageResource(R.drawable.ic_orange_flag)
                    Flag.RED -> binding.ivFlag.setImageResource(R.drawable.ic_red_flag)
                }

                binding.btnDelete.setOnClickListener {
                    deleteTask(task)
                }
            }
        }
    }
}