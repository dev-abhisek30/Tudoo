package com.dev.simplified.tudoo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.dev.simplified.tudoo.databinding.FragmentAddBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import nl.bryanderidder.themedtogglebuttongroup.ThemedButton
import java.text.SimpleDateFormat
import java.util.*


class AddFragment : Fragment() {
    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!
    private val args: AddFragmentArgs by navArgs()

    private lateinit var sharedViewModel: SharedViewModel
    val calendar = Calendar.getInstance()
    private lateinit var date: Date
    private var chosenDate: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val view = binding.root
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        date = calendar.time
        //Log.d("DATE",date.toString())
        //Log.d("DATE",date.year.toString())
        calendar.set(Calendar.YEAR, 1899)
        date = calendar.time
        //Log.d("DATE",date.toString())
        //Log.d("DATE",date.year.toString())

        val priorityButtonGroup = binding.priorityBtnGroup
        priorityButtonGroup.selectButton(binding.highBtn)
        val deadlineButtonGroup = binding.deadlineBtnGroup
        deadlineButtonGroup.setOnSelectListener { button: ThemedButton ->
            val deadlineSelectedButtons = deadlineButtonGroup.selectedButtons[0]
            deadlineSelectedButtons.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    date = sharedViewModel.setNewDateTime(requireContext())
                    val timeFormat = SimpleDateFormat("EEE, dd-MM-yyyy, hh:mm a", Locale.ENGLISH)
                    val time = timeFormat.format(date.time)
                    binding.chooseBtn.text = time.toString()
                }
            }
            chosenDate = true
        }

        binding.addTodoButton.setOnClickListener {
            insertTaskToDB()
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun insertTaskToDB() {
        val mTitle = binding.titleEditText.text.toString()
        val mDescription = binding.descriptionEdittext.text.toString()
        val mPriority = getPrioritySelected()
        val mDeadline = chosenDate && (date.year != -1)
        val validation = sharedViewModel.verifyDataFromUser(mTitle, mDescription)
        val timeFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val dateInString = timeFormat.format(date.time)
        if (validation && mDeadline) {
            val todo = Todo(0, mTitle, mPriority, mDescription, Status.ACTIVE, date, dateInString)
            sharedViewModel.addTodo(todo)
            Toast.makeText(requireContext(), "Successfully Added", Toast.LENGTH_LONG).show()
            if(args.fragmentNo == 0)
                findNavController().navigate(R.id.action_addFragment_to_homeFragment)
            else
                findNavController().navigate(R.id.action_addFragment_to_allTodoFragment)
        } else {
            Toast.makeText(requireContext(), "Please Fill out all Fields", Toast.LENGTH_LONG).show()
        }
    }


    private fun getPrioritySelected(): Priority {
        val priorityButtonGroup = binding.priorityBtnGroup
        val selectedPriority = priorityButtonGroup.selectedButtons[0].text
        return sharedViewModel.parsePriority(selectedPriority)
    }
}