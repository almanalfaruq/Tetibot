package ga.almanalfaruq.tetibot

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.card_adapter.view.*
import android.content.Intent
import android.support.v4.content.ContextCompat


/**
 * Created by almantera on 21/09/17.
 */
class CardAdapter(val newsList: List<News>, val listener: (News) -> Unit) : RecyclerView.Adapter<CardAdapter.MyHolder>() {
    override fun getItemCount(): Int = newsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_adapter, parent, false))

    override fun onBindViewHolder(holder: MyHolder, position: Int) = holder.bind(newsList[position], listener)

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(news: News, listener: (News) -> Unit) = with(itemView) {
            txtId.text = news.id
            txtTitle.text = news.title
            txtDate.text = news.date
            txtDescription.text = news.description
            txtCategory.text = news.category
            if (txtCategory.text.toString().equals("Akademik")) {
                txtCategory.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorBiru))
            }
            btnShare.setOnClickListener {
                val message = "[Info " + txtCategory.text.toString() + "]" +
                        "\n" + txtTitle.text.toString() + "\n" +
                        txtDate.text.toString() + "\n\n" +
                        txtDescription.text.toString()
                val share = Intent(Intent.ACTION_SEND)
                share.type = "text/plain"
                share.putExtra(Intent.EXTRA_TEXT, message)

                itemView.context.startActivity(Intent.createChooser(share, "Share to"))
            }
            setOnClickListener { listener(news) }
        }
    }
}