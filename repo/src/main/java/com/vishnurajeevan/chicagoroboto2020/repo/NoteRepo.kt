package com.vishnurajeevan.chicagoroboto2020.repo

import com.vishnurajeevan.chicagoroboto2020.db.NoteDao
import com.vishnurajeevan.chicagoroboto2020.models.UiNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class NoteRepo {
  private val repoContext = Dispatchers.IO

  fun notes() = NoteDao.notes()
    .map { notes ->
      notes.map { UiNote(it.id, it.title, it.description) }
    }
    .flowOn(repoContext)

  fun noteCount() = NoteDao.count()
    .flowOn(repoContext)
}