package ga.almanalfaruq.tetibot.helper

import ga.almanalfaruq.tetibot.model.News
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.ArrayList

/**
 * Created by almantera on 08/02/18.
 */
class Helper {

    /**
     * Get news from DTETI's website
     * @return list of [News] from website
     */
    fun retriveNewsFromWebsite(): ArrayList<News> {
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

    /**
     * Creating formatted news so it can be used with the [News] model
     * @param oneNews one news from many news (a table row)
     * @return formatted [News]
     */
    private fun createFormattedNews(oneNews: Element): News {
        val newsId = getNewsId(oneNews)
        val newsTitle = getNewsTitle(oneNews)
        val newsCategory = getNewsCategory(oneNews)
        val newsDescription = getNewsDescription(oneNews)
        val newsDate = getNewsDate(oneNews)
        val newsUrl = getNewsUrl(oneNews)
        return News(newsId, newsTitle, newsCategory, newsDescription, newsDate, newsUrl)
    }

    /**
     * Get news' id from a table row
     * @param oneNews one news from many news (a table row)
     * @return news' id
     */
    private fun getNewsId(oneNews: Element): String {
        return oneNews.parents().first().id()
    }

    /**
     * Get news' title from a table row
     * @param oneNews one news from many news (a table row)
     * @return news' title
     */
    private fun getNewsTitle(oneNews: Element): String {
        return oneNews.select("b").text()
    }

    /**
     * Get news' category from a table row
     * @param oneNews one news from many news (a table row)
     * @return news' category
     */
    private fun getNewsCategory(oneNews: Element): String {
        return oneNews.select("span.label").text()
    }

    /**
     * Get news' description from a table row
     * @param oneNews one news from many news (a table row)
     * @return news' description
     */
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

    /**
     * Get news' date from a table row
     * @param oneNews one news from many news (a table row)
     * @return news' date
     */
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

    /**
     * Get news' download url if there is any download url from a table row
     * @param oneNews one news from many news (a table row)
     * @return news' download url
     */
    private fun getNewsUrl(oneNews: Element): String {
        val linkDownload = oneNews.select("a.btn").attr("href")
        return if (!linkDownload.isEmpty()) {
            "http://sarjana.jteti.ugm.ac.id" + linkDownload
        } else {
            ""
        }
    }

    /**
     * Get news' body text (title, date, description are included in this body text)
     * from a table row
     * @param oneNews one news from many news (a table row)
     * @return news' body text (per word)
     */
    private fun getBodyText(oneNews: Element): List<String> {
        return oneNews.text().split(" ")
    }

}