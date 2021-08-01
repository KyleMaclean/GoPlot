package com.example.goplot;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AdjustGoalsPersistTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    @Test
    public void adjustGoalsPersistTest() {
        ViewInteraction materialButton = onView(
                allOf(withId(R.id.buttonTrackGoals), withText("Track Goals"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                        0),
                                6),
                        isDisplayed()));
        materialButton.perform(click());

        ViewInteraction editText = onView(
                allOf(withId(R.id.editTextNumberWeeklyDistanceGoal), withText("20"),
                        withParent(withParent(IsInstanceOf.instanceOf(android.widget.TableLayout.class))),
                        isDisplayed()));
        editText.check(matches(withText("20")));

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.editTextNumberMonthlyDistanceGoal), withText("100"),
                        withParent(withParent(IsInstanceOf.instanceOf(android.widget.TableLayout.class))),
                        isDisplayed()));
        editText2.check(matches(withText("100")));

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editTextNumberWeeklyDistanceGoal), withText("20"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.TableLayout")),
                                        2),
                                1),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("200"));

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.editTextNumberWeeklyDistanceGoal), withText("200"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.TableLayout")),
                                        2),
                                1),
                        isDisplayed()));
        appCompatEditText2.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.editTextNumberMonthlyDistanceGoal), withText("100"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.TableLayout")),
                                        6),
                                1),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("1000"));

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.editTextNumberMonthlyDistanceGoal), withText("1000"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.TableLayout")),
                                        6),
                                1),
                        isDisplayed()));
        appCompatEditText4.perform(closeSoftKeyboard());

        pressBack();

        ViewInteraction materialButton2 = onView(
                allOf(withId(R.id.buttonTrackGoals), withText("Track Goals"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                        0),
                                6),
                        isDisplayed()));
        materialButton2.perform(click());

        ViewInteraction editText3 = onView(
                allOf(withId(R.id.editTextNumberWeeklyDistanceGoal), withText("200"),
                        withParent(withParent(IsInstanceOf.instanceOf(android.widget.TableLayout.class))),
                        isDisplayed()));
        editText3.check(matches(withText("200")));

        ViewInteraction editText4 = onView(
                allOf(withId(R.id.editTextNumberMonthlyDistanceGoal), withText("1000"),
                        withParent(withParent(IsInstanceOf.instanceOf(android.widget.TableLayout.class))),
                        isDisplayed()));
        editText4.check(matches(withText("1000")));

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.editTextNumberWeeklyDistanceGoal), withText("200"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.TableLayout")),
                                        2),
                                1),
                        isDisplayed()));
        appCompatEditText5.perform(replaceText("20"));

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.editTextNumberWeeklyDistanceGoal), withText("20"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.TableLayout")),
                                        2),
                                1),
                        isDisplayed()));
        appCompatEditText6.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.editTextNumberMonthlyDistanceGoal), withText("1000"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.TableLayout")),
                                        6),
                                1),
                        isDisplayed()));
        appCompatEditText7.perform(replaceText("100"));

        ViewInteraction appCompatEditText8 = onView(
                allOf(withId(R.id.editTextNumberMonthlyDistanceGoal), withText("100"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.TableLayout")),
                                        6),
                                1),
                        isDisplayed()));
        appCompatEditText8.perform(closeSoftKeyboard());

        pressBack();

        ViewInteraction materialButton3 = onView(
                allOf(withId(R.id.buttonTrackGoals), withText("Track Goals"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                        0),
                                6),
                        isDisplayed()));
        materialButton3.perform(click());

        ViewInteraction editText5 = onView(
                allOf(withId(R.id.editTextNumberWeeklyDistanceGoal), withText("20"),
                        withParent(withParent(IsInstanceOf.instanceOf(android.widget.TableLayout.class))),
                        isDisplayed()));
        editText5.check(matches(withText("20")));

        ViewInteraction editText6 = onView(
                allOf(withId(R.id.editTextNumberMonthlyDistanceGoal), withText("100"),
                        withParent(withParent(IsInstanceOf.instanceOf(android.widget.TableLayout.class))),
                        isDisplayed()));
        editText6.check(matches(withText("100")));
    }
}
