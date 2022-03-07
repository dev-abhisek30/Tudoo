package com.dev.simplified.tudoo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dev.simplified.tudoo.data.Priority
import com.dev.simplified.tudoo.data.Status
import com.dev.simplified.tudoo.data.Todo
import com.dev.simplified.tudoo.databinding.FragmentUpdateBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UpdateFragment : Fragment() {
    private var _binding: FragmentUpdateBinding? = null

    private val binding get() = _binding!!
    private val args: UpdateFragmentArgs by navArgs()
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var date: Date
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        val view = binding.root

        setViews()

        val deadlineButtonGroup = binding.updateDeadlineBtnGroup
        val deadlineSelectedButtons = deadlineButtonGroup.selectedButtons[0]
        deadlineSelectedButtons.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                date = sharedViewModel.setNewDateTime(requireContext())
                val timeFormat = SimpleDateFormat("EEE, dd-MM-yyyy, hh:mm a", Locale.ENGLISH)
                val time = timeFormat.format(date.time)
                binding.updateChooseBtn.text = time.toString()
            }
        }

        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        binding.updateTodoButton.setOnClickListener {
            updateTaskInDB(Status.ACTIVE)
        }
        binding.CompleteTodoButton.setOnClickListener {
            updateTaskInDB(Status.COMPLETE)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateTaskInDB(status: Status) {
        val mTitle = binding.updateTitleEditText.text.toString()
        val mDescription = binding.updateDescriptionEdittext.text.toString()
        val mPriority = getPrioritySelected()
        val mDeadline = (date.year != -1)
        val validation = sharedViewModel.verifyDataFromUser(mTitle, mDescription)
        val timeFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val dateInString = timeFormat.format(date.time)

        if (validation && mDeadline) {
            val todo = Todo(args.currentItem.id, mTitle, mPriority, mDescription, status, date, dateInString)
            sharedViewModel.updateTodo(todo)
            Toast.makeText(requireContext(), "Successfully Updated", Toast.LENGTH_LONG).show()
            if(args.fragmentNo == 0)
                findNavController().navigate(R.id.action_updateFragment_to_homeFragment)
            else
                findNavController().navigate(R.id.action_updateFragment_to_allTodoFragment)
        } else {
            Toast.makeText(requireContext(), "Please Fill out all Fields", Toast.LENGTH_LONG).show()
        }
    }

    private fun getPrioritySelected(): Priority {
        val priorityButtonGroup = binding.updatePriorityBtnGroup
        val selectedPriority = priorityButtonGroup.selectedButtons[0].text
        return sharedViewModel.parsePriority(selectedPriority)
    }

    private fun setViews() {
        binding.updateTitleEditText.setText(args.currentItem.title)
        binding.updateDescriptionEdittext.setText(args.currentItem.description)
        parsePriorityToView(args.currentItem.priority)
        date = args.currentItem.deadline
        parseDeadlineToView(args.currentItem.deadline)
    }

    private fun parsePriorityToView(selectedPriority: Priority) {
        var view = binding.updateHighBtn
        when (selectedPriority.toString()) {
            "HIGH" -> {
                view = binding.updateHighBtn
            }
            "MEDIUM" -> {
                view = binding.updateMediumBtn
            }
            "LOW" -> {
                view = binding.updateLowBtn
            }
            else -> view = binding.updateHighBtn
        }
        binding.updatePriorityBtnGroup.selectButton(view)
    }

    private fun parseDeadlineToView(deadline: Date) {
        var view = binding.updateChooseBtn
        val timeFormat = SimpleDateFormat("EEE, dd-MM-yyyy, hh:mm a", Locale.ENGLISH)
        val time = timeFormat.format(deadline.time)
        view.text = time.toString()
        binding.updateDeadlineBtnGroup.selectButton(view)
    }
}