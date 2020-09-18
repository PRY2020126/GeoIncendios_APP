package com.example.geoincendios.persistence

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.geoincendios.R
import kotlinx.android.synthetic.main.single_guardado.view.*


class ZonaRiesgoAdapter(items :MutableList<ZonaRiesgoBD>,var clickListener: ClickListener ):RecyclerView.Adapter<ZonaRiesgoAdapter.ViewHolder>() {

    var items : MutableList<ZonaRiesgoBD>? = null

    var viewHolder : ViewHolder? = null

    init {
        this.items = items
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZonaRiesgoAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_guardado,parent,false)
        viewHolder = ViewHolder(view,clickListener)

        return viewHolder!!
    }

    override fun getItemCount(): Int {
        return this.items?.count()!!
    }

    override fun onBindViewHolder(holder: ZonaRiesgoAdapter.ViewHolder, position: Int) {
        val item = items?.get(position)

        holder.tv_title?.setText(item?.addres)
        when(item!!.riesgo)
        {
            1 -> holder.iv_zona_riesgo?.setBackgroundResource(R.mipmap.ic_riesgo_foreground)
            2 -> holder.iv_zona_riesgo?.setBackgroundResource(R.mipmap.ic_riesgo_medio_foreground)
            3 -> holder.iv_zona_riesgo?.setBackgroundResource(R.mipmap.ic_riesgo_bajo_foreground)
        }
        //holder.tv_lon?.setText(item?.lng)
        //holder.tv_lat?.setText(item?.lat)
    }

    class ViewHolder(view: View, listener: ClickListener): RecyclerView.ViewHolder(view), View.OnClickListener{
        var view = view
        var listener :ClickListener? = null
        var tv_title : TextView? = null
        var iv_zona_riesgo : ImageView? = null
        //var tv_lat : TextView? = null
        //var tv_lon : TextView? = null
        init {
            tv_title = view.tv_guardado_titulo
            //tv_lat = view.tv_guardado_latitud
            //tv_lon = view.tv_guardado_longitud
            iv_zona_riesgo = view.iv_color_riesgo
            this.listener = listener
            view.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            this.listener?.onClick(v!!, adapterPosition)
        }

    }


}