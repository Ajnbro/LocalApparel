package cmsc436.semesterproject.localapparel

import android.graphics.Bitmap
import android.location.Location
import java.util.*

class ApparelItem {
    var isForSale: Boolean? = null
    var isForRent: Boolean? = null
    var itemBitmap: Bitmap? = null
    var itemName: String? = null // Limit Characters
    var itemDescription: String? = null
    var itemPrice: Float? = null
    var itemLocation : Location? = null
    var sellDate: Date? = null

    constructor(isForSale: Boolean?, isForRent: Boolean?, itemBitmap: Bitmap?, itemName: String?,
                itemDescription: String?, itemPrice: Float?, itemLocation: Location?, sellDate: Date?) {
        this.isForSale = isForSale
        this.isForRent = isForRent
        this.itemBitmap = itemBitmap
        this.itemName = itemName
        this.itemDescription = itemDescription
        this.itemPrice = itemPrice
        this.itemLocation = itemLocation
        this.sellDate = sellDate
    }

}