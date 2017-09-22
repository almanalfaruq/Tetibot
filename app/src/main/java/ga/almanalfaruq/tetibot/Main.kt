package ga.almanalfaruq.tetibot

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.slidingPaneLayout
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class Main : AppCompatActivity(), AnkoLogger {

    private val newsList : ArrayList<News> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sliding_layout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        sliding_layout.addPanelSlideListener(object: SlidingUpPanelLayout.PanelSlideListener{
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
        recView.layoutManager = GridLayoutManager(this, 1)
        recView.adapter = CardAdapter(newsList) {
//            toast("${it.title}")
            txtSliderTitle.text = "${it.category}"
            txtArticle.text = "${it.title}\n${it.date}\n\n${it.description}"
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }

        doAsync {
            var list = RetriveInformation()
            uiThread {
                newsList.addAll(list)
                recView.adapter.notifyDataSetChanged()
            }
        }
        refLayout.setOnRefreshListener {
            doAsync {
                var list = RetriveInformation()
                uiThread {
                    info("test")
                    newsList.clear()
                    newsList.addAll(list)
                    recView.adapter.notifyDataSetChanged()
                    refLayout.isRefreshing = false
                }
            }
        }

    }

    fun RetriveInformation(): ArrayList<News> {
        var news : ArrayList<News> = ArrayList()
        try {
            info("Called")
            val doc : Document = Jsoup.connect("http://sarjana.jteti.ugm.ac.id/akademik/").get()
            // Select table with 5 elements
            val tableRow : Elements = doc.select("table.table-pad > tbody > tr")
            // Select b inside table for the title
            val indexTitle = tableRow.select("b")
            // Select td inside table that equal to 2nd index
            val indexBody = tableRow.select("td:eq(2)")
            for (row in indexBody) {
                info(row.text())
                var body = row.text().split(" ")
                var title = row.select("b").text()
                var category = row.select("span.label").text()
                var desc = ""
                var date = ""
                var titleSize = title.split(" ").size
                var x = 4 + titleSize
                while (x < body.size) {
                    desc += body[x] + " "
                    x++
                }
                x = 1
                while (x < 4) {
                    date += body[x] + " "
                    x++
                }
                info("Title: " + title)
                info("Category: " + category)
                info("Desc: " + desc)
                info("Date: " + date)
                news.add(News(title, category, desc, date))
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
        return news
    }

}
