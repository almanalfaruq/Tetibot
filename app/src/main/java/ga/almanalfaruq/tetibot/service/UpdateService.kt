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
            val newNews = helper.RetriveInformation()
            val sessionManager = SessionManager(this@UpdateService)
            val tempNewsId = sessionManager.getOldNews().toString()
            uiThread {
                Log.d("UpdateService", newNews.size.toString())
                if (newNews.size > 0) {
                    if (!tempNewsId.equals(newNews[0].id, true)) {
                        sendNotification(this@UpdateService)
                        sessionManager.setOldNews(newNews[0].id.toInt())
                        Log.d("UpdateService", "Not equals ID")
                    }
                }
                Log.d("UpdateService", "Job finished")
                jobFinished(params, true)
            }
        }
    }

    // Creating the notification
    private fun sendNotification(context: Context) {
        val notificationIntent = Intent(context, Main::class.java)
        val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
        val notificationMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "channel_tetibot"
        val channelName = "Tetibot Channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId, channelName, notificationMgr)
        }
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationOreo(context, channelId, contentIntent)
        } else {
            createNotification(context, contentIntent)
        }
        notification.flags = Notification.FLAG_AUTO_CANCEL
        notificationMgr.notify(0, notification)
    }

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationOreo(context: Context, channelId: String,
                                       contentIntent: PendingIntent): Notification {
        return Notification.Builder(context, channelId)
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

    private fun createNotification(context: Context,
                                   contentIntent: PendingIntent): Notification {
        return Notification.Builder(context)
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