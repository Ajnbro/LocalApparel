package cmsc436.semesterproject.localapparel

import android.content.Context
import android.content.SharedPreferences

// CITATION: Done with the help of https://medium.com/@mxcsyounes/the-easiest-way-to-build-intro-sliders-in-android-in-3-steps-3d6c952153e8
class PreferenceManager(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        "preference",
        0)
    private val editor: SharedPreferences.Editor

    init {
        editor = preferences.edit()
    }

    fun isFirstRun() = preferences.getBoolean("isFirstRun", true)

    fun setFirstRun() {
        editor.putBoolean("isFirstRun", false).commit()
        editor.commit()
    }
}