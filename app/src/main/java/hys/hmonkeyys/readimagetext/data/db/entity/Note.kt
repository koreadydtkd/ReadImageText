package hys.hmonkeyys.readimagetext.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note")
data class Note(
    @PrimaryKey val uid: Int?,
    @ColumnInfo(name = "english") val english: String?,
    @ColumnInfo(name = "korean") var korean: String?,
)