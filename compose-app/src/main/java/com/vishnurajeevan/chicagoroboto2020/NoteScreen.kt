package com.vishnurajeevan.chicagoroboto2020

import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import androidx.ui.tooling.preview.PreviewParameter
import androidx.ui.tooling.preview.PreviewParameterProvider
import com.vishnurajeevan.chicagoroboto2020.graph.Graph
import com.vishnurajeevan.chicagoroboto2020.models.UiNote
import com.vishnurajeevan.chicagoroboto2020.modifier.Modification
import com.vishnurajeevan.chicagoroboto2020.ui.ChicagoRoboto2020Theme
import com.vishnurajeevan.chicagoroboto2020.viewmodel.NoteListViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn

@Composable
fun NoteScreen() {
  val state = remember { mutableStateOf(NoteListViewState()) }
  launchInComposition {
    Graph.noteRepo.notes().flowOn(Dispatchers.IO).collect {
      state.value = state.value.copy(isLoading = false, notes = it)
    }
  }

  fun showNoteCompositionDialog(shouldShow: Boolean) {
    state.value = state.value.copy(
        showCompositionDialog = shouldShow,
        noteToEdit = if (shouldShow) state.value.noteToEdit else null
    )
  }

  val dataModifier = AmbientDataModifier.current
  ChicagoRoboto2020Theme {
    // A surface container using the 'background' color from the theme
    Surface(color = MaterialTheme.colors.background) {
      Scaffold(
          topBar = { AppBar() },
          floatingActionButton = {
            if (!state.value.isLoading) {
              NewNoteFab(
                  allowCreation = state.value.creationEnabled,
                  onClick = { showNoteCompositionDialog(true) }
              )
            }
          }
      ) {
        NoteList(
            list = state.value.notes,
            isLoading = state.value.isLoading,
            onItemClicked = {
              state.value = state.value.copy(showCompositionDialog = true, noteToEdit = it)
            })

        if (state.value.showCompositionDialog) {
          NoteDialog(
              note = state.value.noteToEdit,
              onNoteCreate = { title, desc ->
                dataModifier.submit(Modification.CreateNote(title, desc))
                showNoteCompositionDialog(false)
              },
              onNoteUpdate = {
                dataModifier.submit(Modification.UpdateNote(it))
                showNoteCompositionDialog(false)
              },
              onNoteDelete = {
                dataModifier.submit(Modification.DeleteNote(it))
                showNoteCompositionDialog(false)
              },
              onDismiss = { showNoteCompositionDialog(false) }
          )
        }
      }
    }
  }
}

@Composable
fun AppBar() {
  TopAppBar() {
    Box(
        modifier = Modifier.fillMaxSize().padding(start = 16.dp),
        gravity = Alignment.CenterStart
    ) { Text(text = "Compose App", style = MaterialTheme.typography.h5) }
  }
}

@Composable
fun NoteItem(
    note: UiNote,
    onItemClicked: (UiNote) -> Unit = {}
) {
  Card(
      modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
          .clickable(onClick = { onItemClicked(note) }),
      elevation = 2.dp
  ) {
    Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
      Text(text = note.title, style = MaterialTheme.typography.h6)
      Text(text = note.description, style = MaterialTheme.typography.body1)
    }
  }
}

@Composable
fun NoteList(
    list: List<UiNote>,
    isLoading: Boolean,
    onItemClicked: (UiNote) -> Unit
) {
  when {
    isLoading -> Box(modifier = Modifier.fillMaxSize(), gravity = Alignment.Center) {
      CircularProgressIndicator()
    }
    list.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), gravity = Alignment.Center) {
      Text(text = "No notes")
    }
    else -> LazyColumnFor(items = list) {
      NoteItem(note = it, onItemClicked)
    }
  }
}

@Preview("New Note Fab")
@Composable
fun NewNoteFab(
    @PreviewParameter(BooleanParamProvider::class) allowCreation: Boolean,
    onClick: () -> Unit = {}) {
  ExtendedFloatingActionButton(
      text = { if (allowCreation) Text("Add New Note") else Text("Maximum Notes") },
      backgroundColor = if (allowCreation) MaterialTheme.colors.secondary else MaterialTheme.colors.error,
      contentColor = if (allowCreation) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onError,
      onClick = {
        if (allowCreation) {
          onClick()
        }
      }
  )
}

@Composable
fun NoteDialog(
    note: UiNote? = null,
    onNoteCreate: (title: String, description: String) -> Unit = { _, _ -> },
    onNoteUpdate: (UiNote) -> Unit = {},
    onNoteDelete: (id: Long) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
  when (note) {
    null -> {
      val title = remember { mutableStateOf("") }
      val description = remember { mutableStateOf("") }

      AlertDialog(
          onDismissRequest = { onDismiss() },
          title = { Text("New Note") },
          text = {
            Column {
              Text("Title")
              TextField(value = title.value, onValueChange = { title.value = it })
              Text("Description")
              TextField(value = description.value, onValueChange = { description.value = it })
            }
          },
          confirmButton = {
            Button(
                onClick = { onNoteCreate(title.value, description.value) },
                enabled = title.value.isNotBlank() && description.value.isNotBlank()
            ) {
              Text(text = "Create")
            }
          }
      )
    }
    else -> {
      val title = remember { mutableStateOf(note.title) }
      val description = remember { mutableStateOf(note.description) }

      AlertDialog(
          onDismissRequest = { onDismiss() },
          title = { Text("Update Note") },
          text = {
            Column {
              Text("Title")
              TextField(value = title.value, onValueChange = { title.value = it })
              Text("Description")
              TextField(value = description.value, onValueChange = { description.value = it })
            }
          },
          confirmButton = {
            Button(
                onClick = {
                  onNoteUpdate(note.copy(title = title.value, description = description.value))
                },
                enabled = title.value.isNotBlank() && description.value.isNotBlank()
            ) { Text(text = "Update") }
          },
          dismissButton = {
            Button(
                backgroundColor = MaterialTheme.colors.error,
                contentColor = MaterialTheme.colors.onError,
                onClick = { onNoteDelete(note.id) }
            ) { Text(text = "Delete") }
          }
      )
    }
  }
}

// region previews
@Preview("note item")
@Composable
fun NoteItemPreview() = NoteItem(
    note =
    UiNote(1L, "title", "This is my preview description")
)

@Preview(name = "empty list")
@Composable
fun EmptyNoteList() = NoteList(emptyList(), false, {})

@Preview(name = "loading")
@Composable
fun LoadingNoteList() = NoteList(emptyList(), true, {})

@Preview(name = "loaded list")
@Composable
fun PreviewNoteList() = NoteList(
    list = listOf(
        UiNote(1L, "Title 1", "Desc 1"),
        UiNote(2L, "Title 2", "Desc 2"),
        UiNote(3L, "Title 3", "Desc 3"),
    ),
    isLoading = false,
    onItemClicked = {}
)

class BooleanParamProvider(
    override val values: Sequence<Boolean> = sequenceOf(true, false)
) : PreviewParameterProvider<Boolean>


//@Preview("new note dialog")
//@Composable
//fun NewNoteDialogPreview() = NoteDialog()
//
//@Preview("edit note dialog")
//@Composable
//fun EditNoteDialogPreview() = NoteDialog(
//  note = UiNote(1L, "title", "This is my preview description")
//)

// endregion
