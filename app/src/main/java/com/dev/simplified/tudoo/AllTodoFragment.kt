package com.dev.simplified.tudoo

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev.simplified.tudoo.data.Todo
import com.dev.simplified.tudoo.databinding.FragmentAllTodoBinding
import com.google.android.material.snackbar.Snackbar


class AllTodoFragment : Fragment(){
    private var _binding: FragmentAllTodoBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var adapter: AllTodoAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllTodoBinding.inflate(inflater, container, false)
        val view = binding.root

        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        setUpRecyclerView()

        val search = binding.searchView
        search.isSubmitButtonEnabled = true
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchThroughDatabase(query)
                }
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {
                    searchThroughDatabase(query)
                }
                return true
            }
        })

        sharedViewModel.readAllTodo.observe(viewLifecycleOwner) { user ->
            sharedViewModel.checkIfDatabaseEmpty(user)
            sharedViewModel.emptyDatabase.value?.let { setVisibility(it) }
            adapter.setData(user)
        }

        /*val id = binding.searchView.context.resources.getIdentifier("android:id/search_src_text", null, null)
        val searchView = binding.searchView.findViewById<TextView>(id)
        val myCustomFont = context?.let { ResourcesCompat.getFont(it,R.font.montserratalternates_medium) }
        searchView.setTypeface(myCustomFont)*/

        binding.addBtn.setOnClickListener {
            val action = AllTodoFragmentDirections.actionAllTodoFragmentToAddFragment(1)
            findNavController().navigate(action)
        }

        binding.settingsImage.setOnClickListener {
            val popup = PopupMenu(context,binding.settingsImage)
            popup.menuInflater.inflate(R.menu.popup_menu,popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menuDeleteAll -> deleteAll()
                    R.id.menuSortByHighPriority -> sharedViewModel.sortByHighPriority.observe(viewLifecycleOwner, Observer { adapter.setData(it) })
                    R.id.menuSortByLowPriority -> sharedViewModel.sortByLowPriority.observe(viewLifecycleOwner, Observer { adapter.setData(it) })
                }
                true
            }
            popup.show()
        }


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"

        sharedViewModel.searchDatabase(searchQuery).observe(this, Observer { list ->
            list?.let {
                //sharedViewModel.checkIfDatabaseEmpty(list)
                adapter.setData(it)
            }
        })
    }

    private fun setUpRecyclerView() {
        adapter = AllTodoAdapter()
        val recyclerView = binding.todoRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        swipeToDelete(recyclerView)
    }

    private fun deleteAll() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            sharedViewModel.deleteAllTodo()
            Toast.makeText(
                requireContext(),
                "Successfully Removed: Everything",
                Toast.LENGTH_SHORT
            ).show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete everything?")
        builder.setMessage("Are you sure you want to remove everything?")
        builder.create().show()
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deleteItem = adapter.todoList[viewHolder.adapterPosition]
                //deleteItem
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
            }
            false -> {
                binding.emptyTodoImageView.visibility = View.INVISIBLE
                binding.emptyTodoTxtview.visibility = View.INVISIBLE
            }
        }
    }
}