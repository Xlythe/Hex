package com.xlythe.hex;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Field;

import app.cash.paparazzi.Paparazzi;
import app.cash.paparazzi.EnvironmentKt;

import org.junit.Rule;
import org.junit.Test;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hex.core.PlayerObject;
import com.hex.core.Timer;
import com.xlythe.hex.compat.Game;
import com.xlythe.hex.view.BoardView;
import com.xlythe.hex.view.HexDialogView;
import com.xlythe.hex.view.HexagonLayout;
import com.xlythe.hex.view.SelectorLayout;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;

/**
 * Golden-image coverage for every XML-backed screen in the application.
 *
 * <p>The fixed landscape Pixel 5 configuration matches the orientation contract
 * used by the real application. Programmatic dialogs are covered by their content
 * layouts; interaction behavior remains in the regular JVM tests.</p>
 */
public class ScreenshotTest {
    @Rule
    public final Paparazzi paparazzi;

    public ScreenshotTest() {
        this(TestDeviceConfigs.PHONE_LANDSCAPE);
    }

    protected ScreenshotTest(app.cash.paparazzi.DeviceConfig deviceConfig) {
        paparazzi = new Paparazzi(EnvironmentKt.detectEnvironment(), deviceConfig);
    }

    @Test
    public void mainMenu() {
        View view = inflate(R.layout.fragment_main);
        HexagonLayout menu = view.findViewById(R.id.hexagonButtons);
        menu.setText(R.string.app_name);
        configure(menu.getButtons()[0], R.string.main_button_settings,
                R.color.main_settings, R.drawable.settings);
        configure(menu.getButtons()[1], R.string.main_button_donate,
                R.color.main_donate, R.drawable.store);
        configure(menu.getButtons()[2], R.string.main_button_history,
                R.color.main_history, R.drawable.history);
        configure(menu.getButtons()[3], R.string.main_button_instructions,
                R.color.main_instructions, R.drawable.howtoplay);
        configure(menu.getButtons()[4], R.string.main_button_achievements,
                R.color.main_achievements, R.drawable.achievements);
        configure(menu.getButtons()[5], R.string.main_button_play,
                R.color.main_play, R.drawable.play);
        TextView title = view.findViewById(R.id.title);
        title.setText(paparazzi.getContext().getString(R.string.main_title, "Player 1"));
        snapshot(view);
    }

    @Test
    public void mainMenuCarouselRotatesWhenSwiped() throws ReflectiveOperationException {
        View view = inflate(R.layout.fragment_main);
        HexagonLayout menu = view.findViewById(R.id.hexagonButtons);
        menu.setText(R.string.app_name);
        for (int index = 0; index < menu.getButtons().length; index++) {
            configure(menu.getButtons()[index], R.string.main_button_play,
                    R.color.main_play, R.drawable.play);
        }
        snapshot(view, "carousel_before_swipe");

        float originalRotation = rotationOf(menu);
        long downTime = 1_000L;
        dispatch(menu, downTime, downTime, MotionEvent.ACTION_DOWN,
                menu.getWidth() * 0.75f, menu.getHeight() * 0.25f);
        dispatch(menu, downTime, downTime + 16L, MotionEvent.ACTION_MOVE,
                menu.getWidth() * 0.55f, menu.getHeight() * 0.08f);

        assertNotEquals(
                "The deliberately oversized menu must remain swipe-rotatable",
                originalRotation,
                rotationOf(menu),
                0.01f);
    }

