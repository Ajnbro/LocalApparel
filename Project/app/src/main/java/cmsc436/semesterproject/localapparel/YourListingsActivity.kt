package cmsc436.semesterproject.localapparel

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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

class YourListingsActivity : AppCompatActivity(){
    // Feed variables
    lateinit var listingsRecyclerView: RecyclerView
    lateinit var listings: MutableList<ApparelItem>

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
                    // Ensure that the item has not expired
                    val c = Calendar.getInstance()
                    var today = dateString(
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)
                    )
                    var listingExpirationDate = item!!.listingExpirationDate.toString()
                    val sdf = SimpleDateFormat("yyyy-MM-dd")
                    val expirationDate: Date = sdf.parse(listingExpirationDate) as Date
                    val todayDate: Date = sdf.parse(today) as Date
                    val currentFirebaseUser: FirebaseUser? =
                        FirebaseAuth.getInstance().currentUser

                    if (item.userID == currentFirebaseUser?.uid &&!todayDate.after(expirationDate)) {
                        listings.add(item)
                    }
                }
            }

            // Add the listings to the RecyclerView
            listingsRecyclerView.adapter = ItemRecyclerViewAdapter(
                this@YourListingsActivity,
                listings,
                R.layout.apparel_item
            )
        }

        override fun onCancelled(databaseError: DatabaseError) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.your_listings)

        // Setting up Firebase resources
        databaseListings = FirebaseDatabase.getInstance().getReference("listings")
        storageListings = FirebaseStorage.getInstance().getReference("listings")

        // Setting up feed resources
        listings = ArrayList()
        listingsRecyclerView = findViewById<View>(R.id.feed) as RecyclerView
        listingsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Sets up a divider between each item in the recycler view
        var dividerItemDecoration = DividerItemDecoration(
            applicationContext,
            LinearLayoutManager.VERTICAL
        )
        dividerItemDecoration.setDrawable(
            applicationContext.resources.getDrawable(R.drawable.recycler_view_line)
        )
        listingsRecyclerView.addItemDecoration(dividerItemDecoration)


        // Sets up the bottom navbar
        var mNavBar = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        mNavBar.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.browse -> {
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

    override fun onStart() {
        super.onStart()

        // Refresh the listings
        databaseListings.addValueEventListener(databaseRefreshListingsListener)
    }

    companion object {
        private val TAG = "LocalApparel-YourListingsActivity"

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