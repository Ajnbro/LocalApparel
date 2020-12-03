package cmsc436.semesterproject.localapparel

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage


class ItemList(private val context: Activity, private var items: List<ApparelItem>) : ArrayAdapter<ApparelItem>(context,
    R.layout.apparel_item, items) {

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewItem = inflater.inflate(R.layout.apparel_item, null, true)

        val itemName = listViewItem.findViewById<View>(R.id.itemName) as TextView
        val imageView = listViewItem.findViewById<View>(R.id.itemImage) as ImageView
        val itemPrice = listViewItem.findViewById<View>(R.id.itemPrice) as TextView

        val item = items[position]
        itemName.text = item.itemName
        itemPrice.text = "$" + item.itemPrice
        // SET IMAGE HERE
        val ONE_MEGABYTE = 1024 * 1024.toLong()
        val storageListings = FirebaseStorage.getInstance().getReference("listings/" + item!!.itemID.toString())
        storageListings.getBytes(ONE_MEGABYTE)
            .addOnSuccessListener(OnSuccessListener<ByteArray?> {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it?.size as Int)
                imageView.setImageBitmap(bitmap)
            }).addOnFailureListener(OnFailureListener {
                // set to stub image
                val bitmap =  BitmapFactory.decodeResource(parent.resources,
                    R.drawable.stub)
                imageView.setImageBitmap(bitmap)
            })

        return listViewItem
    }

    companion object {
        private val TAG = "LocalApparel"
    }
}