    @Test
    public void mainMenuCenterTouchDoesNotSpinOrActivateASection() throws ReflectiveOperationException {
        View view = inflate(R.layout.fragment_main);
        HexagonLayout menu = view.findViewById(R.id.hexagonButtons);
        menu.setText(R.string.app_name);
        for (int index = 0; index < menu.getButtons().length; index++) {
            configure(menu.getButtons()[index], R.string.main_button_play,
                    R.color.main_play, R.drawable.play);
        }
        snapshot(view, "carousel_center_touch");
        float rotation = rotationOf(menu);
        long downTime = 1_000L;
        Field centerField = HexagonLayout.class.getDeclaredField("center");
        centerField.setAccessible(true);
        com.hex.core.Point center = (com.hex.core.Point) centerField.get(menu);
        float centerX = center.x;
        float centerY = center.y;
        dispatch(menu, downTime, downTime, MotionEvent.ACTION_DOWN, centerX, centerY);
        dispatch(menu, downTime, downTime + 16L, MotionEvent.ACTION_MOVE, centerX + 1, centerY);
        dispatch(menu, downTime, downTime + 32L, MotionEvent.ACTION_UP, centerX + 1, centerY);
        assertEquals(rotation, rotationOf(menu), 0.01f);
    }

    @Test
    public void gameSelection() {
        View view = inflate(R.layout.fragment_game_selection);
        SelectorLayout selector = view.findViewById(R.id.buttons);
        configure(selector.getButtons()[0], R.string.game_selection_button_computer,
                R.color.select_computer);
        configure(selector.getButtons()[1], R.string.game_selection_button_pass,
                R.color.select_pass_to_play);
        configure(selector.getButtons()[2], R.string.game_selection_button_net,
                R.color.select_online);
        snapshot(view);
    }

    @Test
    public void instructions() {
        View view = inflate(R.layout.fragment_instructions);
        ((TextView) view.findViewById(R.id.title))
                .setText(R.string.main_button_instructions);
        snapshot(view);
    }

    @Test
    public void history() {
        snapshot(R.layout.fragment_history);
    }

    @Test
    public void onlineSelection() {
        View view = inflate(R.layout.fragment_online_selection);
        SelectorLayout selector = view.findViewById(R.id.buttons);
        configure(selector.getButtons()[0], R.string.online_selection_button_quick,
                R.color.select_quick_game);
        configure(selector.getButtons()[1], R.string.online_selection_button_invite,
                R.color.select_friends);
        configure(selector.getButtons()[2], R.string.online_selection_button_pending,
                R.color.select_pending_invites);
        snapshot(view);
    }

    @Test
    public void gameBoard() {
        View view = inflate(R.layout.fragment_game);
        BoardView board = view.findViewById(R.id.board);
        PlayerObject player1 = new PlayerObject(1);
        player1.setName("Player 1");
        player1.setColor(ContextCompat.getColor(
                paparazzi.getContext(), R.color.main_settings));
        PlayerObject player2 = new PlayerObject(2);
        player2.setName("Player 2");
        player2.setColor(ContextCompat.getColor(
                paparazzi.getContext(), R.color.main_play));
        Game.GameOptions options = new Game.GameOptions();
        options.gridSize = 11;
        options.swap = true;
        options.timer = new Timer(0, 0, Timer.NO_TIMER);
        board.setTitleText("%s");
        board.setActionText("Your turn");
        board.setTimerText("Time left");
        board.setGame(new Game(options, player1, player2));
        snapshot(view);
    }

