package com.dev.simplified.tudoo

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
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
import nl.bryanderidder.themedtogglebuttongroup.ThemedButton
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
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        setViews()
        binding.updateHighBtn.applyToTexts {
            it.setTypeface(context?.let { it1 -> ResourcesCompat.getFont(it1,R.font.montserratalternates_medium) },
                Typeface.NORMAL)
        }
        binding.updateMediumBtn.applyToTexts {
            it.setTypeface(context?.let { it1 -> ResourcesCompat.getFont(it1,R.font.montserratalternates_medium) },
                Typeface.NORMAL)
        }
        binding.updateLowBtn.applyToTexts {
            it.setTypeface(context?.let { it1 -> ResourcesCompat.getFont(it1,R.font.montserratalternates_medium) },
                Typeface.NORMAL)
        }
        binding.updateToday.applyToTexts {
            it.setTypeface(context?.let { it1 -> ResourcesCompat.getFont(it1,R.font.montserratalternates_medium) },
                Typeface.NORMAL)
        }
        binding.updateTomorrow.applyToTexts {
            it.setTypeface(context?.let { it1 -> ResourcesCompat.getFont(it1,R.font.montserratalternates_medium) },
                Typeface.NORMAL)
        }
        binding.updateChooseBtn.applyToTexts {
            it.setTypeface(context?.let { it1 -> ResourcesCompat.getFont(it1,R.font.montserratalternates_medium) },
                Typeface.NORMAL)
        }
        val deadlineButtonGroup = binding.updateDeadlineBtnGroup
        //val deadlineSelectedButtons = deadlineButtonGroup.selectedButtons[0]
        deadlineButtonGroup.setOnSelectListener { button: ThemedButton ->
            val deadlineSelectedButtons = deadlineButtonGroup.selectedButtons[0]
            if(deadlineSelectedButtons.text == "Today"){
                //date = sharedViewModel.getTodayAndTomorrow(true)
                date = sharedViewModel.today
            }else if(deadlineSelectedButtons.text == "Tomorrow"){
                //date = sharedViewModel.getTodayAndTomorrow(false)
                date = sharedViewModel.tomorrow
            }else {
                CoroutineScope(Dispatchers.Main).launch {
                    date = sharedViewModel.setNewDateTime(requireContext())
                    val timeFormat = SimpleDateFormat("EEE, dd-MM-yyyy, hh:mm a", Locale.ENGLISH)
                    val time = timeFormat.format(date.time)
                    binding.updateChooseBtn.text = time.toString()
                }
            }
            /*CoroutineScope(Dispatchers.Main).launch {
                date = sharedViewModel.setNewDateTime(requireContext())
                val timeFormat = SimpleDateFormat("EEE, dd-MM-yyyy, hh:mm a", Locale.ENGLISH)
                val time = timeFormat.format(date.time)
                binding.updateChooseBtn.text = time.toString()
            }*/
            val timeFormat = SimpleDateFormat("EEE, dd-MM-yyyy, hh:mm a", Locale.ENGLISH)
            val time = timeFormat.format(date.time)
            Toast.makeText(requireContext(), time.toString(), Toast.LENGTH_LONG).show()
        }
        binding.updateTodoButton.setOnClickListener {
            updateTaskInDB(Status.ACTIVE)
        }

        binding.deleteImage.setOnClickListener {
            deleteItem()
        }

        binding.closeTextView.setOnClickListener {
            if(args.fragmentNo == 0)
                findNavController().navigate(R.id.action_updateFragment_to_homeFragment)
            else
                findNavController().navigate(R.id.action_updateFragment_to_allTodoFragment)
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
        val today = sharedViewModel.getDataString(sharedViewModel.today)
        val tomorrow = sharedViewModel.getDataString(sharedViewModel.tomorrow)
        val d = sharedViewModel.getDataString(deadline)
        Log.d("update-",deadline.time.toString())
        //Log.d("updatey",today.time.toString())
        //Log.d("updatew",tomorrow.time.toString())
        if(today == d){
            val view = binding.updateToday
            binding.updateDeadlineBtnGroup.selectButton(view)
        }
        else if(tomorrow == d){
            val view = binding.updateTomorrow
            binding.updateDeadlineBtnGroup.selectButton(view)
        }else {
            val view = binding.updateChooseBtn
            val timeFormat = SimpleDateFormat("EEE, dd-MM-yyyy, hh:mm a", Locale.ENGLISH)
            val time = timeFormat.format(deadline.time)
            view.text = time.toString()
            binding.updateDeadlineBtnGroup.selectButton(view)
        }
    }

    private fun deleteItem() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            sharedViewModel.deleteTodo(args.currentItem)
            Toast.makeText(
                requireContext(),
                "Successfully Removed: ${args.currentItem.title}",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigate(R.id.action_updateFragment_to_homeFragment)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete '${args.currentItem.title}'?")
        builder.setMessage("Are you sure you want to delete '${args.currentItem.title}'?")
        builder.create().show()
    }
}