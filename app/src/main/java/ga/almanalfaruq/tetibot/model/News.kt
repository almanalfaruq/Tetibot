package ga.almanalfaruq.tetibot.model

import android.annotation.SuppressLint
import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by almantera on 19/09/17.
 */
@Entity(tableName = "news")
@SuppressLint("ParcelCreator")
@Parcelize
data class News(@PrimaryKey(autoGenerate = false) val id: String,
                @ColumnInfo(name = "title") val title: String,
                @ColumnInfo(name = "category") val category: String,
                @ColumnInfo(name = "description") val description: String,
                @ColumnInfo(name = "date") val date: String,
                @ColumnInfo(name = "url") val url: String = "") : Parcelable  {

}