package ga.almanalfaruq.tetibot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Created by almantera on 22/09/17.
 */
class UpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val dailyUpdater = Intent(context, UpdateService::class.java)
        dailyUpdater.putExtra("news", intent.getSerializableExtra("news"))
        context.startService(dailyUpdater)
        Log.d("Receiver: ", "Service started")
    }

}


