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
import android.os.Handler
import com.firebase.jobdispatcher.*
import ga.almanalfaruq.tetibot.adapter.CardAdapter
import ga.almanalfaruq.tetibot.helper.DbWorkerThread
import ga.almanalfaruq.tetibot.helper.Helper
import ga.almanalfaruq.tetibot.helper.NewsDatabase
import ga.almanalfaruq.tetibot.helper.SessionManager
import ga.almanalfaruq.tetibot.model.News
import ga.almanalfaruq.tetibot.service.UpdateService
import kotlin.collections.ArrayList


class Main : AppCompatActivity(), AnkoLogger {

    private val newsList = ArrayList<News>()
    private var list = ArrayList<News>()
    private val helper = Helper()
    private var newsDb: NewsDatabase? = null
    private lateinit var dbWorkerThread: DbWorkerThread
    private val uiHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbWorkerThread = DbWorkerThread("dbWorkerThread")
        dbWorkerThread.start()

        newsDb = NewsDatabase.getInstance(this)

        settingUpSlidingLayout()
        settingUpRecyclerView()
        getNewsFromDb()
        // Refreshing the refresh layout when first time opened
        refLayout.isRefreshing = true
        getNewsFromWebsite()
        // Swipe refresh code
        refLayout.setOnRefreshListener {
            getNewsFromWebsite()
        }
    }

    /**
     * Setting the sliding layout
     */
    private fun settingUpSlidingLayout() {
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
    }

    /**
     * Setting the recycler view
     */
    private fun settingUpRecyclerView() {
        recView.layoutManager = GridLayoutManager(this, 1) as RecyclerView.LayoutManager?
        recView.adapter = CardAdapter(newsList) {
            txtSliderTitle.text = it.category
            txtTitleSlide.text = it.title
            txtDateSlide.text = it.date
            txtDescriptionSlide.text = it.description
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }
    }

    /**
     * Get data from website
     */
    private fun getNewsFromWebsite() {
        doAsync {
            list = helper.retriveNewsFromWebsite()
            uiThread {
                if (list.size > 0) {
                    insertNewsToRecView()
                    insertNewsToDb(list)
                    toast("Updated from website")
                } else {
                    toast("Cannot reach to the server, try again in a few second")
                }
            }
        }
    }

    // Inserting data from website to recycler view
    private fun insertNewsToRecView() {
        newsList.clear()
        newsList.addAll(list)
        recView.adapter.notifyDataSetChanged()
        refLayout.isRefreshing = false
    }

    private fun getNewsFromDb() {
        val task = Runnable {
            list = newsDb?.newsDao()?.getAll() as ArrayList<News>
            uiHandler.post({
                if (list.isEmpty()) {
                    toast("Local data is empty, please connect to the internet to fill it")
                    refLayout.isRefreshing = false
                } else {
                    insertNewsToRecView()
                }
            })
        }
        dbWorkerThread.postTask(task)
    }

    private fun insertNewsToDb(news: ArrayList<News>) {
        val task = Runnable { newsDb?.newsDao()?.insert(news) }
        dbWorkerThread.postTask(task)
    }

    companion object {
        // Starting job scheduler
        fun startJobScheduler(context: Context, tempNews: ArrayList<News>) {
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
            val sessionManager = SessionManager(context)
            if (tempNews.size > 0) {
                sessionManager.setOldNews(tempNews[0].id.toInt())
            }
            val job = createJob(dispatcher)
            dispatcher.mustSchedule(job)
        }

        // Creating job scheduler with some configuration
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
        NewsDatabase.destroyInstance()
        dbWorkerThread.quit()
        // Start the job scheduler if application paused
        startJobScheduler(this, list)
        super.onStop()
    }

    override fun onPause() {
        NewsDatabase.destroyInstance()
        dbWorkerThread.quit()
        // Start the job scheduler if application paused
        startJobScheduler(this, list)
        super.onPause()
    }

    override fun onDestroy() {
        NewsDatabase.destroyInstance()
        dbWorkerThread.quit()
        // Start the job scheduler if application paused
        startJobScheduler(this, list)
        super.onDestroy()
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
