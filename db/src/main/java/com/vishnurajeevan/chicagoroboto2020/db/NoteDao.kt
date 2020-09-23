package com.vishnurajeevan.chicagoroboto2020.db

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.vishnurajeevan.chicagoroboto2020.db.model.Note
import com.vishnurajeevan.chicagoroboto2020.db.model.NoteQueries
import kotlinx.coroutines.Dispatchers

object NoteDao {
  lateinit var noteQueries: NoteQueries

  var isReady = false
    private set

  fun setup(driver: SqlDriver) {
    noteQueries = Database(driver).noteQueries
    isReady = true
  }

  fun saveNote(note: Note) = noteQueries.insert(note)

  fun deleteNote(id: Long) = noteQueries.delete(id)

  fun updateNote(note: Note) = noteQueries.update(note.title, note.description, note.id)

  fun notes() = noteQueries.selectAll().asFlow().mapToList(Dispatchers.IO)

  fun count() = noteQueries.count().asFlow().mapToOne(Dispatchers.IO)
}