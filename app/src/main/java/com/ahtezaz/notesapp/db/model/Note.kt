package com.ahtezaz.notesapp.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class Note(
    @PrimaryKey(autoGenerate = false)
    var id: Int? = 0,
    val title: String,
    val location: String,
    val descriptor: String,
    val image: String,
    val audioFilePath: String,

    ) : java.io.Serializable {

}