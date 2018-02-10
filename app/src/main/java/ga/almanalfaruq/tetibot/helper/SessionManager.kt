package ga.almanalfaruq.tetibot.helper

import android.content.Context

/**
 * Created by almantera on 10/02/18.
 * Used to save old news id
 */
class SessionManager(context: Context) {

    companion object {
        const val PREF_NAME = "SessionManagerTetibot"
        const val KEY_OLDNEWS = "oldnews"
    }

    private val preferences = context.getSharedPreferences(PREF_NAME, 0)
    private val editor = preferences.edit()

    /**
     * Set old news [id] to shared preferences
     * @param id new id
     */
    fun setOldNews(id: Int) {
        editor.putInt(KEY_OLDNEWS, id)
        editor.commit()
    }

    /**
     * Get old id from shared preferences
     * @return old id or it's default value = 0
     */
    fun getOldNews() : Int {
        return preferences.getInt(KEY_OLDNEWS, 0)
    }

}