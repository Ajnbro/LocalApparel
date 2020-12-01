package cmsc436.semesterproject.localapparel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*


class ItemDetailsActivity : Activity() {

    lateinit var mNavBar: BottomNavigationView
    private lateinit var databaseListings: DatabaseReference
    private lateinit var storageListings: StorageReference
    var listing: ApparelItem? = null
    var itemID: String? = null
    lateinit var mName: TextView
    lateinit var mImage: ImageView
    lateinit var mDescription: TextView
    lateinit var mPrice: TextView
    lateinit var mIsForSale: CheckBox
    lateinit var mIsForRent: CheckBox
    lateinit var mExpiration: TextView
    lateinit var mLocation: TextView



    var databaseRefreshListingsListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            Log.i(TAG, "Hits onDataChange()")
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
                        mPrice.text = item!!.itemPrice.toString()
                        mExpiration.text = item!!.listingExpirationDate
                        mIsForSale.isChecked = item!!.isForSale as Boolean
                        mIsForRent.isChecked = item!!.isForRent as Boolean
                        // NEED TO DO LOCATION AND IMAGE STILL
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
        mIsForRent = findViewById<View>(R.id.itemForSale) as CheckBox
        mExpiration = findViewById<View>(R.id.itemExpirationDate) as TextView
        mLocation = findViewById<View>(R.id.itemLocation) as TextView

        databaseListings = FirebaseDatabase.getInstance().getReference("listings")
        storageListings = FirebaseStorage.getInstance().getReference("listings")
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
                R.id.profile -> {
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
