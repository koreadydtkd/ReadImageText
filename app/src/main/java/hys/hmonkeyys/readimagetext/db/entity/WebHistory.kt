package hys.hmonkeyys.readimagetext.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "web")
data class WebHistory(
    @PrimaryKey val uid: Int?,
    @ColumnInfo(name = "load_url") val loadUrl: String?,
    @ColumnInfo(name = "visit_date") var visitDate: String?,
)