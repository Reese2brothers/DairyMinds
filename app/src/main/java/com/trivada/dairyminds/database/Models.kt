package com.trivada.dairyminds.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Dairy(
    val title : String,
    var content : String,
    var date : String,
    var time : String,
)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

