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
        // If the panel slide down
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
        // Get the data from web
        doAsync {
            list = helper.RetriveInformation()
            uiThread {
                if (list.size > 0 ) {
                    newsList.addAll(list)
                    recView.adapter.notifyDataSetChanged()
                } else {
                    toast("Cannot reach to the server, try again in a few second")
                }
            }
        }
        // Swipe refresh code
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
                        toast("Cannot reach to the server, try again in a few second")
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
                    // Set it's lifetime
                    .setLifetime(Lifetime.FOREVER)
                    // What's the class it should use to create the job
                    .setService(UpdateService::class.java)
                    // Set tag to identify it's id
                    .setTag("job_tetibot")
                    // If there is another job created with the same tag, it will be replaced
                    .setReplaceCurrent(true)
                    // Trigger the job between 900-1800 seconds (10-30 minutes)
                    .setTrigger(Trigger.executionWindow(900, 1800))
                    // Start the job if there's a network connection
                    .setConstraints(Constraint.ON_ANY_NETWORK)
                    // How the job retrying it's job (?) -> 30s, 60s, 90s, 120s, etc.
                    .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                    .build()
            return job
        }
    }

    override fun onResume() {
        super.onResume()
        // Clear notification if resume the app
        val notification = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notification.cancelAll()
    }

    override fun onStop() {
        super.onStop()
        // Start the job scheduler if application stoped
        startJobScheduler(this, list)
    }

    override fun onPause() {
        super.onPause()
        // Start the job scheduler if application paused
        startJobScheduler(this, list)
    }

    override fun onBackPressed() {
        // If back pressed when slider is expanded, the hide the slider
        if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        } else {
            super.onBackPressed()
        }
    }

}
