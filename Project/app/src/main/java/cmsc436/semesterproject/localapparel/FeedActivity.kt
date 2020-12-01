package cmsc436.semesterproject.localapparel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*


class FeedActivity : AppCompatActivity() {

    lateinit var mNavBar: BottomNavigationView
    //private lateinit var mLayoutInflater: LayoutInflater
    lateinit var mRefreshButton: Button
    private lateinit var databaseListings: DatabaseReference
    internal lateinit var listViewListings: ListView
    internal lateinit var listings: MutableList<ApparelItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        databaseListings = FirebaseDatabase.getInstance().getReference("listings")
        //mLayoutInflater = LayoutInflater.from(this)

        listings = ArrayList()
        listViewListings = findViewById<View>(R.id.feed) as ListView

        loadItems()

        mRefreshButton = findViewById<View>(R.id.refresh_button) as Button
        mRefreshButton.setOnClickListener {
            loadItems()
        }

        listViewListings.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val list = listings[i]
            //val intent = Intent(applicationContext, ItemDetailsActivity::class.java)

            //intent.putExtra(AUTHOR_ID, author.authorId)
            //intent.putExtra(AUTHOR_NAME, author.authorName)
            //intent.putExtra(USER_ID, USER_ID)
            //startActivity(intent)
        }

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

    public override fun onResume() {
        super.onResume()
        loadItems()
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




    private fun loadItems() {
        // Sort based on locations or other specified filters and then add everything
        //for (){
        //}

        Log.i(TAG, "Loaded items for the feed")
    }

    companion object {
        private val TAG = "LocalApparel-FeedActivity"
    }
}