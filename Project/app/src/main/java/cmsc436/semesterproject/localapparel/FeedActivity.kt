package cmsc436.semesterproject.localapparel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.Location.distanceBetween
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FeedActivity : AppCompatActivity() {

    // Feed variables
    lateinit var listingsRecyclerView: RecyclerView
    lateinit var distancesSpinner: Spinner
    lateinit var listings: MutableList<ApparelItem>

    // Location variables
    var mDistance: Float = 5f
    private lateinit var locationManager: LocationManager
    private lateinit var mLocationListener: LocationListener
    private var mLastLocationReading: Location? = null
    private val mMinTime: Long = 5000
    private val mMinDistance = 1000.0f

    // Firebase variables
    private lateinit var databaseListings: DatabaseReference
    private lateinit var storageListings: StorageReference

    // CITATION: based upon Lab7
    var databaseRefreshListingsListener: ValueEventListener = object : ValueEventListener {
        @SuppressLint("SimpleDateFormat")
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            listings.clear()

            var item: ApparelItem? = null
            for (postSnapshot in dataSnapshot.children) {
                try {
                    item = postSnapshot.getValue(ApparelItem::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    return
                } finally {
                    // Ensure that the item is within the specified number of miles
                    var itemLat = item!!.itemLatitude as Double
                    var itemLong = item.itemLongitude as Double
                    var diff = FloatArray(1)
                    distanceBetween(
                        itemLat,
                        itemLong,
                        mLastLocationReading!!.latitude,
                        mLastLocationReading!!.longitude,
                        diff
                    )
                    var dist = diff[0] * 0.000621371192;

                    // Ensure that the item has not expired
                    val c = Calendar.getInstance()
                    var today = dateString(
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)
                    )
                    var listingExpirationDate = item.listingExpirationDate.toString()
                    val sdf = SimpleDateFormat("yyyy-MM-dd")
                    val expirationDate: Date = sdf.parse(listingExpirationDate) as Date
                    val todayDate: Date = sdf.parse(today) as Date
                    val currentFirebaseUser: FirebaseUser? =
                        FirebaseAuth.getInstance().currentUser
                    if (item.userID != currentFirebaseUser?.uid && (dist <= mDistance) && !todayDate.after(expirationDate)) {
                        listings.add(item)
                    }
                }
            }

            // Add the listings to the RecyclerView
            listingsRecyclerView.adapter = ItemRecyclerViewAdapter(
                this@FeedActivity,
                listings,
                R.layout.apparel_item
            )
        }

        override fun onCancelled(databaseError: DatabaseError) {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feed)

        // Setting up Firebase resources
        databaseListings = FirebaseDatabase.getInstance().getReference("listings")
        storageListings = FirebaseStorage.getInstance().getReference("listings")

        // Setting up Location resources
        mLocationListener = makeLocationListener()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Setting up feed resources
        listings = ArrayList()
        listingsRecyclerView = findViewById<View>(R.id.feed) as RecyclerView
        listingsRecyclerView.layoutManager = LinearLayoutManager(this)
        distancesSpinner = findViewById<View>(R.id.distances_spinner) as Spinner

        // Sets up a divider between each item in the recycler view
        var dividerItemDecoration = DividerItemDecoration(
            applicationContext,
            LinearLayoutManager.VERTICAL
        )
        dividerItemDecoration.setDrawable(
            applicationContext.resources.getDrawable(R.drawable.recycler_view_line)
        )
        listingsRecyclerView.addItemDecoration(dividerItemDecoration)

        // CITATION: https://developer.android.com/guide/topics/ui/controls/spinner
        // Set up the Spinner for distance selection
        ArrayAdapter.createFromResource(
            this,
            R.array.distances_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            distancesSpinner.adapter = adapter
        }

        distancesSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, pos: Int, id: Long
            ) {
                // Store the distance selected by the user to filter locations using
                mDistance = parent.getItemAtPosition(pos).toString().split(" ")[0].toFloat()
                // Refresh the listings
                databaseListings.addListenerForSingleValueEvent(databaseRefreshListingsListener)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Sets up the bottom navbar
        var mNavBar = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        mNavBar.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.browse -> {
                    // Respond to navigation item 1 click
                    true
                }
                R.id.add -> {
                    // Respond to navigation item 2 click
                    val intent = Intent(this, AddItemActivity::class.java)
                    startActivity(intent)
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
    }

    override fun onStart() {
        super.onStart()

        // Refresh the listings
        databaseListings.addValueEventListener(databaseRefreshListingsListener)
    }

    // CITATION: Lab11
    // Ensures that location permissions have been properly granted
    override fun onResume() {
        super.onResume()
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
                this@FeedActivity, arrayOf(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION"
                ),
                MY_PERMISSIONS_LOCATION
            )
        } else getLocationUpdates()
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
            Log.d(TAG, e.toString())
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

    companion object {
        private val TAG = "LocalApparel-FeedActivity"
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