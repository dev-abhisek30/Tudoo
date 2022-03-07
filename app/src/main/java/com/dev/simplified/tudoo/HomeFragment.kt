package com.dev.simplified.tudoo

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.simplified.tudoo.data.Todo
import com.dev.simplified.tudoo.databinding.CalendarDayLayoutBinding
import com.dev.simplified.tudoo.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.InDateStyle
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.yearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    //used by calenderView
    private var selectedDates: LocalDate? = null
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private val selectionFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var adapter: HomeAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        setUpRecyclerView()

        val daysOfWeek = daysOfWeekFromLocale()
        binding.legendLayout.root.children.forEachIndexed { index, view ->
            (view as TextView).apply {
                text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase(Locale.ENGLISH)
                setTextColorRes(R.color.black)
            }
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
            //val dotView = CalendarDayLayoutBinding.bind(view).CalenderDotView
            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date)
                    }
                    binding.calendar.notifyDayChanged(day)
                }
            }
        }

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)

        binding.calendar.setup(startMonth, endMonth, daysOfWeek.first())

        binding.calendar.scrollToMonth(currentMonth)

        if (savedInstanceState == null) {
            binding.calendar.post {
                // Show today's events initially.
                selectDate(today)
            }
        }

        binding.calendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                //val dotView = container.dotView
                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    when {
                        today == day.date -> {
                            textView.setTextColorRes(R.color.example_3_white)
                            textView.setBackgroundResource(R.drawable.calendar_today_bg)
                            //dotView.makeInVisible()
                        }
                        selectedDates == day.date -> {
                            textView.setTextColorRes(R.color.example_3_blue)
                            textView.setBackgroundResource(R.drawable.calendar_selected_bg)
                            //dotView.makeInVisible()
                        }
                        else -> {
                            textView.setTextColorRes(R.color.example_3_black)
                            textView.background = null
                            val dateInString = selectionFormatter.format(day.date).toString()
                            sharedViewModel.selectedDateTodo(dateInString)
                            //dotView.isVisible = events[day.date].orEmpty().isNotEmpty()
                        }
                    }
                } else {
                    textView.setTextColorRes(R.color.example_1_white_light)
                    textView.background = null
                    //dotView.makeInVisible()
                }
            }
        }

        binding.calendar.monthScrollListener = {
            calendarMonthScrollListner(it)
        }

        binding.weekModeCheckBox.setOnCheckedChangeListener { _, monthToWeek ->
            val firstDate = binding.calendar.findFirstVisibleDay()?.date ?: return@setOnCheckedChangeListener
            val lastDate = binding.calendar.findLastVisibleDay()?.date ?: return@setOnCheckedChangeListener
            weekModeClickListner( endMonth, monthToWeek, firstDate, lastDate)
        }

        binding.addBtn.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToAddFragment(0)
            findNavController().navigate(action)
        }

        sharedViewModel.readSelectedDateTodo.observe(viewLifecycleOwner) { list ->
            sharedViewModel.checkIfDatabaseEmpty(list)
            sharedViewModel.emptyDatabase.value?.let { setVisibility(it) }
            adapter.setData(list)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDates != date) {
            val oldDate = selectedDates
            selectedDates = date
            oldDate?.let { binding.calendar.notifyDateChanged(it) }
            binding.calendar.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
    }

    private fun updateAdapterForDate(date: LocalDate) {
        /*eventsAdapter.apply {
            events.clear()
            events.addAll(this@Example3Fragment.events[date].orEmpty())
            notifyDataSetChanged()
        }*/
        //binding.exThreeSelectedDateText.text = selectionFormatter.format(date)
        //Log.d("-Here--",selectionFormatter.format(date).toString())
        val dateInString = selectionFormatter.format(date).toString()
        sharedViewModel.selectedDateTodo(dateInString)
        sharedViewModel.readSelectedDateTodo.observe(viewLifecycleOwner) { list ->
            sharedViewModel.checkIfDatabaseEmpty(list)
            sharedViewModel.emptyDatabase.value?.let { setVisibility(it) }
            adapter.setData(list)
        }
    }

    private fun TextView.setTextColorRes(@ColorRes color: Int) = setTextColor(context.getColorCompat(color))

    private fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

    private fun calendarMonthScrollListner(it: CalendarMonth) {
        if (binding.calendar.maxRowCount == 6) {
            binding.yearText.text = it.yearMonth.year.toString()
            binding.monthText.text = monthTitleFormatter.format(it.yearMonth)
        } else {
            val firstDate = it.weekDays.first().first().date
            val lastDate = it.weekDays.last().last().date
            if (firstDate.yearMonth == lastDate.yearMonth) {
                binding.yearText.text = firstDate.yearMonth.year.toString()
                binding.monthText.text = monthTitleFormatter.format(firstDate)
            } else {
                binding.monthText.text =
                    "${monthTitleFormatter.format(firstDate)} - ${monthTitleFormatter.format(lastDate)}"
                if (firstDate.year == lastDate.year) {
                    binding.yearText.text = firstDate.yearMonth.year.toString()
                } else {
                    binding.yearText.text = "${firstDate.yearMonth.year} - ${lastDate.yearMonth.year}"
                }
            }
        }
    }

    private fun weekModeClickListner( endMonth: YearMonth, monthToWeek: Boolean, firstDate: LocalDate, lastDate: LocalDate){
        val oneWeekHeight = binding.calendar.daySize.height
        val oneMonthHeight = oneWeekHeight * 6

        val oldHeight = if (monthToWeek) oneMonthHeight else oneWeekHeight
        val newHeight = if (monthToWeek) oneWeekHeight else oneMonthHeight

        val animator = ValueAnimator.ofInt(oldHeight, newHeight)
        animator.addUpdateListener { animator ->
            binding.calendar.updateLayoutParams {
                height = animator.animatedValue as Int
            }
        }
        animator.doOnStart {
            if (!monthToWeek) {
                binding.calendar.updateMonthConfiguration(
                    inDateStyle = InDateStyle.ALL_MONTHS,
                    maxRowCount = 6,
                    hasBoundaries = true
                )
            }
        }
        animator.doOnEnd {
            if (monthToWeek) {
                binding.calendar.updateMonthConfiguration(
                    inDateStyle = InDateStyle.FIRST_MONTH,
                    maxRowCount = 1,
                    hasBoundaries = false
                )
            }

            if (monthToWeek) {
                binding.calendar.scrollToDate(firstDate)
            } else {
                if (firstDate.yearMonth == lastDate.yearMonth) {
                    binding.calendar.scrollToMonth(firstDate.yearMonth)
                } else {
                    binding.calendar.scrollToMonth(minOf(firstDate.yearMonth.next, endMonth))
                }
            }
        }
        animator.duration = 250
        animator.start()
    }

    fun View.makeInVisible() {
        visibility = View.INVISIBLE
    }

    private fun setUpRecyclerView() {
        adapter = HomeAdapter()
        val recyclerView = binding.todoRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        swipeToDelete(recyclerView)
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deleteItem = adapter.todoList[viewHolder.adapterPosition]
                sharedViewModel.deleteTodo(deleteItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)

                undoDelete(viewHolder.itemView, deleteItem, viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    //to restore DeletedItem
    private fun undoDelete(view: View, deleteItem: Todo, position: Int) {
        val snackbar = Snackbar.make(
            view, "Delete '${deleteItem.title}'", Snackbar.LENGTH_LONG
        )
        snackbar.setAction("Undo") {
            sharedViewModel.addTodo(deleteItem)
            adapter.notifyItemChanged(position)
        }
        snackbar.show()
    }

    private fun setVisibility (value : Boolean) {
        when(value) {
            true -> {
                binding.emptyTodoImageView.visibility = View.VISIBLE
                binding.emptyTodoTxtview.visibility = View.VISIBLE
                binding.todoRecyclerView.visibility = View.GONE
            }
            false -> {
                binding.emptyTodoImageView.visibility = View.GONE
                binding.emptyTodoTxtview.visibility = View.GONE
                binding.todoRecyclerView.visibility = View.VISIBLE
            }
        }
    }
}