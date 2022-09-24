package com.orgzly.android.espresso

import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.matcher.ViewMatchers.*
import com.orgzly.R
import com.orgzly.android.OrgzlyTest
import com.orgzly.android.db.entity.SavedSearch
import com.orgzly.android.espresso.EspressoUtils.clickSetting
import com.orgzly.android.espresso.EspressoUtils.onNoteInBook
import com.orgzly.android.prefs.AppPreferences
import com.orgzly.android.repos.RepoType
import com.orgzly.android.ui.main.MainActivity
import com.orgzly.android.ui.settings.SettingsActivity
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import java.io.File

@Ignore("Not a test")
class ScreenshotsTakingNotATest : OrgzlyTest() {
    private lateinit var scenario: ActivityScenario<out AppCompatActivity>

    companion object {
        private const val SCREENSHOTS_DIRECTORY = "/sdcard/Download/screenshots"

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            File(SCREENSHOTS_DIRECTORY).run {
                if (exists()) {
                    if (!deleteRecursively()) {
                        throw Exception("Failed to delete $SCREENSHOTS_DIRECTORY")
                    }
                }
                if (!mkdirs()) {
                    throw Exception("Failed to create $SCREENSHOTS_DIRECTORY")
                }
            }
        }
    }

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()

        importBooks()

        AppPreferences.colorTheme(context, "system")

        AppPreferences.displayedBookDetails(
            context,
            listOf(
                R.string.pref_value_book_details_mtime,
                R.string.pref_value_book_details_notes_count,
                R.string.pref_value_book_details_link_url,
                R.string.pref_value_book_details_encoding_detected,
                R.string.pref_value_book_details_encoding_used,
                R.string.pref_value_book_details_last_action,
            ).map(context.resources::getString)
        )

        dataRepository.replaceSavedSearches(listOf(
            SavedSearch(0, "10 days agenda", ".it.done ad.10", 0),
            SavedSearch(0, "Work in progress", "tn.wip b.gtd", 0),
            SavedSearch(0, "Pre travel", "t.pre@travel", 0),
            SavedSearch(0, "Post travel", "t.post@travel", 0),
            SavedSearch(0, "@ Office", "t.l@office b.gtd", 0),
            SavedSearch(0, "@ Market", "t.l@market b.gtd", 0),
            SavedSearch(0, "@ Downtown", "t.l@downtown b.gtd", 0),
        ))
    }

    private fun importBooks() {
        /*
         * Getting Started.org
         * README.org
         * changelog.org
         * miscellaneous.org
         */
        testUtils.setupRepo(RepoType.DIRECTORY, "file:/data/data/com.orgzly/cache");

        testUtils.sync()
        testUtils.sync() // For "No change"
    }

    @Test
    fun main() {
        startActivity(MainActivity::class.java)

        takeScreenshot("books.png")

        onView(withId(R.id.drawer_layout)).perform(open())
        onView(withId(R.id.sync_button_container)).perform(click()) // Sync for fresh "Last sync"

        takeScreenshot("navigation-drawer.png")

        onView(allOf(
            isDescendantOfA(withId(R.id.drawer_navigation_view)),
            withText("Getting Started")
        ))
            .perform(click())

        // Open quick-menu
        // Not working
        onNoteInBook(4).perform(swipeRight())
        onNoteInBook(4).perform(swipeRight())

        // Fold a note
        onView(allOf(
            withId(R.id.item_head_fold_button_text),
            hasSibling(withText("There is no limit to the number of levels you can have"))
        )).perform(click())

        // Select a note
        onNoteInBook(3).perform(longClick())

        takeScreenshot("book.png")

        // Deselect
        pressBack()

        // Enter note
        onNoteInBook(10).perform(click())
        pressBack() // Exit edit mode

        takeScreenshot("note.png")

        onView(withId(R.id.drawer_layout)).perform(open())
        onView(withText(R.string.searches)).perform(click())

        takeScreenshot("saved-searches.png")

        onView(withId(R.id.drawer_layout)).perform(open())

        onView(allOf(isDescendantOfA(
            withId(R.id.drawer_navigation_view)), withText("10 days agenda")))
            .perform(click())

        takeScreenshot("agenda.png")
    }

    @Test
    fun mainDark() {
        startActivity(MainActivity::class.java, true)

        onView(withId(R.id.drawer_layout)).perform(open())
        onView(withId(R.id.sync_button_container)).perform(click()) // Sync for fresh "Last sync"

        takeScreenshot("navigation-drawer-dark.png")
    }

    @Test
    fun settings() {
        startActivity(SettingsActivity::class.java)

        clickSetting("", R.string.sync)
        clickSetting("", R.string.repositories)

        takeScreenshot("repositories.png")
    }

    private fun startActivity(activityClass: Class<out AppCompatActivity>, nightMode: Boolean = false) {
        if (nightMode) {
            // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            AppPreferences.colorTheme(context, "dark")
            AppPreferences.darkColorScheme(context, "dynamic")

        } else {
            // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        scenario = ActivityScenario.launch(activityClass)

        // onView(withId(R.id.main_content)).check(matches(isDisplayed()))
        SystemClock.sleep(1000)
    }

    private fun takeScreenshot(name: String) {
        return // Set the breakpoint there

        /* Using Android Studio for framing instead of
         * https://developer.android.com/distribute/marketing-tools/device-art-generator
         * which doesn't align the images correctly.
         */

//        File(SCREENSHOTS_DIRECTORY, name).let { file ->
//            UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).run {
//                if (!takeScreenshot(file, 1.0f, 100)) {
//                    throw Exception("Failed to create screenshot $name")
//                }
//            }
//        }
    }
}
