package com.vishnurajeevan.chicagoroboto2020

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import androidx.ui.tooling.preview.PreviewParameter
import androidx.ui.tooling.preview.PreviewParameterProvider
import com.vishnurajeevan.chicagoroboto2020.graph.Graph
import com.vishnurajeevan.chicagoroboto2020.models.UiNote
import com.vishnurajeevan.chicagoroboto2020.modifier.DataModifier
import com.vishnurajeevan.chicagoroboto2020.modifier.Modification
import com.vishnurajeevan.chicagoroboto2020.ui.ChicagoRoboto2020Theme
import kotlinx.coroutines.flow.collect

val AmbientDataModifier = ambientOf { DataModifier() }

data class ViewState(
  val showCompositionDialog: Boolean = false,
  val noteToEdit: UiNote? = null,
  val isLoading: Boolean = true,
  val notes: List<UiNote> = emptyList()
)

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Graph.setup(this.applicationContext)

    setContent {
      val state = remember { mutableStateOf(ViewState()) }
      launchInComposition {
        Graph.noteRepo.notes().collect {
          state.value = state.value.copy(isLoading = false, notes = it)
        }
      }
      Providers(AmbientDataModifier provides Graph.modifier) {
        ChicagoRoboto2020Theme {
          // A surface container using the 'background' color from the theme
          Surface(color = MaterialTheme.colors.background) {
            fun showNoteCompositionDialog(shouldShow: Boolean) {
              state.value = state.value.copy(
                showCompositionDialog = shouldShow,
                noteToEdit = if (shouldShow) state.value.noteToEdit else null
              )
            }
            Scaffold(
              topBar = {
                TopAppBar() {
                  Box(
                    modifier = Modifier.fillMaxSize().padding(start = 16.dp),
                    gravity = Alignment.CenterStart
                  ) { Text(text = "Compose App", style = MaterialTheme.typography.h5) }
                }
              },
              floatingActionButton = {
                if (!state.value.isLoading) {
                  val enabled = state.value.notes.size < 5
                  NewNoteFab(enabled, onClick = { showNoteCompositionDialog(true) })
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
                NoteDialog(note = state.value.noteToEdit, showDialog = ::showNoteCompositionDialog)
              }
            }
          }
        }
      }
    }
  }
}

@Preview
@Composable
fun NoteItem(
  @PreviewParameter(SampleNoteProvider::class) note: UiNote,
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

@Preview
@Composable
fun NoteList(
  @PreviewParameter(SampleNoteListProvider::class) list: List<UiNote>,
  isLoading: Boolean = false,
  onItemClicked: (UiNote) -> Unit = {}
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

@Composable
fun NewNoteFab(enabled: Boolean, onClick: () -> Unit) {
  ExtendedFloatingActionButton(
    text = { if (enabled) Text("Add New Note") else Text("Maximum Notes") },
    onClick = {
      if (enabled) {
        onClick()
      }
    }
  )
}

@Composable
fun NoteDialog(note: UiNote? = null, showDialog: (Boolean) -> Unit) {
  val isCreation = note == null
  val title = remember { mutableStateOf(note?.title ?: "") }
  val description = remember { mutableStateOf(note?.description ?: "") }
  val dataModifier = AmbientDataModifier.current

  val dialogTitle = if (isCreation) "New Note" else "Update Note"

  AlertDialog(
    onDismissRequest = { showDialog(false) },
    title = { Text(dialogTitle) },
    text = {
      Column() {
        Text("Title")
        TextField(value = title.value, onValueChange = { title.value = it })
        Text("Description")
        TextField(value = description.value, onValueChange = { description.value = it })
      }
    },
    confirmButton = {
      if (isCreation) {
        Button(
          onClick = {
            dataModifier.submit(
              Modification.CreateNote(title = title.value, description = description.value)
            )
            showDialog(false)
          },
          enabled = title.value.isNotBlank() && description.value.isNotBlank()
        ) {
          Text(text = "Create")
        }
      } else {
        Button(
          onClick = {
            dataModifier.submit(
              Modification.UpdateNote(
                note!!.copy(title = title.value, description = description.value)
              )
            )
            showDialog(false)
          },
          enabled = title.value.isNotBlank() && description.value.isNotBlank()
        ) { Text(text = "Update") }
      }
    },
    dismissButton = {
      if (!isCreation) {
        Button(
          backgroundColor = MaterialTheme.colors.error,
          contentColor = MaterialTheme.colors.onError,
          onClick = {
            dataModifier.submit(Modification.DeleteNote(note!!.id))
            showDialog(false)
          }
        ) { Text(text = "Delete") }
      }
    }
  )
}

class SampleNoteProvider : PreviewParameterProvider<UiNote> {
  override val values = sequenceOf(UiNote(1L, "title", "This is my preview description"))
}

class SampleNoteListProvider : PreviewParameterProvider<List<UiNote>> {
  override val values: Sequence<List<UiNote>>
    get() = sequenceOf(
      listOf(
        UiNote(1L, "First Note", "This is the description for the first note"),
        UiNote(2L, "Second Note", "This is the description for the second note"),
      )
    )
}