    @Test
    public void preferencesActivity() {
        View view = inflate(R.layout.preferences);
        TextView title = view.findViewById(R.id.title);
        title.setText(R.string.activity_title_preferences);

        PreferenceManager preferenceManager =
                new PreferenceManager(paparazzi.getContext());
        PreferenceScreen preferences = preferenceManager.inflateFromResource(
                paparazzi.getContext(), R.xml.preferences_general, null);
        preferences.findPreference(Settings.GAME_SIZE)
                .setSummary(paparazzi.getContext().getString(
                        R.string.preferences_summary_game_size, 11, 11));
        preferences.findPreference(Settings.DIFFICULTY)
                .setSummary(paparazzi.getContext().getResources()
                        .getStringArray(R.array.comDifficultyArray)[2]);

        RecyclerView list = new RecyclerView(paparazzi.getContext());
        list.setLayoutManager(new LinearLayoutManager(paparazzi.getContext()));
        list.setAdapter(new PreferenceGroupAdapter(preferences));
        FrameLayout content = view.findViewById(R.id.content);
        content.removeAllViews();
        content.addView(list, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        snapshot(view);
        list.scrollToPosition(list.getAdapter().getItemCount() - 1);
        snapshot(view, "bottom");
    }

    @Test
    public void timerPreferencesDialog() {
        snapshot(R.layout.preferences_timer);
    }

    @Test
    public void donationDialog() {
        HexDialogView dialog = new HexDialogView(paparazzi.getContext());
        configureDonation(dialog.getButtons()[0], R.drawable.donate_bronze_d,
                R.string.donate_bronze, R.string.donate_bronze_price,
                0.20f, 0.70f, 0.13f);
        configureDonation(dialog.getButtons()[1], R.drawable.donate_silver_d,
                R.string.donate_silver, R.string.donate_silver_price,
                0.77f, 0.65f, 0.14f);
        configureDonation(dialog.getButtons()[2], R.drawable.donate_gold_d,
                R.string.donate_gold, R.string.donate_gold_price,
                0.45f, 0.30f, 0.15f);
        dialog.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        // HexDialogView starts lifecycle-bound animators when Paparazzi attaches
        // it. Capture a fixed, fully-open frame instead of whichever animation
        // phase the host happens to render first.
        paparazzi.snapshot(dialog, null, 300_000_000L);
    }

    @Test
    public void gameChatDialog() {
        snapshot(R.layout.dialog_game_chat);
    }

    private void snapshot(int layoutResource) {
        snapshot(inflate(layoutResource));
    }

    private View inflate(int layoutResource) {
        LayoutInflater inflater = LayoutInflater.from(paparazzi.getContext());
        FrameLayout parent = new FrameLayout(paparazzi.getContext());
        View view = inflater.inflate(layoutResource, parent, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
    }

    private void snapshot(View view) {
        paparazzi.snapshot(view);
    }

    private void snapshot(View view, String name) {
        paparazzi.snapshot(view, name);
    }

    private void dispatch(
            View view,
            long downTime,
            long eventTime,
            int action,
            float x,
            float y) {
        MotionEvent event = MotionEvent.obtain(
                downTime, eventTime, action, x, y, 0);
        try {
            view.dispatchTouchEvent(event);
        } finally {
            event.recycle();
        }
    }

    private float rotationOf(HexagonLayout menu)
            throws ReflectiveOperationException {
        Field rotation = HexagonLayout.class.getDeclaredField("mRotation");
        rotation.setAccessible(true);
        return rotation.getFloat(menu);
    }

    private void configure(
            SelectorLayout.Button button,
            int textResource,
            int colorResource) {
        button.setText(textResource);
        button.setColor(ContextCompat.getColor(paparazzi.getContext(), colorResource));
    }

    private void configure(
            HexagonLayout.Button button,
            int textResource,
            int colorResource,
            int drawableResource) {
        button.setText(textResource);
        button.setColor(ContextCompat.getColor(paparazzi.getContext(), colorResource));
        button.setDrawableResource(drawableResource);
    }

    private void configureDonation(
            HexDialogView.Button button,
            int imageResource,
            int textResource,
            int priceResource,
            float centerX,
            float centerY,
            float sideLength) {
        View card = inflate(R.layout.dialog_view_donate);
        ((ImageView) card.findViewById(R.id.image)).setImageResource(imageResource);
        ((TextView) card.findViewById(R.id.text)).setText(textResource);
        ((TextView) card.findViewById(R.id.price)).setText(priceResource);
        button.setView(card);
        button.setCenterXPercent(centerX);
        button.setCenterYPercent(centerY);
        button.setSideLengthPercent(sideLength);
    }
}
