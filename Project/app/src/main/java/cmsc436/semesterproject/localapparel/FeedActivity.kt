package cmsc436.semesterproject.localapparel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class FeedActivity : AppCompatActivity() {

    lateinit var mNavBar: BottomNavigationView
    private lateinit var databaseListings: DatabaseReference
    private lateinit var storageListings: StorageReference
    lateinit var listViewListings: ListView
    lateinit var distancesSpinner: Spinner
    lateinit var listings: MutableList<ApparelItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        databaseListings = FirebaseDatabase.getInstance().getReference("listings")
        storageListings = FirebaseStorage.getInstance().getReference("listings")

        listings = ArrayList()
        listViewListings = findViewById<View>(R.id.feed) as ListView

        listViewListings.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val list = listings[i]
            //val intent = Intent(applicationContext, ItemDetailsActivity::class.java)

            //intent.putExtra(AUTHOR_ID, author.authorId)
            //intent.putExtra(AUTHOR_NAME, author.authorName)
            //intent.putExtra(USER_ID, USER_ID)
            //startActivity(intent)
        }

        distancesSpinner = findViewById<View>(R.id.distances_spinner) as Spinner

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.distances_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            distancesSpinner.adapter = adapter
        }

        distancesSpinner.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                arg0: AdapterView<*>?, arg1: View,
                arg2: Int, arg3: Long
            ) {
                Toast.makeText(this@FeedActivity, "You have clicked", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        })


        mNavBar = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
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
                R.id.profile -> {
                    // Respond to navigation item 3 click
                    true
                }
                else -> false
            }
        }
    }

    override fun onStart() {
        super.onStart()

        databaseListings.addValueEventListener(object : ValueEventListener {
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
                        listings.add(item!!)
                    }
                }

                val itemListAdaptor = ItemList(this@FeedActivity, listings)
                listViewListings.adapter = itemListAdaptor
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    companion object {
        private val TAG = "LocalApparel-FeedActivity"
    }
}