package ga.almanalfaruq.tetibot.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.card_adapter.view.*
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat
import ga.almanalfaruq.tetibot.R
import ga.almanalfaruq.tetibot.model.News


/**
 * Created by almantera on 21/09/17.
 */
class CardAdapter(val newsList: List<News>, val listener: (News) -> Unit) : RecyclerView.Adapter<CardAdapter.MyHolder>() {
    // Get the array list size from passed variable
    override fun getItemCount(): Int = newsList.size

    // Inflating layout to card adapter
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_adapter, parent, false))

    // Binding every position from the list
    override fun onBindViewHolder(holder: MyHolder, position: Int) = holder.bind(newsList[position], listener)

    // Get the item type from it's position
    // to make every item unique
    override fun getItemViewType(position: Int): Int {
        return position
    }

    // Get the item id from it's position
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // Binding every item detail to it's view
    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(news: News, listener: (News) -> Unit) = with(itemView) {
            txtId.text = news.id
            txtTitle.text = news.title
            txtDate.text = news.date
            txtDescription.text = news.description
            txtCategory.text = news.category
            // Check if download url is empty
            if (news.url == "") {
                btnDownload.visibility = View.GONE
            } else {
                btnDownload.setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(news.url))
                    itemView.context.startActivity(browserIntent)
                }
            }
            // Change the background color to blue if meets the condition
            if (txtCategory.text.toString().equals("Akademik")) {
                txtCategory.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorBiru))
            }
            // Creating listener to share button
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