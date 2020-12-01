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
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*


class AddItemActivity : Activity() {

    private var mDate: Date? = null
    private var mItemName: EditText? = null
    private var mImageUploadButton: Button? = null
    private var mImageBitmap: Bitmap? = null
    private var mItemImage: ImageView? = null
    private var mItemDescription: EditText? = null
    private var mItemPrice: EditText? = null
    private var mItemSaleCheckBox: CheckBox? = null
    private var mItemRentCheckBox: CheckBox? = null
    private var mItemExpirationDate: TextView? = null
    private var mLastLocationReading: Location? = null

    private val mMinTime: Long = 5000

    private val mMinDistance = 1000.0f

    lateinit var mNavBar: BottomNavigationView
    private lateinit var databaseListings: DatabaseReference
    private lateinit var storageListings: StorageReference
    private lateinit var locationManager: LocationManager
    private lateinit var mLocationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_item)

        databaseListings = FirebaseDatabase.getInstance().getReference("listings")
        storageListings = FirebaseStorage.getInstance().getReference("listings")

        mLocationListener = makeLocationListener()
        mItemName = findViewById<View>(R.id.itemName) as EditText
        mImageUploadButton = findViewById<View>(R.id.itemImageUpload) as Button
        mItemDescription = findViewById<View>(R.id.itemDescription) as EditText
        mItemPrice = findViewById<View>(R.id.itemPrice) as EditText
        mItemSaleCheckBox = findViewById<View>(R.id.itemForSale) as CheckBox
        mItemRentCheckBox = findViewById<View>(R.id.itemForRent) as CheckBox
        mItemExpirationDate = findViewById<View>(R.id.itemExpirationDate) as TextView
        mItemImage = findViewById<View>(R.id.itemImage) as ImageView

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Set the default date
        setDefaultDate()

        val datePickerButton = findViewById<View>(R.id.date_picker_button) as Button
        datePickerButton.setOnClickListener { showDatePickerDialog() }

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
                    true
                }
                R.id.profile -> {
                    // Respond to navigation item 3 click
                    true
                }
                else -> false
            }
        }

        // Set up OnClickListener for the Submit Button
        val submitButton = findViewById<View>(R.id.submitButton) as Button
        submitButton.setOnClickListener {
            submitListing()
        }

        mImageUploadButton!!.setOnClickListener { imageOnClick() }
    }

    private fun imageOnClick() {
        startActivityForResult(Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY)
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(applicationContext, "android.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(applicationContext, "android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@AddItemActivity, arrayOf("android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION"),
                MY_PERMISSIONS_LOCATION)
        } else getLocationUpdates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        //Detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == RESULT_OK) {
            val selectedImage: Uri? = data.data
            try {
                mImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                mItemImage?.setImageBitmap(mImageBitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun submitListing() {
        var isForSale = mItemSaleCheckBox!!.isChecked
        var isForRent = mItemRentCheckBox!!.isChecked
        var itemName = mItemName!!.text.toString()
        var itemDescription = mItemDescription!!.text.toString()
        var itemPrice = mItemPrice!!.text.toString().toDouble() // MAYBE ADD A REGEX TO CATCH BAD INPUT

        val baos = ByteArrayOutputStream()
        mImageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData: ByteArray = baos.toByteArray()

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        setDateString(year, month, day)
        var listingPostDate = dateString

        var listingExpirationDate = mItemExpirationDate!!.text.toString()

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
                key
        )

        storageListings.child(key).putBytes(imageData)
        databaseListings.child(key).setValue(item, object : DatabaseReference.CompletionListener {
            override fun onComplete(firebaseError: DatabaseError?, ref: DatabaseReference) {
                if (firebaseError != null) {
                    Toast.makeText(
                        applicationContext,
                        "Error posting listing! Please check your input.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(applicationContext, "Listing successfully uploaded!", Toast.LENGTH_LONG)
                        .show()
                    finish()
                }
            }
        })
    }

    private fun setDefaultDate() {
        mDate = Date()

        val c = Calendar.getInstance()
        setDateString(
            c.get(Calendar.YEAR), c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )

        mItemExpirationDate!!.text = dateString
    }

    // DialogFragment used to pick a ToDoItem deadline date
    class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

            // Use the current date as the default date in the picker

            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            // Create a new instance of DatePickerDialog and return it
            return DatePickerDialog(activity, this, year, month, day)
        }

        override fun onDateSet(
            view: DatePicker, year: Int, monthOfYear: Int,
            dayOfMonth: Int
        ) {
            setDateString(year, monthOfYear, dayOfMonth)
            val dateView: TextView = activity.findViewById(R.id.itemExpirationDate)
            dateView.text = dateString
        }

    }

    private fun showDatePickerDialog() {
        val newFragment = DatePickerFragment()
        newFragment.show(fragmentManager, "datePicker")
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_LOCATION -> {
                var g = 0
                Log.d(TAG, "Perm?: " + permissions.size + " -? " + grantResults.size)
                for (perm in permissions) Log.d(TAG, "Perm: " + perm + " --> " + grantResults[g++])
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) getLocationUpdates() else {
                    Log.i(TAG, "Permission was not granted to access location")
                    finish()
                }
            }
        }
    }

    private fun getLocationUpdates(){
        try {
            var loc = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc != null && (System.currentTimeMillis() - loc.time) < FIVE_MINS) {
                mLastLocationReading = loc;
            }

            loc = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc != null && (System.currentTimeMillis() - loc.time) < FIVE_MINS) {
                mLastLocationReading = loc;
            }

            if (null != locationManager!!.getProvider(LocationManager.NETWORK_PROVIDER)) {
                Log.i(TAG, "Network location updates requested")
                locationManager!!.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    mMinTime,
                    mMinDistance,
                    mLocationListener
                )
            }

            if (null != locationManager!!.getProvider(LocationManager.GPS_PROVIDER)) {
                Log.i(TAG, "GPS location updates requested")
                locationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    mMinTime,
                    mMinDistance,
                    mLocationListener
                )
            }

        } catch (e: SecurityException) {
            Log.d(TAG, e.localizedMessage)
        }
    }


    private fun makeLocationListener(): LocationListener {
        return object : LocationListener {
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

        private var dateString: String? = null

        private val GET_FROM_GALLERY = 3;

        const val MY_PERMISSIONS_LOCATION = 4

        private const val FIVE_MINS = 5 * 60 * 1000.toLong()

        private fun setDateString(year: Int, monthOfYear: Int, dayOfMonth: Int) {
            var monthOfYear = monthOfYear

            // Increment monthOfYear for Calendar/Date -> Time Format setting
            monthOfYear++
            var mon = "" + monthOfYear
            var day = "" + dayOfMonth

            if (monthOfYear < 10)
                mon = "0$monthOfYear"
            if (dayOfMonth < 10)
                day = "0$dayOfMonth"

            dateString = "$year-$mon-$day"
        }
    }
}
