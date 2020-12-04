package cmsc436.semesterproject.localapparel

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.DecimalFormat
import java.util.*


class ItemDetailsActivity : Activity() {

    lateinit var mNavBar: BottomNavigationView

    var itemID: String? = null
    lateinit var mName: TextView
    lateinit var mImage: ImageView
    lateinit var mDescription: TextView
    lateinit var mPrice: TextView
    lateinit var mIsForSale: CheckBox
    lateinit var mIsForRent: CheckBox
    lateinit var mExpiration: TextView
    lateinit var mLocation: TextView
    lateinit var mUserEmail: TextView

    // Firebase
    private lateinit var databaseListings: DatabaseReference
    private lateinit var storageListings: StorageReference
    var databaseRefreshListingsListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            var item: ApparelItem? = null
            for (postSnapshot in dataSnapshot.children) {
                try {
                    item = postSnapshot.getValue(ApparelItem::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    return
                } finally {
                    if (item!!.itemID == itemID) {
                        mName.text = item!!.itemName
                        mDescription.text = item!!.itemDescription
                        val decim = DecimalFormat("0.00")
                        mPrice.text = "$" + decim.format(item!!.itemPrice)
                        mExpiration.text = item!!.listingExpirationDate
                        mIsForSale.isChecked = item!!.isForSale as Boolean
                        mIsForRent.isChecked = item!!.isForRent as Boolean
                        mUserEmail.text = item!!.userEmail.toString()
                        val geocoder = Geocoder(this@ItemDetailsActivity, Locale.getDefault())
                        val addresses: List<Address> = geocoder.getFromLocation(item!!.itemLatitude as Double, item!!.itemLongitude as Double, 1)
                        val address: String = addresses[0].getAddressLine(0)
                        val cityAndStateNames = address.split(", ")
                        mLocation.text = cityAndStateNames[1] + ", " + cityAndStateNames[2]
                        val ONE_MEGABYTE = 1024 * 1024.toLong()
                        storageListings = FirebaseStorage.getInstance().getReference("listings/" + item!!.itemID.toString())
                        storageListings.getBytes(ONE_MEGABYTE)
                            .addOnSuccessListener(OnSuccessListener<ByteArray?> {
                                val bitmap = BitmapFactory.decodeByteArray(it, 0, it?.size as Int)
                                mImage.setImageBitmap(bitmap)
                            }).addOnFailureListener(OnFailureListener {
                                // Potentially do something
                            })
                    }
                }
            }
        }
        override fun onCancelled(databaseError: DatabaseError) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_details)

        itemID = intent.getStringExtra("ITEM ID")

        mName = findViewById<View>(R.id.itemName) as TextView
        mImage = findViewById<View>(R.id.itemImage) as ImageView
        mDescription = findViewById<View>(R.id.itemDescription) as TextView
        mPrice = findViewById<View>(R.id.itemPrice) as TextView
        mIsForSale = findViewById<View>(R.id.itemForSale) as CheckBox
        mIsForRent = findViewById<View>(R.id.itemForRent) as CheckBox
        mExpiration = findViewById<View>(R.id.itemExpirationDate) as TextView
        mLocation = findViewById<View>(R.id.itemLocation) as TextView
        mUserEmail = findViewById<View>(R.id.userEmail) as TextView

        databaseListings = FirebaseDatabase.getInstance().getReference("listings")
        databaseListings.addListenerForSingleValueEvent(databaseRefreshListingsListener)

        mNavBar = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        mNavBar.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.browse -> {
                    // Respond to navigation item 1 click
                    val intent = Intent(this, FeedActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.add -> {
                    // Respond to navigation item 2 click
                    val intent = Intent(this, AddItemActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.listings -> {
                    // Respond to navigation item 3 click
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        private val TAG = "LocalApparel-ItemDetailsActivity"
    }
}
