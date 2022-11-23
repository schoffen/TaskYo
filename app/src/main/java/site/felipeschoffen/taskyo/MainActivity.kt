package site.felipeschoffen.taskyo

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import site.felipeschoffen.taskyo.databinding.ActivityMainBinding
import site.felipeschoffen.taskyo.databinding.ItemTaskBinding
import android.view.WindowManager.LayoutParams
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import site.felipeschoffen.taskyo.database.Task
import site.felipeschoffen.taskyo.databinding.DialogCreateTaskBinding
import kotlinx.coroutines.*
import site.felipeschoffen.taskyo.database.TaskDao
import site.felipeschoffen.taskyo.database.TaskList
import site.felipeschoffen.taskyo.database.TaskListDao
import site.felipeschoffen.taskyo.databinding.DialogCreateTaskListBinding
import site.felipeschoffen.taskyo.databinding.DialogFlagSelectorBinding
import site.felipeschoffen.taskyo.databinding.ItemListsBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var rvTasks: RecyclerView
    private lateinit var rvLists: RecyclerView

    private var selectedList: String = "unclassified"

    private var listMenuSwitch = false

    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.to_bottom
        )
    }

    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.from_bottom
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showTasksFromSelectedList()
        showAllTaskLists()

        binding.clMain.setOnClickListener {
            closeAllOpenMenus()
        }

        binding.svTasks.setOnClickListener {
            closeAllOpenMenus()
        }

        binding.btnCreateTask.setOnClickListener {
            closeAllOpenMenus()
            showCreateTaskDialog()
        }

        binding.btnNewList.setOnClickListener {
            closeAllOpenMenus()
            showCreateTaskListDialog()
        }

        binding.btnLists.setOnClickListener {
            listMenuBehavior()
        }

        binding.btnHome.setOnClickListener {
            closeAllOpenMenus()
            homeMenuBehavior()
        }

    }

    private fun changeListTitle() {
        binding.tvListTitle.text = selectedList.uppercase()
    }

    private fun closeAllOpenMenus() {
        if (listMenuSwitch) {
            listMenuBehavior()
        }

        setHomeMenuIcon(false)
    }

    private fun homeMenuBehavior() {
        selectedList = "unclassified"
        setHomeMenuIcon(true)
        showTasksFromSelectedList()
    }

    private fun setHomeMenuIcon(colored: Boolean) {
        if (colored) {
            binding.btnHome.setImageResource(R.drawable.ic_menu_home_colored)
        } else {
            binding.btnHome.setImageResource(R.drawable.ic_menu_home_gray)
        }
    }

    private fun listMenuBehavior() {
        setListMenuIcon()
        setListMenuVisibility()
        setListMenuAnimation()
        listMenuSwitch = !listMenuSwitch
    }

    private fun setListMenuIcon() {
        if (!listMenuSwitch) {
            binding.btnLists.setImageResource(R.drawable.ic_menu_lists_colored)
        } else {
            binding.btnLists.setImageResource(R.drawable.ic_menu_lists_gray)
        }
    }

    private fun setListMenuVisibility() {
        if (!listMenuSwitch) {
            binding.svLists.visibility = View.VISIBLE
            binding.btnNewList.visibility = View.VISIBLE
        } else {
            binding.svLists.visibility = View.INVISIBLE
            binding.btnNewList.visibility = View.INVISIBLE
        }
    }

    private fun setListMenuAnimation() {
        if (!listMenuSwitch) {
            binding.svLists.startAnimation(fromBottom)
            binding.btnNewList.startAnimation(fromBottom)
        } else {
            binding.svLists.startAnimation(toBottom)
            binding.btnNewList.startAnimation(toBottom)
        }

    }

    private fun showCreateTaskListDialog() {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DialogCreateTaskListBinding.inflate(LayoutInflater.from(dialog.context))
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
            showAllTaskLists()
            selectedList = taskList.name
            showTasksFromSelectedList()
        }
    }

    private fun deleteTaskList(taskList: TaskList) {
        lifecycleScope.launch {
            homeMenuBehavior()

            Thread {
                getTaskDao().deleteTasksByTaskList(taskList.name)
            }.start()

            getTaskListDao().delete(taskList)
            closeAllOpenMenus()
            showAllTaskLists()
        }
    }

    private fun showAllTaskLists() {
        Thread {
            val taskLists = getTaskListDao().fetchAllTasks()

            runOnUiThread {
                updateTaskListRecyclerView(taskLists)
            }
        }.start()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateTaskListRecyclerView(taskLists: List<TaskList>) {
        rvLists = binding.rvLists
        rvLists.adapter = TasksListAdapter(taskLists)
        rvLists.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        rvLists.adapter?.notifyDataSetChanged()
    }

    private fun showCreateTaskDialog() {
        val task = Task()
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DialogCreateTaskBinding.inflate(LayoutInflater.from(dialog.context))
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setDimAmount(0f) // Set bottom sheet dialog background to transparent
        dialog.create()
        dialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()

        dialogBinding.ivFlag.setOnClickListener {
            val (priorityDialogBinding, priorityDialog) = showPriorityDialog()

            priorityDialogBinding.btnWhiteFlag.setOnClickListener {
                dialogBinding.ivFlag.setImageResource(R.drawable.ic_flag_white)
                task.priority = Flag.WHITE
                priorityDialog.dismiss()
            }

            priorityDialogBinding.btnGreenFlag.setOnClickListener {
                dialogBinding.ivFlag.setImageResource(R.drawable.ic_flag_green)
                task.priority = Flag.GREEN
                priorityDialog.dismiss()
            }

            priorityDialogBinding.btnOrangeFlag.setOnClickListener {
                dialogBinding.ivFlag.setImageResource(R.drawable.ic_flag_orange)
                task.priority = Flag.ORANGE
                priorityDialog.dismiss()
            }

            priorityDialogBinding.btnRedFlag.setOnClickListener {
                dialogBinding.ivFlag.setImageResource(R.drawable.ic_flag_red)
                task.priority = Flag.RED
                priorityDialog.dismiss()
            }

        }

        dialogBinding.btnSave.setOnClickListener {
            task.description = dialogBinding.etTask.text.toString()
            task.listName = selectedList

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
                changeListTitle()
                updateTasksRecyclerView(tasks)
            }
        }.start()
    }

    private fun getTaskDao(): TaskDao {
        val app = application as App
        return app.db.taskDao()
    }

    private fun getTaskListDao(): TaskListDao {
        val app = application as App
        return app.db.taskListDao()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateTasksRecyclerView(tasks: List<Task>) {
        rvTasks = binding.rvTasks
        rvTasks.adapter = TasksAdapter(tasks)
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter?.notifyDataSetChanged()
    }

    private fun showPriorityDialog(): Pair<DialogFlagSelectorBinding, Dialog> {
        val dialogBinding = DialogFlagSelectorBinding.inflate(layoutInflater)
        val dialog = Dialog(this@MainActivity)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogBinding.root)
        dialog.show()

        return dialogBinding to dialog
    }

    private fun updateTaskFlagPriority(task: Task, flag: Flag) {
        lifecycleScope.launch {
            task.priority = flag
            getTaskDao().update(task)
            showTasksFromSelectedList()
        }
    }

    private inner class TasksListAdapter(private val listOfTaskLists: List<TaskList>) :
        RecyclerView.Adapter<TasksListAdapter.TaskListViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListViewHolder {
            val binding =
                ItemListsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TaskListViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TaskListViewHolder, position: Int) {
            val itemCurrent = listOfTaskLists[position]
            val last = position == listOfTaskLists.size - 1
            holder.bind(itemCurrent, last)
        }

        override fun getItemCount(): Int {
            return listOfTaskLists.size
        }

        private inner class TaskListViewHolder(val binding: ItemListsBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(taskList: TaskList, last: Boolean) {
                if (last) {
                    binding.btnList.background = AppCompatResources.getDrawable(
                        this@MainActivity,
                        R.drawable.recycler_view_item_background_first
                    )
                }

                binding.btnList.text = taskList.name

                binding.btnList.setOnClickListener {
                    selectedList = binding.btnList.text.toString()
                    showTasksFromSelectedList()
                    closeAllOpenMenus()
                }

                binding.btnList.setOnLongClickListener {
                    showDeleteListDialog(taskList)
                    return@setOnLongClickListener true
                }
            }

            fun showDeleteListDialog(taskList: TaskList) {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("Delete ${taskList.name} ?")
                    .setPositiveButton("Delete") { dialogInterface, _ ->
                        deleteTaskList(taskList)
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .create().show()
            }
        }
    }

    private inner class TasksAdapter(private val taskList: List<Task>) :
        RecyclerView.Adapter<TasksAdapter.TasksViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
            val binding =
                ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TasksViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
            val itemCurrent = taskList[position]
            val first = position == 0
            val last = position == (taskList.size - 1)
            holder.bind(itemCurrent, first, last)
        }

        override fun getItemCount(): Int {
            return taskList.size
        }

        private inner class TasksViewHolder(val binding: ItemTaskBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(task: Task, first: Boolean, last: Boolean) {
                if (first) {
                    binding.cLMain.background = AppCompatResources.getDrawable(
                        this@MainActivity,
                        R.drawable.recycler_view_item_background_first
                    )
                }

                if (last) {
                    binding.cLMain.background = AppCompatResources.getDrawable(
                        this@MainActivity,
                        R.drawable.recycler_view_item_background_last
                    )
                }

                binding.tvTask.text = task.description

                when (task.priority) {
                    Flag.WHITE -> binding.ivFlag.setImageResource(R.drawable.ic_flag_white)
                    Flag.GREEN -> binding.ivFlag.setImageResource(R.drawable.ic_flag_green)
                    Flag.ORANGE -> binding.ivFlag.setImageResource(R.drawable.ic_flag_orange)
                    Flag.RED -> binding.ivFlag.setImageResource(R.drawable.ic_flag_red)
                }

                binding.btnDelete.setOnClickListener {
                    deleteTask(task)
                }

                binding.ivFlag.setOnClickListener {
                    val (priorityDialogBinding, priorityDialog) = showPriorityDialog()
                    priorityDialogBinding.btnWhiteFlag.setOnClickListener {
                        updateTaskFlagPriority(task, Flag.WHITE)
                        priorityDialog.dismiss()
                    }

                    priorityDialogBinding.btnGreenFlag.setOnClickListener {
                        updateTaskFlagPriority(task, Flag.GREEN)
                        priorityDialog.dismiss()
                    }

                    priorityDialogBinding.btnOrangeFlag.setOnClickListener {
                        updateTaskFlagPriority(task, Flag.ORANGE)
                        priorityDialog.dismiss()
                    }

                    priorityDialogBinding.btnRedFlag.setOnClickListener {
                        updateTaskFlagPriority(task, Flag.RED)
                        priorityDialog.dismiss()
                    }
                }
            }
        }
    }
}