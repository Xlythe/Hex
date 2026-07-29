package com.xlythe.hex.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.hex.core.Player;
import com.hex.core.PlayingEntity;
import com.xlythe.hex.R;
import com.xlythe.hex.compat.Game;

/** Lifecycle-safe game result dialog; no Activity or Fragment is held statically. */
public final class GameOverDialogFragment extends DialogFragment {
    static final String REQUEST_KEY = "game-over-action";
    static final String RESULT_ACTION = "action";
    static final String ACTION_PLAY_AGAIN = "play-again";
    static final String ACTION_HOME = "home";

    private static final String ARG_WINNER_NAME = "winner-name";
    private static final String ARG_LOCAL_WIN = "local-win";
    private static final String ARG_SHOW_WINNER = "show-winner";
    private static final String ARG_DURATION = "duration";

    public static GameOverDialogFragment create(Game game, PlayingEntity winner) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_WINNER_NAME, winner.getName());
        arguments.putBoolean(ARG_LOCAL_WIN, winner.getType() == Player.Human);
        arguments.putBoolean(
                ARG_SHOW_WINNER,
                game.getPlayer1().getType() == Player.Human
                        && game.getPlayer2().getType() == Player.Human);
        arguments.putLong(ARG_DURATION, game.getGameLength());

        GameOverDialogFragment fragment = new GameOverDialogFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = requireArguments();
        boolean localWin = arguments.getBoolean(ARG_LOCAL_WIN);
        String result = getString(localWin ? R.string.game_over_won : R.string.game_over_lose);
        long duration = arguments.getLong(ARG_DURATION);
        long hours = duration / (60 * 60 * 1000);
        long minutes = duration / (60 * 1000) - hours * 60;
        long seconds = duration / 1000 - minutes * 60 - hours * 60 * 60;

        StringBuilder message = new StringBuilder();
        if (arguments.getBoolean(ARG_SHOW_WINNER)) {
            message.append(arguments.getString(ARG_WINNER_NAME, ""))
                    .append('\n');
        }
        message.append(getString(
                R.string.game_over_duration,
                getString(R.string.game_over_length, hours, minutes, seconds)));

        return new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.game_over_action, result))
                .setMessage(message)
                .setPositiveButton(R.string.game_over_play_again, (dialog, which) ->
                        sendAction(ACTION_PLAY_AGAIN))
                .setNegativeButton(R.string.game_over_home, (dialog, which) ->
                        sendAction(ACTION_HOME))
                .setCancelable(false)
                .create();
    }

    private void sendAction(String action) {
        Bundle result = new Bundle();
        result.putString(RESULT_ACTION, action);
        getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
    }
}
