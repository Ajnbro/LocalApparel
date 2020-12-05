package cmsc436.semesterproject.localapparel

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
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
import java.text.DecimalFormat

// CITATION: Based upon UIRecyclerView example
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
        // Grab the individual item from the list of items list
        var item = mItems[i]

        // Set the name of the item for the viewHolder
        viewHolder.itemName.text = item.itemName

        // formatter for prices
        val decim = DecimalFormat("0.00")
        // Set the price of the item for the viewHolder

        viewHolder.itemPrice.text = "$" + decim.format(item!!.itemPrice)

        // Sets the visibility of " hourly rate"
        if (item!!.isForRent!!) {
            viewHolder.mPriceRentalText.visibility = View.VISIBLE
        }

        // Gets the item image from Firebase
        val ONE_MEGABYTE = 1024 * 1024.toLong()
        var storageListings: StorageReference = FirebaseStorage.getInstance().getReference("listings/" + item!!.itemID.toString())
        storageListings.getBytes(ONE_MEGABYTE)
            .addOnSuccessListener(OnSuccessListener<ByteArray?> {
                // Sets the item image bitmap as the image received from Firebase
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it?.size as Int)
                viewHolder.itemImage.setImageBitmap(bitmap)
            }).addOnFailureListener(OnFailureListener {
                // An issue occurred and the item image bitmap will be set to the stub image
                val stubBitmap: Bitmap = BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.stub
                )
                viewHolder.itemImage.setImageBitmap(stubBitmap)
            })

        // Set an onClick listener to show the details of any item selected
        viewHolder.itemView.setOnClickListener {
            val listing = mItems[i]
            val intent = Intent(context, ItemDetailsActivity::class.java)

            intent.putExtra("ITEM ID", listing.itemID)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    // Views for each item in the RecyclerView
    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val itemName: TextView = itemView.findViewById(R.id.itemName)
        internal val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        internal val itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
        internal val mPriceRentalText: TextView = itemView.findViewById(R.id.rentalHourly)
    }

}

