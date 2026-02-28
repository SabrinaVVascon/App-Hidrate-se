package br.com.hidrateseplus.app.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.hidrateseplus.app.databinding.ItemHistoryDayBinding

class HistoryAdapter(
    private val items: List<HistoryDay>
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    class VH(val binding: ItemHistoryDayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHistoryDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvDate.text = item.date
        holder.binding.tvTotal.text = "Total: ${item.total}"
    }

    override fun getItemCount() = items.size
}