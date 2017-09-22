package ga.almanalfaruq.tetibot

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.card_adapter.view.*

/**
 * Created by almantera on 21/09/17.
 */
class CardAdapter(val newsList: List<News>, val listener: (News) -> Unit) : RecyclerView.Adapter<CardAdapter.MyHolder>() {
    override fun getItemCount(): Int = newsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_adapter, parent, false))

    override fun onBindViewHolder(holder: MyHolder, position: Int) = holder.bind(newsList[position], listener)

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(news: News, listener: (News) -> Unit) = with(itemView) {
            txtTitle.text = news.title
            txtDate.text = news.date
            txtDescription.text = news.description
            btnShare.setOnClickListener {
                
            }
            setOnClickListener { listener(news) }
        }
    }
}