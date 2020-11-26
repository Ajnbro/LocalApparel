package cmsc436.semesterproject.localapparel

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class FeedActivity : AppCompatActivity() {

    lateinit var mNavBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        mNavBar = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.browse -> {
                    // Respond to navigation item 1 click
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
    }
}