package io.github.nikulshinip.cottage.ui.temps

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import io.github.nikulshinip.cottage.R
import io.github.nikulshinip.cottage.obj.Temp
import kotlinx.android.synthetic.main.temp_item.view.*

class TempsAdapter(val temps: List<Temp>,
                   val context: Context) : BaseAdapter(){

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {

        var holder: Holder
        var rowView = view
        if (rowView == null) {
            rowView = LayoutInflater.from(context).inflate(R.layout.temp_item, parent, false)
            holder = Holder(rowView.number, rowView.title, rowView.temp)
            rowView.setTag(holder)
        }else{
            holder = rowView.getTag() as Holder
        }

        holder.apply {
            id.text = (position + 1).toString()
            title.text = temps[position].title
            temp.text = temps[position].temp.toString() + "°C"
            if (temps[position].temp > 100)
                temp.setTextColor(Color.RED)
        }
        return rowView!!
    }

    override fun getItem(p0: Int): Any = temps[p0]

    override fun getItemId(p0: Int): Long = p0.toLong()
    override fun getCount(): Int = temps.size

    private class Holder(val id: TextView,
                         val title: TextView,
                         val temp: TextView)
}