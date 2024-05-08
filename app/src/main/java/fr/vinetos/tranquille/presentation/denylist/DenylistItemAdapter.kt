package fr.vinetos.tranquille.presentation.denylist;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import fr.vinetos.tranquille.R
import fr.vinetos.tranquille.data.DenylistItem

class DenylistItemAdapter(
    private val denylist: List<DenylistItem>
) : RecyclerView.Adapter<DenylistItemAdapter.DenylistItemViewHolder>() {

    class DenylistItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DenylistItemViewHolder {
        return DenylistItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.blacklist_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: DenylistItemViewHolder, position: Int) {
        // When a new item is visible
        val currentDenylistItem = denylist[position]
        holder.itemView.apply {
            findViewById<TextView>(R.id.name).text = currentDenylistItem.name
            findViewById<TextView>(R.id.pattern).text = currentDenylistItem.pattern
            findViewById<TextView>(R.id.stats).text = "No stats available"
            toggleVisibility(findViewById(R.id.errorIcon), !currentDenylistItem.invalid.toBoolean())
            setOnClickListener {

            }
        }
    }

    override fun getItemCount(): Int = denylist.size

    override fun getItemId(position: Int): Long = position.toLong()

    private fun toggleVisibility(invalidImage: AppCompatImageView, valid: Boolean) {
        if (valid) {
            invalidImage.visibility = View.GONE
        } else {
            invalidImage.visibility = View.VISIBLE
        }
    }

    private fun Long.toBoolean() = this == 1.toLong()
}