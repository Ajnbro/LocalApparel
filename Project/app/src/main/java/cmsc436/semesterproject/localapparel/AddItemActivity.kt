package cmsc436.semesterproject.localapparel

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*


class AddItemActivity : Activity() {

    private var mDate: Date? = null
    private var mItemName: EditText? = null
    private var mImageUploadButton: Button? = null
    private var mItemDescription: EditText? = null
    private var mItemPrice: EditText? = null
    private var mItemSaleCheckBox: CheckBox? = null
    private var mItemRentCheckBox: CheckBox? = null
    private var mItemExpirationDate: TextView? = null

    lateinit var mNavBar: BottomNavigationView
    private lateinit var databaseListings: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_item)

        databaseListings = FirebaseDatabase.getInstance().getReference("listings")


        mItemName = findViewById<View>(R.id.itemName) as EditText
        mImageUploadButton = findViewById<View>(R.id.itemImageUpload) as Button
        mItemDescription = findViewById<View>(R.id.itemDescription) as EditText
        mItemPrice = findViewById<View>(R.id.itemPrice) as EditText
        mItemSaleCheckBox = findViewById<View>(R.id.itemForSale) as CheckBox
        mItemRentCheckBox = findViewById<View>(R.id.itemForRent) as CheckBox
        mItemExpirationDate = findViewById<View>(R.id.itemExpirationDate) as TextView

        // Set the default date and time

        setDefaultDateTime()

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
    }

    private fun submitListing() {
        var isForSale = mItemSaleCheckBox!!.isChecked
        var isForRent = mItemRentCheckBox!!.isChecked
        var itemName = mItemName!!.text.toString()
        var itemDescription = mItemDescription!!.text.toString()
        var itemPrice = mItemPrice!!.text.toString().toFloat() // MAYBE ADD A REGEX TO CATCH BAD INPUT

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        setDateString(year, month, day)
        var listingPostDate = dateString

        var listingExpirationDate = mItemExpirationDate!!.text.toString()

        var userID = FirebaseAuth.getInstance().currentUser!!.uid

        var item = ApparelItem(
            isForSale,
            isForRent,
            itemName,
            itemDescription,
            itemPrice,
            null,
            listingPostDate,
            listingExpirationDate,
            userID
        )

        val key = databaseListings.push().key.toString()
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


    private fun setDefaultDateTime() {

        // Default is current time + 7 days
        mDate = Date()
        mDate = Date(mDate!!.time)

        val c = Calendar.getInstance()
        c.time = mDate

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

    companion object {

        // 7 days in milliseconds - 7 * 24 * 60 * 60 * 1000
        private val SEVEN_DAYS = 604800000

        private val TAG = "LocalApparel"

        private var dateString: String? = null

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
