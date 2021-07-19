package com.appspell.shaderview.demo.viewpager

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.repeatedlyUntil
import androidx.test.espresso.matcher.BoundedDiagnosingMatcher
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.viewpager2.widget.ViewPager2
import com.appspell.shaderview.demo.R
import org.hamcrest.Description
import org.junit.Rule
import org.junit.Test

class ViewPagerActivityTest {

    @get:Rule
    var scenario = ActivityScenarioRule(ViewPagerActivity::class.java)

    @Test
    fun test() {
        onView(withId(R.id.pager))
            .perform(repeatedlyUntil(ViewActions.swipeLeft(), PagerViewPageMatcher(4), 10))
            .perform(repeatedlyUntil(ViewActions.swipeRight(), PagerViewPageMatcher(0), 10))
    }

    class PagerViewPageMatcher(private val page: Int) :
        BoundedDiagnosingMatcher<View, ViewPager2>(ViewPager2::class.java) {

        override fun matchesSafely(item: ViewPager2?, mismatchDescription: Description?): Boolean {
            return item?.currentItem == page
        }

        override fun describeMoreTo(description: Description?) {
            description!!.appendText("Check ViewPager position")
        }
    }
}