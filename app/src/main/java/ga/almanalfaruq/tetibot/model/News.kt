package ga.almanalfaruq.tetibot.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by almantera on 19/09/17.
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class News(val id: String, val title: String, val category: String,
                val description: String, val date: String,
                val url: String = "") : Parcelable  {

}