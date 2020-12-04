package cmsc436.semesterproject.localapparel

import android.content.Context
import android.content.SharedPreferences

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