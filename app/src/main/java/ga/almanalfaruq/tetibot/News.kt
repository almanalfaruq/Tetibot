package ga.almanalfaruq.tetibot

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import org.jsoup.Jsoup
import java.io.Serializable
import java.util.ArrayList

/**
 * Created by almantera on 19/09/17.
 */

data class News(var id: String, var title: String, var category: String, var description: String, var date: String) : Parcelable  {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(category)
        parcel.writeString(description)
        parcel.writeString(date)
    }

    override fun describeContents(): Int = 0


    companion object CREATOR : Parcelable.Creator<News> {
        override fun createFromParcel(parcel: Parcel): News {
            return News(parcel)
        }

        override fun newArray(size: Int): Array<News> {
            return newArray(size)
        }
    }

}