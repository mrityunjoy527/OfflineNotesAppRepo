package com.example.notesmvvm.fragment

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesmvvm.R
import com.example.notesmvvm.activity.MainActivity
import com.example.notesmvvm.adapters.RvNotesAdapter
import com.example.notesmvvm.databinding.FragmentNoteBinding
import com.example.notesmvvm.utils.SwipeToDelete
import com.example.notesmvvm.utils.hideKeyboard
import com.example.notesmvvm.viewModel.NoteActivityViewModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NoteFragment : Fragment(R.layout.fragment_note) {

    private lateinit var noteBinding: FragmentNoteBinding
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private lateinit var rvAdapter: RvNotesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false).apply {
            duration = 350
        }
        enterTransition = MaterialElevationScale(true).apply {
            duration = 350
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteBinding = FragmentNoteBinding.bind(view)
        val activity = activity as MainActivity
        val navControllers = Navigation.findNavController(view)
        requireView().hideKeyboard()
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)
//            activity.window.statusBarColor = Color.WHITE
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.parseColor("#9e9d9d")
        }
        noteBinding.addNoteFab.setOnClickListener{
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navControllers.navigate(NoteFragmentDirections.actionNoteFragmentToSaveAndUpdateFragment())
        }
        noteBinding.innerFab.setOnClickListener{
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navControllers.navigate(NoteFragmentDirections.actionNoteFragmentToSaveAndUpdateFragment())
        }
        recyclerViewDisplay()
        swipeToDelete(noteBinding.rvNote)
        noteBinding.search.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                noteBinding.noData.isVisible = false
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(s.toString().isNotEmpty()) {
                    val text = s.toString()
                    val query = "%$text%"
                    if(query.isNotEmpty()) {
                        noteActivityViewModel.searchNote(query).observe(viewLifecycleOwner) {
                            rvAdapter.submitList(it)
                        }
                    }else observerDataChanges()
                }else observerDataChanges()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        noteBinding.search.setOnEditorActionListener { v, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                v.clearFocus()
                requireView().hideKeyboard()
            }
            return@setOnEditorActionListener true
        }
        noteBinding.rvNote.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->
            when {
                scrollY > oldScrollY -> noteBinding.chatFabText.isVisible = false
                scrollX == scrollY -> noteBinding.chatFabText.isVisible = true
                else -> noteBinding.chatFabText.isVisible = true
            }
        }
    }

    private fun swipeToDelete(rvNote: RecyclerView) {
        val swipeToDeleteCallback = object: SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val note = rvAdapter.currentList[position]
                var actionBtnTapped = false
                noteActivityViewModel.deleteNote(note)
                noteBinding.search.apply {
                    hideKeyboard()
                    clearFocus()
                }
                if(noteBinding.search.text.toString().isEmpty()) {
                    observerDataChanges()
                }
                val snackbar = Snackbar.make(
                    requireView(), "Note Deleted", Snackbar.LENGTH_LONG
                ).addCallback(object: BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {
                        super.onShown(transientBottomBar)
                        transientBottomBar?.setAction("UNDO") {
                            noteActivityViewModel.saveNote(note)
                            actionBtnTapped = true
                            noteBinding.noData.isVisible = false
                        }
                    }
                }).apply {
                    animationMode = Snackbar.ANIMATION_MODE_FADE
                    setAnchorView(R.id.add_note_fab)
                }
                snackbar.setActionTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellowOrange
                    )
                )
                snackbar.show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(rvNote)
    }

    private fun observerDataChanges() {
        noteActivityViewModel.getAllNotes().observe(viewLifecycleOwner) { list->
            noteBinding.noData.isVisible = list.isEmpty()
            rvAdapter.submitList(list)
        }
    }

    private fun recyclerViewDisplay() {
        when(resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> setUpRecyclerView(2)
            Configuration.ORIENTATION_LANDSCAPE -> setUpRecyclerView(3)
        }
    }

    private fun setUpRecyclerView(spanCount: Int) {
        noteBinding.rvNote.apply {
            layoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            rvAdapter = RvNotesAdapter()
            rvAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter = rvAdapter
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        observerDataChanges()
    }
}