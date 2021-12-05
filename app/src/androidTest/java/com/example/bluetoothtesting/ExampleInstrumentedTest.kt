package com.example.bluetoothtesting

import android.provider.Telephony
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Delay
import org.hamcrest.Matchers.not

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.bluetoothtesting", appContext.packageName)
    }

    @Test
    fun cnnectionReadDevice(){
        //onView(withId(R.id.chatFragment)).check(matches(not(isDisplayed())))
        onView(withText("RPE-00002")).check(matches(isDisplayed()))
        onView(withText("RPE-00002")).perform(click())
        Thread.sleep(3000)
        onView(withText("Valore aspirazione")).check(matches(isDisplayed()))
        onView(withId(R.id.chatFragment)).check(matches(isDisplayed()))
    }

    @Test
    fun reconnectionSixTimesBefore3SEC(){
        //onView(withId(R.id.chatFragment)).check(matches(not(isDisplayed())))
        for(i in 1..3) {
            onView(withText("RPE-00002")).check(matches(isDisplayed()))
            onView(withText("RPE-00002")).perform(click())
            Thread.sleep(3500)
            onView(withText("Valore aspirazione")).check(matches(isDisplayed()))
            onView(withId(R.id.chatFragment)).check(matches(isDisplayed()))
            Espresso.pressBack() //solo se in RootActivity
            onView(withText("RPE-00010")).check(matches(isDisplayed()))
            onView(withText("RPE-00010")).perform(click())
            Thread.sleep(3500)
            onView(withText("Valore aspirazione")).check(matches(isDisplayed()))
            onView(withId(R.id.chatFragment)).check(matches(isDisplayed()))
            Espresso.pressBack()
        }
    }

    @Test
    fun Thread(){

    }

}