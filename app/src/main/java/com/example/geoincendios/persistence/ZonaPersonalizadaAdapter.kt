package com.example.geoincendios.persistence

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.geoincendios.R
import kotlinx.android.synthetic.main.single_guardado.view.*
import kotlinx.android.synthetic.main.single_personalizado.view.*


class ZonaPersonalizadaAdapter(items :MutableList<ZonaRiesgoBD>,var clickListener: ClickListener ):RecyclerView.Adapter<ZonaPersonalizadaAdapter.ViewHolder>() {

    var items : MutableList<ZonaRiesgoBD>? = null

    var viewHolder : ViewHolder? = null

    init {
        this.items = items
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZonaPersonalizadaAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_personalizado,parent,false)
        viewHolder = ViewHolder(view,clickListener)

        return viewHolder!!
    }

    override fun getItemCount(): Int {
        return this.items?.count()!!
    }

    override fun onBindViewHolder(holder: ZonaPersonalizadaAdapter.ViewHolder, position: Int) {
        val item = items?.get(position)

        holder.tv_title?.setText(item?.addres)

    }

    class ViewHolder(view: View, listener: ClickListener): RecyclerView.ViewHolder(view), View.OnClickListener{
        var view = view
        var listener :ClickListener? = null
        var tv_title : TextView? = null
        init {
            tv_title = view.tv_personalizado_titulo
            this.listener = listener
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            this.listener?.onClick(v!!, adapterPosition)
        }

    }


}