package com.vishnurajeevan.chicagoroboto2020

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Providers
import androidx.compose.runtime.ambientOf
import androidx.compose.ui.platform.setContent
import com.vishnurajeevan.chicagoroboto2020.graph.Graph
import com.vishnurajeevan.chicagoroboto2020.modifier.DataModifier
import com.vishnurajeevan.chicagoroboto2020.repo.NoteRepo

val DataModifierAmbient = ambientOf { DataModifier() }
val NoteRepoAmbient = ambientOf { NoteRepo() }

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Graph.setup(this.applicationContext)
    setContent {
      Providers(DataModifierAmbient provides Graph.modifier, NoteRepoAmbient provides Graph.noteRepo) {
        NoteScreen()
      }
    }
  }
}

