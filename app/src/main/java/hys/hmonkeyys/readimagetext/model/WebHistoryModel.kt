package hys.hmonkeyys.readimagetext.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "web")
data class WebHistoryModel (
    @PrimaryKey val uid: Int?,
    @ColumnInfo(name = "load_url") val loadUrl: String?,
    @ColumnInfo(name = "visit_date") val visitDate: String?
)