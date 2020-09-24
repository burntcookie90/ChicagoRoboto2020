package com.vishnurajeevan.chicagoroboto2020.modifier

import com.vishnurajeevan.chicagoroboto2020.db.NoteDao
import com.vishnurajeevan.chicagoroboto2020.db.model.Note

class DataModifier {
  fun submit(modification: Modification) = when (modification) {
    is Modification.CreateNote -> NoteDao.createNote(modification.title, modification.description)
    is Modification.UpdateNote -> NoteDao.updateNote(Note(modification.note.id, modification.note.title, modification.note.description))
    is Modification.DeleteNote -> NoteDao.deleteNote(modification.id)
  }
}
