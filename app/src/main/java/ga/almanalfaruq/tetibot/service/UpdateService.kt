package ga.almanalfaruq.tetibot.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import ga.almanalfaruq.tetibot.Main
import ga.almanalfaruq.tetibot.helper.Helper
import ga.almanalfaruq.tetibot.helper.SessionManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class UpdateService : JobService() {

    private lateinit var contentIntent: PendingIntent

    override fun onStartJob(params: JobParameters): Boolean {
        checkUpdate(params)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d("UpdateService", "Job stopped")
        return false
    }

    private fun checkUpdate(params: JobParameters) {
        Log.d("UpdateService", "Job started")
        doAsync {
            // Get the newest news from the web
            val helper = Helper()
            val newNews = helper.retriveNewsFromWebsite()
            val sessionManager = SessionManager(this@UpdateService)
            val tempNewsId = sessionManager.getOldNews().toString()
            uiThread {
                Log.d("UpdateService", newNews.size.toString())
                if (newNews.size > 0) {
                    if (!tempNewsId.equals(newNews[0].id, true)) {
                        sendNotification()
                        sessionManager.setOldNews(newNews[0].id.toInt())
                        Log.d("UpdateService", "Not equals ID")
                    }
                }
                Log.d("UpdateService", "Job finished")
                jobFinished(params, true)
            }
        }
    }

    // Send the notification to phone
    private fun sendNotification() {
        val notificationIntent = Intent(this, Main::class.java)
        contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notificationMgr = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "channel_tetibot"
        val channelName = "Tetibot Channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId, channelName, notificationMgr)
        }
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationOreo(channelId)
        } else {
            createNotification()
        }
        notification.flags = Notification.FLAG_AUTO_CANCEL
        notificationMgr.notify(0, notification)
    }

    // Used for creating notification channel in API >= 26
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String,
                                          notificationMgr: NotificationManager) {
        val notificationChannel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationMgr.createNotificationChannel(notificationChannel)
    }

    // Used for creating notification in API >= 26
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationOreo(channelId: String): Notification {
        return Notification.Builder(this, channelId)
                // Notification title
                .setContentTitle("Tetibot")
                // Notification description
                .setContentText("Update Dari Web Akademik DTETI")
                // Notification icon
                .setSmallIcon(android.R.drawable.star_on)
                // Notification intent -> open the apps
                .setContentIntent(contentIntent)
                .build()
    }

    // Used for creating notification in API < 26
    private fun createNotification(): Notification {
        return Notification.Builder(this)
                // Notification title
                .setContentTitle("Tetibot")
                // Notification description
                .setContentText("Update Dari Web Akademik DTETI")
                // Notification icon
                .setSmallIcon(android.R.drawable.star_on)
                // Notification intent -> open the apps
                .setContentIntent(contentIntent)
                .build()
    }
}