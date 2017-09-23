package ga.almanalfaruq.tetibot

import android.app.*
import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import android.graphics.Color
import android.os.Build
import android.os.Parcelable
import android.util.Log
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.util.*


/**
 * Created by almantera on 22/09/17.
 */
class UpdateService : IntentService("Update-Service") {

    private lateinit var context : Context

    fun UpdateService() {

    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }

    override fun onHandleIntent(intent: Intent) {
        val btTemp = intent.getByteArrayExtra("news")
        var temp = ParcelableUtil.unmarshall(btTemp, News)
        doAsync {
            val list = RetriveInformation()
            uiThread {
                if (list.size > 0) {
                    if (temp != list.get(0)) {
                        sendNotification(context)
                        temp = list.get(0)
                    }
                    setRecurringAlarm(context, temp)
                } else {
                    toast("Cannot reach to the server, try to connect again")
                    setRecurringAlarm(context, temp)
                }
            }
        }
    }

    companion object {
        private val TAG = UpdateService.javaClass.simpleName

        fun RetriveInformation(): ArrayList<News> {
            var news : ArrayList<News> = ArrayList()
            try {
                val doc = Jsoup.connect("http://sarjana.jteti.ugm.ac.id/akademik/").get()
                // Select table with 5 elements
                val tableRow = doc.select("table.table-pad > tbody > tr")
                // Select td inside table that equal to 2nd index
                val indexBody = tableRow.select("td:eq(2)")
                for (row in indexBody) {
                    val body = row.text().split(" ")
                    val title = row.select("b").text()
                    val category = row.select("span.label").text()
                    var desc = ""
                    var date = ""
                    val titleSize = title.split(" ").size
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
                Log.d(TAG,"Title: " + title)
                Log.d(TAG, "Category: " + category)
                Log.d(TAG, "Desc: " + desc)
                Log.d(TAG, "Date: " + date)
                    news.add(News(title, category, desc, date))
                }
            } catch (e : Exception) {
                e.printStackTrace()
            }
            return news
        }

        fun setRecurringAlarm(context: Context, news: News) {

            val updateTime = Calendar.getInstance()
            updateTime.setTimeZone(TimeZone.getDefault())
            updateTime.add(Calendar.MINUTE, 10)
            val downloader = Intent(context, UpdateReceiver::class.java)
            val pcNews = ParcelableUtil.marshall(news)
            downloader.putExtra("news", pcNews)
            Log.d(TAG, news.title)
            downloader.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val pendingIntent = PendingIntent.getBroadcast(context, 0, downloader, PendingIntent.FLAG_CANCEL_CURRENT)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent)

            Log.d(TAG, "Set alarmManager.setRepeating to: " + updateTime.getTime().toString())

        }
    }

    private fun sendNotification(context: Context) {
        val notificationIntent = Intent(context, Main::class.java)
        val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
        val notificationMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "channel_tetibot"
        val channelName = "Tetibot Channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationMgr.createNotificationChannel(notificationChannel)
            val notification = Notification.Builder(context, channelId)
                    .setContentTitle("Tetibot")
                    .setContentText("Update Dari Web Akademik DTETI")
                    .setSmallIcon(android.R.drawable.star_on)
                    .setContentIntent(contentIntent)
                    .build()
            notificationMgr.notify(0, notification)
        } else {
            val notification = Notification.Builder(context)
                    .setContentTitle("Tetibot")
                    .setContentText("Update Dari Web Akademik DTETI")
                    .setSmallIcon(android.R.drawable.star_on)
                    .setContentIntent(contentIntent)
                    .build()
            notificationMgr.notify(0, notification)
        }
    }

}