package ga.almanalfaruq.tetibot.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by almantera on 19/09/17.
 */

data class News(var id: String, var title: String, var category: String, var description: String, var date: String, var url: String = "") : Parcelable  {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
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
        parcel.writeString(url)
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