package cmsc436.semesterproject.localapparel

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

internal class ItemRecyclerViewAdapter(
    private val context: Activity,
    private val mItems: MutableList<ApparelItem>,
    private val mItemLayout: Int
) : RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder>() {

    // Create ViewHolder which holds a View to be displayed
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(mItemLayout, viewGroup, false)
        return ViewHolder(v)
    }

    // Binding: The process of preparing a child view to display data corresponding to a position within the adapter.
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        var item = mItems[i]
        viewHolder.itemName.text = item.itemName
        viewHolder.itemPrice.text = "$" + item.itemPrice.toString()

        val ONE_MEGABYTE = 1024 * 1024.toLong()
        var storageListings: StorageReference = FirebaseStorage.getInstance().getReference("listings/" + item!!.itemID.toString())
        storageListings.getBytes(ONE_MEGABYTE)
            .addOnSuccessListener(OnSuccessListener<ByteArray?> {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it?.size as Int)
                viewHolder.itemImage.setImageBitmap(bitmap)
            }).addOnFailureListener(OnFailureListener {
                // Potentially do something
            })

        viewHolder.itemView.setOnClickListener(View.OnClickListener { v ->
            val listing = mItems[i]
            val intent = Intent(context, ItemDetailsActivity::class.java)

            intent.putExtra("ITEM ID", listing.itemID)
            context.startActivity(intent)
        })
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val itemName: TextView = itemView.findViewById(R.id.itemName)
        internal val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        internal val itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
    }

}

