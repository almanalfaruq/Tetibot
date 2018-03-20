package ga.almanalfaruq.tetibot.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import ga.almanalfaruq.tetibot.model.News

/**
 * Created by almantera on 20/03/18.
 */

@Dao
interface NewsDao {

    @Query("SELECT * FROM news")
    fun getAll(): List<News>

    @Query("SELECT COUNT(*) FROM news")
    fun countNews(): Int

    @Insert(onConflict = REPLACE)
    fun insert(news: ArrayList<News>)

    @Delete
    fun delete(news: News)
}