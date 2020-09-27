package com.vishnurajeevan.chicagoroboto2020.viewmodel

import com.vishnurajeevan.chicagoroboto2020.models.UiNote

data class NoteListViewState(
  val showCompositionDialog: Boolean = false,
  val noteToEdit: UiNote? = null,
  val isLoading: Boolean = true,
  val notes: List<UiNote> = emptyList()
) {
  val creationEnabled get() = notes.size < 5
}
