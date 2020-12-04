package cmsc436.semesterproject.localapparel

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
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

    // Item variables
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

    // Firebase variables
    private lateinit var databaseListings: DatabaseReference
    private lateinit var storageListings: StorageReference
    var databaseRefreshListingsListener: ValueEventListener = object : ValueEventListener {
        // Sets the values of the item detail variables
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
                        // Sets the item name
                        mName.text = item.itemName

                        // Sets the item description
                        mDescription.text = item.itemDescription

                        // Sets the item price (and visibility of " hourly rate")
                        val decim = DecimalFormat("0.00")
                        if (item.isForRent!!) {
                            mPrice.text = "$" + decim.format(item.itemPrice) + " hourly rate"
                        } else {
                            mPrice.text = "$" + decim.format(item.itemPrice)
                        }

                        // Sets the item expiration date
                        mExpiration.text = item.listingExpirationDate

                        // Sets the item's sale/rent status
                        mIsForSale.isChecked = item.isForSale as Boolean
                        mIsForRent.isChecked = item.isForRent as Boolean

                        // Sets the seller's contact information
                        mUserEmail.text = item.userEmail.toString()

                        // Sets the seller's/item's location
                        val geocoder = Geocoder(this@ItemDetailsActivity, Locale.getDefault())
                        val addresses: List<Address> = geocoder.getFromLocation(item.itemLatitude as Double, item.itemLongitude as Double, 1)
                        val address: String = addresses[0].getAddressLine(0)
                        val cityAndStateNames = address.split(", ")
                        mLocation.text = cityAndStateNames[1] + ", " + cityAndStateNames[2]

                        // Gets the item image from Firebase
                        val ONE_MEGABYTE = 1024 * 1024.toLong()
                        storageListings = FirebaseStorage.getInstance().getReference("listings/" + item.itemID.toString())
                        storageListings.getBytes(ONE_MEGABYTE)
                            .addOnSuccessListener(OnSuccessListener<ByteArray?> {
                                // Sets the mImage bitmap as the item image
                                val bitmap = BitmapFactory.decodeByteArray(it, 0, it?.size as Int)
                                mImage.setImageBitmap(bitmap)
                            }).addOnFailureListener(OnFailureListener {
                                // An issue occurred and the mImage bitmap will be set to the stub image
                                val stubBitmap: Bitmap = BitmapFactory.decodeResource(
                                    applicationContext.resources,
                                    R.drawable.stub
                                )
                                mImage.setImageBitmap(stubBitmap)
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

        // Grab the ID of the item to show the details for
        itemID = intent.getStringExtra("ITEM ID")

        // Item detail variables
        mName = findViewById<View>(R.id.itemName) as TextView
        mImage = findViewById<View>(R.id.itemImage) as ImageView
        mDescription = findViewById<View>(R.id.itemDescription) as TextView
        mPrice = findViewById<View>(R.id.itemPrice) as TextView
        mIsForSale = findViewById<View>(R.id.itemForSale) as CheckBox
        mIsForRent = findViewById<View>(R.id.itemForRent) as CheckBox
        mExpiration = findViewById<View>(R.id.itemExpirationDate) as TextView
        mLocation = findViewById<View>(R.id.itemLocation) as TextView
        mUserEmail = findViewById<View>(R.id.userEmail) as TextView

        // Sets up Firebase variables
        databaseListings = FirebaseDatabase.getInstance().getReference("listings")
        databaseListings.addListenerForSingleValueEvent(databaseRefreshListingsListener)

        // Sets up the bottom navbar
        var mNavBar = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
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
