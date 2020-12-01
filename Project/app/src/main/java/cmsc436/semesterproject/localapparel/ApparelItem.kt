package cmsc436.semesterproject.localapparel

import android.location.Location

data class ApparelItem(var isForSale: Boolean? = false,
                       var isForRent: Boolean? = false,
                       var itemName: String? = "",
                       var itemDescription: String? = "",
                       var itemPrice: Double? = 0.0,
                       var itemLatitude: Double? = 0.0,
                       var itemLongitude: Double? = 0.0,
                       var listingPostDate: String? = "",
                       var listingExpirationDate: String? = "",
                       var userID: String? = "",
                       var itemID: String? = "")