package com.example.bluetoothtesting

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Giuseppe Gargani
 */

class DevicesRecyclerViewAdapter(val mDeviceList: List<DeviceData>, val context: Context) :
    RecyclerView.Adapter<DevicesRecyclerViewAdapter.VH>() {


     var listener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(context).inflate(R.layout.recyclerview_single_item, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder?.label?.text = mDeviceList[position].deviceName ?: mDeviceList[position].deviceHardwareAddress
    }

    override fun getItemCount(): Int {
        return mDeviceList.size
    }

    /* c'è anche un metodo per inizializzare ViewHolder!!
    QUALE E' IL METODO CORRETTO PER METTERE UN LISTENER SU RECYCLERVIEW?
     */
    inner class VH(itemView: View?) : RecyclerView.ViewHolder(itemView!!){

        var label: TextView? = itemView?.findViewById(R.id.largeLabel)

        init {
            itemView?.setOnClickListener{
                listener?.itemClicked(mDeviceList[adapterPosition])
            }
        }
    }

    fun setItemClickListener(listener: ItemClickListener){
        this.listener = listener
    }

    /*THIS IS AN INTERFACE THAT HAS BEEN IMPLEMENTED DIRECTLY INSIDE ACTIVITY OR FRAGMENT THAT USE THIS ADAPTER
     */
    interface ItemClickListener{
        fun itemClicked(deviceData: DeviceData)
    }
}