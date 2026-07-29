package com.xlythe.hex;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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

import com.xlythe.hex.view.HexDialogView;
import com.xlythe.hex.view.HexagonLayout;
import com.xlythe.hex.view.SelectorLayout;

/**
 * Golden-image coverage for every XML-backed screen in the application.
 *
 * <p>The fixed landscape Pixel 5 configuration matches the orientation contract
 * used by the real application. Programmatic dialogs are covered by their content
 * layouts; interaction behavior remains in the regular JVM tests.</p>
 */
public final class ScreenshotTest {
    @Rule
    public final Paparazzi paparazzi = new Paparazzi(
            EnvironmentKt.detectEnvironment(),
            TestDeviceConfigs.PHONE_LANDSCAPE);

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
        snapshot(R.layout.fragment_instructions);
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
        snapshot(R.layout.fragment_game);
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
        snapshot(dialog);
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
