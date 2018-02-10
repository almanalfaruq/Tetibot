package ga.almanalfaruq.tetibot.helper

import android.util.Log
import ga.almanalfaruq.tetibot.model.News
import org.jsoup.Jsoup
import java.util.ArrayList

/**
 * Created by almantera on 08/02/18.
 */
class Helper {

    fun RetriveInformation(): ArrayList<News> {
        val news : ArrayList<News> = ArrayList()
        try {
            val doc = Jsoup.connect("http://sarjana.jteti.ugm.ac.id/akademik/").get()
            // Select table with 5 elements
            val tableRow = doc.select("table.table-pad > tbody > tr")
            // Select td inside table that equal to 2nd index
            val indexBody = tableRow.select("td:eq(2)")
            for (row in indexBody) {
                val id = row.parents().first().id()
                Log.d("Helper", id)
                val body = row.text().split(" ")
                val title = row.select("b").text()
                val category = row.select("span.label").text()
                var desc = ""
                var date = ""
                val titleSize = title.split(" ").size
                var x = 4 + titleSize
                var href = row.select("a.btn").attr("href")
                while (x < body.size) {
                    desc += body[x] + " "
                    x++
                }
                x = 1
                while (x < 4) {
                    date += body[x] + " "
                    x++
                }
                if (!href.isEmpty()) {
                    desc = desc.dropLast(9)
                    href = "http://sarjana.jteti.ugm.ac.id" + href
                } else {
                    href = ""
                }
                news.add(News(id, title, category, desc, date, href))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            news.clear()
        }
        return news
    }

}