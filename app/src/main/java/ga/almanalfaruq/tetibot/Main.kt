package ga.almanalfaruq.tetibot

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.slidingPaneLayout
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*
import kotlin.collections.ArrayList


class Main : AppCompatActivity(), AnkoLogger {

    private val newsList : ArrayList<News> = ArrayList()
    private lateinit var list : ArrayList<News>
    private lateinit var context : Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        sliding_layout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        sliding_layout.addPanelSlideListener(object: SlidingUpPanelLayout.PanelSlideListener{
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                
            }

            override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    sliding_layout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
                }
            }

        })
        recView.layoutManager = GridLayoutManager(this, 1)
        recView.adapter = CardAdapter(newsList) {
            txtSliderTitle.text = it.category
            txtTitleSlide.text = it.title
            txtDateSlide.text = it.date
            txtDescriptionSlide.text = it.description
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }

        doAsync {
            list = UpdateService.RetriveInformation()
            uiThread {
                if (list.size > 0 ) {
                    newsList.addAll(list)
                    recView.adapter.notifyDataSetChanged()
                    UpdateService.setRecurringAlarm(context, list[0])
                } else {
                    toast("Cannot reach to the server, try again in few second")
                }
            }
        }
        refLayout.setOnRefreshListener {
            doAsync {
                list = UpdateService.RetriveInformation()
                uiThread {
                    if (list.size > 0) {
                        newsList.clear()
                        newsList.addAll(list)
                        recView.adapter.notifyDataSetChanged()
                        refLayout.isRefreshing = false
                        UpdateService.setRecurringAlarm(context, list[0])
                    } else {
                        toast("Cannot reach to the server, try again in few second")
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        } else {
            super.onBackPressed()
        }
    }

}
