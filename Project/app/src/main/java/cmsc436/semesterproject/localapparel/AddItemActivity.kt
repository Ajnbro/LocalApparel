package cmsc436.semesterproject.localapparel

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.cottacush.android.currencyedittext.CurrencyEditText
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class AddItemActivity : Activity() {

    // AddItem variables
    private var mItemName: EditText? = null
    private var mImageUploadButton: Button? = null
    private var mImageBitmap: Bitmap? = null
    private var mItemImage: ImageView? = null
    private var mItemDescription: EditText? = null
    private var mItemPrice: CurrencyEditText? = null
    private var mPriceRentalText: TextView? = null
    private var mItemSaleCheckBox: CheckBox? = null
    private var mItemRentCheckBox: CheckBox? = null
    private var mItemExpirationDate: TextView? = null

    // Firebase variables
    private lateinit var databaseListings: DatabaseReference
    private lateinit var storageListings: StorageReference
    private var mAuth: FirebaseAuth? = null

    // Location variables
    private lateinit var locationManager: LocationManager
    private lateinit var mLocationListener: LocationListener
    private var mLastLocationReading: Location? = null
    private val mMinTime: Long = 5000
    private val mMinDistance = 1000.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_item)

        mItemName = findViewById<View>(R.id.itemName) as EditText
        mImageUploadButton = findViewById<View>(R.id.itemImageUpload) as Button
        mItemDescription = findViewById<View>(R.id.itemDescription) as EditText
        mItemPrice = findViewById<View>(R.id.itemPrice) as CurrencyEditText
        mPriceRentalText = findViewById<View>(R.id.rentalHourly) as TextView
        mItemExpirationDate = findViewById<View>(R.id.itemExpirationDate) as TextView
        mItemImage = findViewById<View>(R.id.itemImage) as ImageView
        mItemSaleCheckBox = findViewById<View>(R.id.itemForSale) as CheckBox
        mItemRentCheckBox = findViewById<View>(R.id.itemForRent) as CheckBox

        // Adds a listener to handle mPriceRentalText visibility and ensure that the sibling checkbox is unchecked
        mItemSaleCheckBox!!.setOnCheckedChangeListener {_, checked: Boolean ->
            if (checked) {
                mItemRentCheckBox!!.isChecked = false;
                mPriceRentalText!!.visibility = View.INVISIBLE
            }
        }

        // Adds a listener to handle mPriceRentalText visibility and ensure that the sibling checkbox is unchecked
        mItemRentCheckBox!!.setOnCheckedChangeListener {_, checked: Boolean ->
            if (checked) {
                mItemSaleCheckBox!!.isChecked = false;
                mPriceRentalText!!.visibility = View.VISIBLE
            } else {
                mPriceRentalText!!.visibility = View.INVISIBLE
            }
        }

        // Setting up Firebase resources
        databaseListings = FirebaseDatabase.getInstance().getReference("listings")
        storageListings = FirebaseStorage.getInstance().getReference("listings")
        mAuth = FirebaseAuth.getInstance()

        // Setting up Location resources
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mLocationListener = makeLocationListener()

        // Set the default date
        setDefaultDate()
        val datePickerButton = findViewById<View>(R.id.expirationDateButton) as Button
        datePickerButton.setOnClickListener { showDatePickerDialog() }

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
                    true
                }
                R.id.listings -> {
                    val intent = Intent(this, YourListingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Sets up the submit button
        val submitButton = findViewById<View>(R.id.submitButton) as Button
        submitButton.setOnClickListener {
            submitListing()
        }

        // Sets up the ability to upload an image
        mImageUploadButton!!.setOnClickListener { imageOnClick() }
    }

    // Starts an activity to select an image from camera roll
    private fun imageOnClick() {
        startActivityForResult(
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            ), IMAGE_SELECTION
        )
    }

    // CITATION: Lab11
    override fun onResume() {
        super.onResume()
        // Check for location permissions
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                "android.permission.ACCESS_FINE_LOCATION"
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                applicationContext,
                "android.permission.ACCESS_COARSE_LOCATION"
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@AddItemActivity, arrayOf(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION"
                ),
                MY_PERMISSIONS_LOCATION
            )
        } else getLocationUpdates()
    }

    // Occurs after the image is selected from the photo gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_SELECTION && resultCode == RESULT_OK) {
            val selectedImage: Uri? = data.data
            try {
                mImageBitmap = MediaStore.Images.Media.getBitmap(
                    this.contentResolver,
                    selectedImage
                )
                // Set the image selected in the photo gallery to the item image
                mItemImage?.setImageBitmap(mImageBitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Validates user input and puts the item in Firebase
    private fun submitListing() {
        // Validate the item name
        var itemName = mItemName!!.text.toString()
        if (itemName.equals("")) {
            Toast.makeText(
                applicationContext,
                "Error posting listing! The item name must be specified.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Validate the item description
        var itemDescription = mItemDescription!!.text.toString()
        if (itemDescription.equals("")) {
            Toast.makeText(
                applicationContext,
                "Error posting listing! The item description must be specified.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Validate the item status (forSale v. forRent)
        var isForSale = mItemSaleCheckBox!!.isChecked
        var isForRent = mItemRentCheckBox!!.isChecked
        if (!isForSale && !isForRent) {
            Toast.makeText(
                applicationContext,
                "Error posting listing! The item must be listed for sale or for rent.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Validate the item price
        var itemPrice = mItemPrice!!.getNumericValue()
        if (itemPrice == null) {
            Toast.makeText(
                applicationContext,
                "Error posting listing! A non-zero price must be specified.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (itemPrice > 1000000) {
            Toast.makeText(
                applicationContext,
                "Error posting listing! The item price may not exceed $1,000,000.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Validate the specified expiration date
        val c = Calendar.getInstance()
        var listingPostDate = dateString(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
        c.get(Calendar.DAY_OF_MONTH))
        var listingExpirationDate = mItemExpirationDate!!.text.toString()

        // Ensure that the specified expiration date has not already expired
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val expirationDate: Date = sdf.parse(listingExpirationDate)
        val postingDate: Date = sdf.parse(listingPostDate)
        if (postingDate.after(expirationDate)) {
            Toast.makeText(
                applicationContext,
                "Error posting listing! The expiration date has already passed.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Grab the userID and key to store within the Firebase item
        var userID = FirebaseAuth.getInstance().currentUser!!.uid
        val key = databaseListings.push().key.toString()

        var item = ApparelItem(
            isForSale,
            isForRent,
            itemName,
            itemDescription,
            itemPrice,
            mLastLocationReading!!.latitude,
            mLastLocationReading!!.longitude,
            listingPostDate,
            listingExpirationDate,
            userID,
            key,
            mAuth?.currentUser?.email
        )

        // Validate that an image was selected and submitted to the application
        if(mImageBitmap == null) {
            Toast.makeText(
                applicationContext,
                "Error posting listing! An image of the item must be uploaded.",
                Toast.LENGTH_LONG
            ).show()
            return
        } else {
            val baos = ByteArrayOutputStream()
            mImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageData: ByteArray = baos.toByteArray()
            storageListings.child(key).putBytes(imageData)
        }

        // Upload the item to Firebase
        databaseListings.child(key).setValue(item, object : DatabaseReference.CompletionListener {
            override fun onComplete(firebaseError: DatabaseError?, ref: DatabaseReference) {
                if (firebaseError != null) {
                    Toast.makeText(
                        applicationContext,
                        "Error posting listing! Please check your input.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Listing successfully uploaded!",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    finish()
                }
            }
        })
    }

    // Sets the default expiration date to today
    private fun setDefaultDate() {
        val today = Calendar.getInstance()
        mItemExpirationDate!!.text = dateString(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH))
    }

    // CITATION: Lab4
    // The DialogFragment class will be used to select the expiration date for the Item
    class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

            // Set the default date within the date picker to today
            val today = Calendar.getInstance()
            val year = today.get(Calendar.YEAR)
            val month = today.get(Calendar.MONTH)
            val day = today.get(Calendar.DAY_OF_MONTH)
            return DatePickerDialog(activity, this, year, month, day)
        }

        override fun onDateSet(
            view: DatePicker, year: Int, monthOfYear: Int,
            dayOfMonth: Int
        ) {
            // Update the expiration date shown to the user with the newly selected date
            val dateView: TextView = activity.findViewById(R.id.itemExpirationDate)
            dateView.text = dateString(year, monthOfYear, dayOfMonth)
        }

    }

    // CITATION: Lab4
    private fun showDatePickerDialog() {
        val newFragment = DatePickerFragment()
        newFragment.show(fragmentManager, "datePicker")
    }

    // CITATION: Lab11
    // Ensures that permissions have been properly granted for Location services
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_LOCATION -> {
                var g = 0
                Log.d(TAG, "Perm?: " + permissions.size + " -? " + grantResults.size)
                for (perm in permissions) Log.d(TAG, "Perm: " + perm + " --> " + grantResults[g++])
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) getLocationUpdates() else {
                    Log.i(TAG, "Permission was not granted to access location")
                    finish()
                }
            }
        }
    }

    // CITATION: Lab11
    // Gets the last known location for the user if the prior known location is older than 3 minutes
    private fun getLocationUpdates(){
        try {
            var loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc != null && (System.currentTimeMillis() - loc.time) < THREE_MINS) {
                mLastLocationReading = loc;
            }

            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null && (System.currentTimeMillis() - loc.time) < THREE_MINS) {
                mLastLocationReading = loc;
            }

            if (null != locationManager.getProvider(LocationManager.NETWORK_PROVIDER)) {
                Log.i(TAG, "Network location updates requested")
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    mMinTime,
                    mMinDistance,
                    mLocationListener
                )
            }

            if (null != locationManager.getProvider(LocationManager.GPS_PROVIDER)) {
                Log.i(TAG, "GPS location updates requested")
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    mMinTime,
                    mMinDistance,
                    mLocationListener
                )
            }

        } catch (e: SecurityException) {
            Log.d(TAG, e.localizedMessage.toString())
        }
    }

    // Makes a location listener used by the location manager to set the last known location
    private fun makeLocationListener(): LocationListener {
        return object : LocationListener {
            // CITATION: Lab11
            override fun onLocationChanged(location: Location) {
                if(mLastLocationReading == null) {
                    mLastLocationReading = location
                }
                else if(mLastLocationReading!!.time < location.time) {
                    mLastLocationReading = location
                }
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }
    }

    companion object {
        private val TAG = "LocalApparel-AddItemActivity"

        // CITATION: Lab11 modified
        const val IMAGE_SELECTION = 3;
        const val MY_PERMISSIONS_LOCATION = 4
        private const val THREE_MINS = 3 * 60 * 1000.toLong()

        // CITATION: Lab4 modified
        private fun dateString(year: Int, monthOfYear: Int, dayOfMonth: Int): String {
            var month = monthOfYear

            month++
            var mon = "" + month
            var day = "" + dayOfMonth

            if (month < 10)
                mon = "0$month"
            if (dayOfMonth < 10)
                day = "0$dayOfMonth"

            return "$year-$mon-$day"
        }
    }
}
