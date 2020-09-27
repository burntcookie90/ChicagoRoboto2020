package com.vishnurajeevan.chicagoroboto2020.graph

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.vishnurajeevan.chicagoroboto2020.db.Database
import com.vishnurajeevan.chicagoroboto2020.db.NoteDao
import com.vishnurajeevan.chicagoroboto2020.modifier.DataModifier
import com.vishnurajeevan.chicagoroboto2020.repo.NoteRepo

object Graph {

  val noteRepo by lazy { NoteRepo() }
  val modifier by lazy { DataModifier() }

  fun setup(context: Context) {
    if (!NoteDao.isReady) {
      NoteDao.setup(AndroidSqliteDriver(Database.Schema, context, "notes.db"))
    }
    else {
      error("Not ready!")
    }
  }

}