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
import android.support.design.widget.Snackbar
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
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbWorkerThread = DbWorkerThread("dbWorkerThread")
        dbWorkerThread.start()

        newsDb = NewsDatabase.getInstance(this)

        initializeComponent()

        refresh_layout.setOnRefreshListener {
            getNewsFromWebsite()
        }
    }

    private fun initializeComponent() {
        settingUpSnackbar()
        settingUpSlidingLayout()
        settingUpRecyclerView()
        getNewsFromDb()
        getNewsFromWebsite()
    }

    private fun settingUpSnackbar() {
        snackbar = Snackbar.make(parent_layout, "Data not updated", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("UPDATE", {
            getNewsFromWebsite()
        })
    }

    private fun settingUpSlidingLayout() {
        hideSlidingLayout()
        // If the panel slide down
        sliding_layout.addPanelSlideListener(object: SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {

            }

            override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    hideSlidingLayout()
                }
            }

        })
    }

    private fun settingUpRecyclerView() {
        recycler_view.layoutManager = GridLayoutManager(this, 1) as RecyclerView.LayoutManager?
        recycler_view.adapter = CardAdapter(newsList) {
            slider_title.text = it.category
            text_title.text = it.title
            text_date.text = it.date
            text_description.text = it.description
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }
    }

    private fun getNewsFromWebsite() {
        if (!refresh_layout.isRefreshing) {
            refresh_layout.isRefreshing = true
        }
        doAsync {
            list = helper.retriveNewsFromWebsite()
            uiThread {
                if (list.size > 0) {
                    showDataFromWebsite(list)
                } else {
                    cannotRetriveData()
                }
            }
        }
    }

    private fun showDataFromWebsite(news: ArrayList<News>) {
        insertNewsToRecView()
        insertNewsToDb(news)
        snackbar.dismiss()
        refresh_layout.isRefreshing = false
        toast("Updated from website")
    }

    private fun cannotRetriveData() {
        getNewsFromDb()
        snackbar.show()
        refresh_layout.isRefreshing = false
        toast("Cannot reach to the server, try again in a few second")
    }

    // Inserting data from website to recycler view
    private fun insertNewsToRecView() {
        newsList.clear()
        newsList.addAll(list)
        recycler_view.adapter.notifyDataSetChanged()
    }

    private fun getNewsFromDb() {
        val task = Runnable {
            list = newsDb?.newsDao()?.getAll() as ArrayList<News>
            uiHandler.post({
                if (list.isEmpty()) {
                    toast("Local data is empty, please connect to the internet to fill it")
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
        val notification = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notification.cancelAll()
    }

    override fun onStop() {
        startJobScheduler(this, list)
        super.onStop()
    }

    override fun onPause() {
        startJobScheduler(this, list)
        super.onPause()
    }

    override fun onDestroy() {
        NewsDatabase.destroyInstance()
        dbWorkerThread.quit()
        startJobScheduler(this, list)
        super.onDestroy()
    }

    override fun onBackPressed() {
        // If back pressed when slider is expanded, the hide the slider
        if (isSlidingLayoutExpanded()) {
            hideSlidingLayout()
        } else {
            super.onBackPressed()
        }
    }

    private fun isSlidingLayoutExpanded(): Boolean {
        return sliding_layout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED
    }

    private fun hideSlidingLayout() {
        sliding_layout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
    }

}
