package site.felipeschoffen.taskyo

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import site.felipeschoffen.taskyo.databinding.ActivityMainBinding
import site.felipeschoffen.taskyo.databinding.TaskItemBinding
import android.view.WindowManager.LayoutParams


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var rvTasks: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val testList = mutableListOf<Task>()
        testList.add(
            Task(
                0, "Varrer a casa", Flag.WHITE, ""
            )
        )

        testList.add(
            Task(
                2, "Tirar o lixo", Flag.GREEN, ""
            )
        )

        testList.add(
            Task(
                3, "passar pano", Flag.ORANGE, ""
            )
        )

        testList.add(
            Task(
                4, "TESTANDO MENSAGEM LONGA aoiuhdiauhidhahdaiuehdiageoiydgaueydgauiybediyabyudbaybuiyibaeyuibedauiybaeiuybaiuybiuybuaeybyuibdaeuiybaiuyebiuaebea", Flag.RED, ""
            )
        )

        rvTasks = binding.rvTasks
        rvTasks.adapter = TaskListAdapter(testList)
        rvTasks.layoutManager = LinearLayoutManager(this)

        binding.btnCreateTask.setOnClickListener {
            createTask()
        }

    }

    private fun createTask() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(layoutInflater.inflate(R.layout.create_task_dialog, null))
        dialog.create()
        dialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
    }

    private inner class TaskListAdapter(private val taskList: List<Task>)
        : RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListViewHolder {
            val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TaskListViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TaskListViewHolder, position: Int) {
            val itemCurrent = taskList[position]
            holder.bind(itemCurrent)
        }

        override fun getItemCount(): Int {
            return taskList.size
        }

        private inner class TaskListViewHolder(val binding: TaskItemBinding)
            : RecyclerView.ViewHolder(binding.root) {

                fun bind(task: Task) {
                    binding.tvTask.text = task.description
                    when (task.priority) {
                        Flag.WHITE -> binding.ivFlag.setImageResource(R.drawable.ic_white_flag)
                        Flag.GREEN -> binding.ivFlag.setImageResource(R.drawable.ic_green_flag)
                        Flag.ORANGE -> binding.ivFlag.setImageResource(R.drawable.ic_orange_flag)
                        Flag.RED -> binding.ivFlag.setImageResource(R.drawable.ic_red_flag)
                    }
                }

        }
    }
}