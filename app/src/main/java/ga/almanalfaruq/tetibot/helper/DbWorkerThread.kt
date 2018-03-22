package ga.almanalfaruq.tetibot.helper

import android.os.Handler
import android.os.HandlerThread

/**
 * Created by almantera on 20/03/18.
 */
class DbWorkerThread(threadName: String) : HandlerThread(threadName) {

    private lateinit var workerThread: Handler

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        workerThread = Handler(looper)
    }

    fun postTask(task: Runnable) {
        workerThread.post(task)
    }

}