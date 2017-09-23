package ga.almanalfaruq.tetibot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log

/**
 * Created by almantera on 22/09/17.
 */
class UpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val dailyUpdater = Intent(context, UpdateService::class.java)
        val btNews = intent.getByteArrayExtra("news")
        val news = ParcelableUtil.unmarshall(btNews, News)
        Log.d("Receiver", news.title)
        dailyUpdater.putExtra("news", news)
        context.startService(dailyUpdater)
        Log.d("Receiver: ", "Service started")
    }

}


