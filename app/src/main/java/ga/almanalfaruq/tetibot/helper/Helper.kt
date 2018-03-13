package ga.almanalfaruq.tetibot.helper

import ga.almanalfaruq.tetibot.model.News
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
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
            val newsTable = doc.select("table.table-pad > tbody > tr")
            // Select td inside table that equal to 2nd index
            val manyNews = newsTable.select("td:eq(2)")
            for (oneNews in manyNews) {
                news.add(createFormattedNews(oneNews))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            news.clear()
        }
        return news
    }

    private fun createFormattedNews(oneNews: Element): News {
        val newsId = getNewsId(oneNews)
        val newsTitle = getNewsTitle(oneNews)
        val newsCategory = getNewsCategory(oneNews)
        val newsDescription = getNewsDescription(oneNews)
        val newsDate = getNewsDate(oneNews)
        val newsUrl = getNewsUrl(oneNews)
        return News(newsId, newsTitle, newsCategory, newsDescription, newsDate, newsUrl)
    }

    private fun getNewsId(oneNews: Element): String {
        return oneNews.parents().first().id()
    }

    private fun getNewsTitle(oneNews: Element): String {
        return oneNews.select("b").text()
    }

    private fun getNewsCategory(oneNews: Element): String {
        return oneNews.select("span.label").text()
    }

    private fun getNewsDescription(oneNews: Element): String {
        val bodyText = getBodyText(oneNews)
        val title = getNewsTitle(oneNews)
        val titleSize = title.split(" ").size
        var x = 4 + titleSize
        var description = ""
        while (x < bodyText.size) {
            description += bodyText[x] + " "
            x++
        }
        val linkDownload = getNewsUrl(oneNews)
        return if (linkDownload.isEmpty()) {
            description
        } else {
            description.dropLast(9)
        }
    }

    private fun getNewsDate(oneNews: Element): String {
        val bodyText = getBodyText(oneNews)
        var x = 1
        var date = ""
        while (x < 4) {
            date += bodyText[x] + " "
            x++
        }
        return date
    }

    private fun getNewsUrl(oneNews: Element): String {
        val linkDownload = oneNews.select("a.btn").attr("href")
        return if (!linkDownload.isEmpty()) {
            "http://sarjana.jteti.ugm.ac.id" + linkDownload
        } else {
            ""
        }
    }

    private fun getBodyText(oneNews: Element): List<String> {
        return oneNews.text().split(" ")
    }

}