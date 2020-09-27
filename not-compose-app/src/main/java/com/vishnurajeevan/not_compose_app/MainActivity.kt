package com.vishnurajeevan.not_compose_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.vishnurajeevan.chicagoroboto2020.graph.Graph
import com.vishnurajeevan.chicagoroboto2020.models.UiNote
import com.vishnurajeevan.chicagoroboto2020.modifier.Modification
import com.vishnurajeevan.chicagoroboto2020.viewmodel.NoteListViewState
import com.vishnurajeevan.not_compose_app.databinding.ActivityMainBinding
import com.vishnurajeevan.not_compose_app.databinding.DialogNoteBinding
import com.vishnurajeevan.not_compose_app.databinding.ItemNoteBinding
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var adapter: NoteListAdapter
  private val viewModel by viewModels<NoteListViewModel>()
  private var noteDialog: AlertDialog? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    Graph.setup(this.applicationContext)

    viewModel.state.observe(this, Observer {
      Log.d("Not Compose", "Emitting new $it")
      bind(it)
    })
    viewModel.load()
    binding.noteList.layoutManager = LinearLayoutManager(this)
    adapter = NoteListAdapter {
      viewModel.showNoteCompositionDialog(true, it)
    }
    binding.noteList.adapter = adapter
  }

  private fun bind(viewState: NoteListViewState) = with(binding) {
    Log.d("Not Compose", "binding $viewState")
    addNotesFab.bind(viewState.creationEnabled)

    when {
      viewState.isLoading -> {
        emptyView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        noteList.visibility = View.GONE
        progressBar.show()
      }
      viewState.notes.isEmpty() -> {
        emptyView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        noteList.visibility = View.GONE
      }
      else -> {
        emptyView.visibility = View.GONE
        progressBar.visibility = View.GONE
        noteList.visibility = View.VISIBLE
        adapter.submitList(viewState.notes)
      }
    }

    if (viewState.showCompositionDialog) {
      noteDialog = noteDialog(viewState.noteToEdit).also { it.show() }
    } else {
      noteDialog?.dismiss()
      noteDialog == null
    }
  }

  private fun ExtendedFloatingActionButton.bind(allowCreation: Boolean) {
    if (allowCreation) {
      text = "Add New Note"
      setBackgroundColor(MaterialColors.getColor(this, R.attr.colorSecondary))
      setTextColor(MaterialColors.getColor(this, R.attr.colorOnSecondary))
      setOnClickListener {
        viewModel.showNoteCompositionDialog(true)
      }
    } else {
      text = "Maximum Notes"
      setBackgroundColor(MaterialColors.getColor(this, R.attr.colorError))
      setTextColor(MaterialColors.getColor(this, R.attr.colorOnError))
      setOnClickListener(null)
    }
  }

  private fun noteDialog(note: UiNote? = null): AlertDialog {
    return with(DialogNoteBinding.inflate(layoutInflater)) {
      val creationEnabled = note == null
      val title = if (creationEnabled) "New Note" else "Update Note"
      val dialog = AlertDialog.Builder(this@MainActivity)
        .setTitle(title)
        .setView(root)
        .setOnDismissListener { viewModel.showNoteCompositionDialog(false) }
        .create()

      if (creationEnabled) {
        confirmButton.text = "Create"
        confirmButton.setOnClickListener {
          Graph.modifier.submit(
            Modification.CreateNote(
              noteTitle.text.toString(),
              noteDesc.text.toString()
            )
          )
          viewModel.showNoteCompositionDialog(false)
        }
        deleteButton.visibility = View.GONE
      } else {
        noteTitle.setText(note!!.title)
        noteDesc.setText(note.description)
        confirmButton.text = "Update"
        confirmButton.setOnClickListener {
          Graph.modifier.submit(
            Modification.UpdateNote(
              note.copy(
                title = noteTitle.text.toString(),
                description = noteDesc.text.toString()
              )
            )
          )
          viewModel.showNoteCompositionDialog(false)
        }
        deleteButton.visibility = View.VISIBLE
        deleteButton.setOnClickListener {
          Graph.modifier.submit(Modification.DeleteNote(note.id))
          viewModel.showNoteCompositionDialog(false)
        }
      }
      dialog
    }
  }
}

class NoteListViewModel() : ViewModel() {
  private val repo = Graph.noteRepo
  val state = MutableLiveData(NoteListViewState())

  fun load() = viewModelScope.launch {
    repo.notes()
      .collect {
        Log.d("Not Compose ViewModel", "Got $it from repo")
        state.postValue(state.value!!.copy(notes = it, isLoading = false))
      }
  }

  fun showNoteCompositionDialog(shouldShow: Boolean, noteToEdit: UiNote? = null) {
    state.postValue(
      state.value!!.copy(
        showCompositionDialog = shouldShow,
        noteToEdit = if (shouldShow) noteToEdit else null
      )
    )
  }
}

class NoteListAdapter(private val onItemClick: (UiNote) -> Unit) :
  ListAdapter<UiNote, NoteItemViewHolder>(
    object : DiffUtil.ItemCallback<UiNote?>() {
      override fun areItemsTheSame(old: UiNote, new: UiNote) = old.id == new.id
      override fun areContentsTheSame(old: UiNote, new: UiNote) = old == new
    }) {
  override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NoteItemViewHolder {
    val binding = ItemNoteBinding.inflate(LayoutInflater.from(p0.context), p0, false)
    return NoteItemViewHolder(onItemClick, binding, binding.root)
  }

  override fun onBindViewHolder(p0: NoteItemViewHolder, p1: Int) = p0.bind(getItem(p1))
}

class NoteItemViewHolder(
  private val onItemClick: (UiNote) -> Unit,
  private val binding: ItemNoteBinding,
  itemView: View
) : RecyclerView.ViewHolder(itemView) {
  private lateinit var note: UiNote

  init {
    binding.root.setOnClickListener { onItemClick(note) }
  }

  fun bind(note: UiNote) = with(binding) {
    this@NoteItemViewHolder.note = note
    title.text = note.title
    desc.text = note.description
  }
}