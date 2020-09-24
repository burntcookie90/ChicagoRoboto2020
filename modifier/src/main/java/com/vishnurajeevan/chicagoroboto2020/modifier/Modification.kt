package com.vishnurajeevan.chicagoroboto2020.modifier

import com.vishnurajeevan.chicagoroboto2020.models.UiNote


sealed class Modification {
  data class CreateNote(val title: String, val description: String): Modification()
  data class UpdateNote(val note: UiNote): Modification()
  data class DeleteNote(val id: Long): Modification()
}
