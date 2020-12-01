package cmsc436.semesterproject.localapparel

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView


class ItemList(private val context: Activity, private var items: List<ApparelItem>) : ArrayAdapter<ApparelItem>(context,
    R.layout.apparel_item, items) {

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.apparel_item, null, true)

        val itemName = listViewItem.findViewById<View>(R.id.itemName) as TextView
        //val imageView = listViewItem.findViewById<View>(R.id.itemImage) as ImageView

        val item = items[position]
        itemName.text = item.itemName
        // SET IMAGE HERE
        //textViewCountry.text = author.authorCountry

        return listViewItem
    }

    companion object {
        private val TAG = "LocalApparel"
    }
}
