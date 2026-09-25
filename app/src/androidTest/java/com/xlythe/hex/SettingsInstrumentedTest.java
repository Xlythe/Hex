package com.xlythe.hex;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.hex.core.Timer;
import com.xlythe.hex.compat.AndroidBotFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class SettingsInstrumentedTest {
    private static final String SWAP = "swapPref";
    private static final String AUTOSAVE = "autosavePref";

    private Context context;
    private SharedPreferences preferences;

    @Before
    public void clearPreferences() {
        context = ApplicationProvider.getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        assertTrue(preferences.edit().clear().commit());
    }

    @After
    public void cleanUpPreferences() {
        assertTrue(preferences.edit().clear().commit());
    }

    @Test
    public void defaultsAndEveryStoredOptionAreReadable() {
        assertEquals(7, Settings.getGridSize(context));
        assertTrue(Settings.getSwap(context));
        assertTrue(Settings.getAutosave(context));
        assertEquals(Timer.NO_TIMER, Settings.getTimerType(context));
        assertEquals(0, Settings.getTimeAmount(context));
        assertEquals(1, Settings.getComputerDifficulty(context));
        assertEquals("Player", Settings.getPlayer1Name(context, "  Player One  "));
        assertEquals(context.getString(R.string.DEFAULT_P1_NAME),
                Settings.getPlayer1Name(context, " "));
        assertEquals(context.getString(R.string.DEFAULT_P2_NAME),
                Settings.getPlayer2Name(context));
        assertEquals(context.getResources().getInteger(R.integer.DEFAULT_P1_COLOR),
                Settings.getPlayer1Color(context));
        assertEquals(context.getResources().getInteger(R.integer.DEFAULT_P2_COLOR),
                Settings.getPlayer2Color(context));

        for (int size : new int[] {7, 9, 11}) {
            putString(Settings.GAME_SIZE, String.valueOf(size));
            assertEquals(size, Settings.getGridSize(context));
        }
        putString(Settings.GAME_SIZE, "0");
        putString(Settings.CUSTOM_GAME_SIZE, "23");
        assertEquals(23, Settings.getGridSize(context));

        for (int timerType : new int[] {
                Timer.NO_TIMER, Timer.PER_MOVE, Timer.ENTIRE_MATCH}) {
            putString(Settings.TIMER_TYPE, String.valueOf(timerType));
            assertEquals(timerType, Settings.getTimerType(context));
        }
        putString(Settings.TIMER, "45");
        assertEquals(45, Settings.getTimeAmount(context));

        for (int difficulty = 0; difficulty <= AndroidBotFactory.TREE; difficulty++) {
            putString(Settings.DIFFICULTY, String.valueOf(difficulty));
            assertEquals(difficulty, Settings.getComputerDifficulty(context));
        }

        assertTrue(preferences.edit()
                .putBoolean(SWAP, false)
                .putBoolean(AUTOSAVE, false)
                .commit());
        assertFalse(Settings.getSwap(context));
        assertFalse(Settings.getAutosave(context));

        assertEquals(0, Settings.getNumTimesOpened(context));
        Settings.incrementNumTimesOpened(context);
        awaitPreferenceWrites();
        assertEquals(1, Settings.getNumTimesOpened(context));
        Settings.setTimesOpened(context, 8);
        awaitPreferenceWrites();
        assertEquals(8, Settings.getNumTimesOpened(context));
    }

    @Test
    public void malformedStoredValuesFallBackAndClamp() {
        putString(Settings.GAME_SIZE, "not-a-number");
        assertEquals(7, Settings.getGridSize(context));

        putString(Settings.GAME_SIZE, "0");
        putString(Settings.CUSTOM_GAME_SIZE, "-100");
        assertEquals(Settings.MIN_BOARD_SIZE, Settings.getGridSize(context));
        putString(Settings.CUSTOM_GAME_SIZE, "100");
        assertEquals(Settings.MAX_BOARD_SIZE, Settings.getGridSize(context));

        putString(Settings.TIMER_TYPE, "99");
        putString(Settings.TIMER, "-5");
        putString(Settings.DIFFICULTY, "broken");
        assertEquals(Timer.ENTIRE_MATCH, Settings.getTimerType(context));
        assertEquals(0, Settings.getTimeAmount(context));
        assertEquals(1, Settings.getComputerDifficulty(context));
    }

    @Test
    public void settingsUiPersistsEveryInteractiveSettingInLandscape() {
        try (ActivityScenario<PreferencesActivity> scenario =
                     ActivityScenario.launch(PreferencesActivity.class)) {
            scenario.onActivity(activity -> assertEquals(
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                    activity.getRequestedOrientation()));

            onView(withText(R.string.preferences_title_game_size)).perform(click());
            onView(withText("9x9")).perform(click());
            assertEquals(9, Settings.getGridSize(context));

            onView(withText(R.string.preferences_title_swap)).perform(click());
            assertFalse(Settings.getSwap(context));

            onView(withText(R.string.preferences_title_timer)).perform(click());
            onView(withId(R.id.timerType)).perform(click());
            onView(withText("Per move"))
                    .inRoot(isPlatformPopup())
                    .perform(click());
            onView(withId(R.id.timer)).perform(replaceText("45"));
            onView(withText(R.string.okay)).perform(click());
            assertEquals(Timer.PER_MOVE, Settings.getTimerType(context));
            assertEquals(45, Settings.getTimeAmount(context));

            scrollToPreference(R.string.preferences_title_com_difficulty);
            onView(withText(R.string.preferences_title_com_difficulty)).perform(click());
            onView(withText("Tree (experimental)")).perform(click());
            assertEquals(AndroidBotFactory.TREE, Settings.getComputerDifficulty(context));

            scrollToPreference(R.string.preferences_title_autosave);
            onView(withText(R.string.preferences_title_autosave))
                    .check(matches(isDisplayed()))
                    .perform(click());
            assertFalse(Settings.getAutosave(context));

            scenario.recreate();
            assertEquals(9, Settings.getGridSize(context));
            assertFalse(Settings.getSwap(context));
            assertEquals(Timer.PER_MOVE, Settings.getTimerType(context));
            assertEquals(45, Settings.getTimeAmount(context));
            assertEquals(AndroidBotFactory.TREE, Settings.getComputerDifficulty(context));
            assertFalse(Settings.getAutosave(context));
        }
    }

    private void scrollToPreference(int title) {
        onView(withId(androidx.preference.R.id.recycler_view)).perform(
                RecyclerViewActions.scrollTo(
                        hasDescendant(withText(title))));
    }

    private void putString(String key, String value) {
        assertTrue(preferences.edit().putString(key, value).commit());
    }

    private void awaitPreferenceWrites() {
        assertTrue(preferences.edit().commit());
    }
}
