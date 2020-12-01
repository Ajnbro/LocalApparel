package cmsc436.semesterproject.localapparel

import android.location.Location

data class ApparelItem(var isForSale: Boolean? = false,
                       var isForRent: Boolean? = false,
                       var itemName: String? = "",
                       var itemDescription: String? = "",
                       var itemPrice: Float? = 0f,
                       var itemLocation : Location? = null,
                       var listingPostDate: String? = "",
                       var listingExpirationDate: String? = "",
                       var userID: String? = "")