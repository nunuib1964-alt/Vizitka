package com.example.bizcards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bizcards.databinding.ItemCardBinding

class CardAdapter(
    private val onClick: (Card) -> Unit,
    private val onLongClick: (Card) -> Unit
) : ListAdapter<Card, CardAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(a: Card, b: Card) = a.id == b.id
        override fun areContentsTheSame(a: Card, b: Card) = a == b
    }

    class VH(val b: ItemCardBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        VH(ItemCardBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val c = getItem(pos)
        h.b.tvName.text = c.name
        h.b.tvCompany.text = c.company.ifBlank { "—" }
        h.b.tvPhone.text = c.phone.ifBlank { "—" }
        h.b.root.setOnClickListener { onClick(c) }
        h.b.root.setOnLongClickListener { onLongClick(c); true }
    }
}
