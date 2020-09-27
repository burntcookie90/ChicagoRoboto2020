package com.vishnurajeevan.chicagoroboto2020

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.ambientOf
import androidx.compose.ui.platform.setContent
import com.vishnurajeevan.chicagoroboto2020.graph.Graph
import com.vishnurajeevan.chicagoroboto2020.modifier.DataModifier

val AmbientDataModifier = ambientOf { DataModifier() }

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Graph.setup(this.applicationContext)
    setContent { NoteScreen() }
  }
}

