package ga.almanalfaruq.tetibot

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import android.app.NotificationManager
import android.content.Context
import com.firebase.jobdispatcher.*
import ga.almanalfaruq.tetibot.adapter.CardAdapter
import ga.almanalfaruq.tetibot.helper.Helper
import ga.almanalfaruq.tetibot.helper.SessionManager
import ga.almanalfaruq.tetibot.model.News
import ga.almanalfaruq.tetibot.service.UpdateService
import kotlin.collections.ArrayList


class Main : AppCompatActivity(), AnkoLogger {

    private val newsList : ArrayList<News> = ArrayList()
    private var list : ArrayList<News> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val helper = Helper()
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
        recView.layoutManager = GridLayoutManager(this, 1) as RecyclerView.LayoutManager?
        recView.adapter = CardAdapter(newsList) {
            txtSliderTitle.text = it.category
            txtTitleSlide.text = it.title
            txtDateSlide.text = it.date
            txtDescriptionSlide.text = it.description
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }

        doAsync {
            list = helper.RetriveInformation()
            uiThread {
                if (list.size > 0 ) {
                    newsList.addAll(list)
                    recView.adapter.notifyDataSetChanged()
                } else {
                    toast("Cannot reach to the server, try again in few second")
                }
            }
        }
        refLayout.setOnRefreshListener {
            doAsync {
                list = helper.RetriveInformation()
                uiThread {
                    if (list.size > 0) {
                        newsList.clear()
                        newsList.addAll(list)
                        recView.adapter.notifyDataSetChanged()
                        refLayout.isRefreshing = false
                    } else {
                        toast("Cannot reach to the server, try again in few second")
                    }
                }
            }
        }
    }

    companion object {
        fun startJobScheduler(context: Context, tempNews: ArrayList<News>) {
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
            val sessionManager = SessionManager(context)
            if (tempNews.size > 0) {
                sessionManager.setOldNews(tempNews[0].id.toInt())
            }
            val job = createJob(dispatcher)
            dispatcher.mustSchedule(job)
        }

        fun createJob(dispatcher: FirebaseJobDispatcher): Job {
            val job : Job = dispatcher.newJobBuilder()
                    .setLifetime(Lifetime.FOREVER)
                    .setService(UpdateService::class.java)
                    .setTag("job_tetibot")
                    .setReplaceCurrent(true)
                    .setTrigger(Trigger.executionWindow(900, 1800))
                    .setConstraints(Constraint.ON_ANY_NETWORK)
                    .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                    .build()
            return job
        }
    }

    override fun onResume() {
        super.onResume()
        val notification = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notification.cancelAll()
    }

    override fun onStop() {
        super.onStop()
        startJobScheduler(this, list)
    }

    override fun onPause() {
        super.onPause()
        startJobScheduler(this, list)
    }

    override fun onBackPressed() {
        if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        } else {
            super.onBackPressed()
        }
    }

}
