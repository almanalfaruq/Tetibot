package ga.almanalfaruq.tetibot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class UpdateService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        checkUpdate(params)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    private fun checkUpdate(params: JobParameters) {
        Log.d("UpdateService", "Job started")
        doAsync {
            val helper = Helper()
            val newNews = helper.RetriveInformation()
            val tempNewsId = params.extras?.getString("tempNewsId")
            uiThread {
                Log.d("UpdateService", newNews.size.toString())
                if (tempNewsId != null && newNews.size > 0) {
                    if (!tempNewsId.equals(newNews[0].id, true)) {
                        sendNotification(this@UpdateService)
                        Log.d("UpdateService", "Same ID")
                    }
                } else {
                    sendNotification(this@UpdateService)
                    Log.d("UpdateService", "No ID")
                }
                Log.d("UpdateService", "Job finished")
                jobFinished(params, true)
            }
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
            notification.flags = Notification.FLAG_AUTO_CANCEL
            notificationMgr.notify(0, notification)
        } else {
            val notification = Notification.Builder(context)
                    .setContentTitle("Tetibot")
                    .setContentText("Update Dari Web Akademik DTETI")
                    .setSmallIcon(android.R.drawable.star_on)
                    .setContentIntent(contentIntent)
                    .build()
            notification.flags = Notification.FLAG_AUTO_CANCEL
            notificationMgr.notify(0, notification)
        }
    }
}