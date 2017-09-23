package ga.almanalfaruq.tetibot

import java.io.Serializable

/**
 * Created by almantera on 19/09/17.
 */
data class News(var title: String, var category: String, var description: String, var date: String) : Serializable {
    constructor() : this("", "", "", "")
}