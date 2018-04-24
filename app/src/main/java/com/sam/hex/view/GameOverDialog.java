package com.sam.hex.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hex.core.Game;
import com.hex.core.Player;
import com.hex.core.PlayingEntity;
import com.sam.hex.R;
import com.sam.hex.fragment.GameFragment;
import com.sam.hex.view.HexDialogView.Button.OnClickListener;

/**
 * @author Will Harmon
 **/
public class GameOverDialog extends HexDialog {
    private static GameFragment gameFragment;
    private static PlayingEntity winner;

    public static class Builder {
        private final GameOverDialog gameOverDialog = new GameOverDialog();

        public Builder(Context context) {
            gameOverDialog.attachBaseContext(context);
        }

        public Builder setGameFragment(GameFragment fragment) {
            gameFragment = fragment;
            return this;
        }

        public Builder setWinner(PlayingEntity playingEntity) {
            winner = playingEntity;
            return this;
        }

        public void show() {
            gameOverDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        gameFragment = null;
        winner = null;

        super.onDestroy();
    }

    @Override
    public View getPositiveView() {
        View v = View.inflate(this, R.layout.dialog_view_game_over_icon, null);

        ImageView iv = v.findViewById(R.id.image);
        iv.setImageResource(R.drawable.play_again);

        return v;
    }

    @Override
    public View getNegativeView() {
        View v = View.inflate(this, R.layout.dialog_view_game_over_icon, null);

        ImageView iv = v.findViewById(R.id.image);
        iv.setImageResource(R.drawable.home);

        return v;
    }

    @Override
    public View getNeutralView() {
        View v = View.inflate(this, R.layout.dialog_view_game_over, null);

        TextView action = v.findViewById(R.id.action);
        TextView time = v.findViewById(R.id.time);

        Game game = gameFragment.getGame();
        PlayingEntity winner = this.winner;

        String actionText = winner.getType().equals(Player.Human) ? getString(R.string.game_over_won) : getString(R.string.game_over_lose);
        long hours = game.getGameLength() / (60 * 60 * 1000);
        long minutes = game.getGameLength() / (60 * 1000) - hours * 60;
        long seconds = game.getGameLength() / (1000) - minutes * 60 - hours * 60 * 60;

        action.setText(getString(R.string.game_over_action, actionText));
        time.setText(getString(R.string.game_over_length, hours, minutes, seconds));

        if (game.getPlayer1().getType().equals(Player.Human) && game.getPlayer2().getType().equals(Player.Human)) {
            TextView player = v.findViewById(R.id.player);
            player.setText(winner.getName());
        }

        return v;
    }

    @NonNull
    @Override
    public OnClickListener getPositiveOnClickListener() {
        return () -> {
            gameFragment.startNewGame();
            dismiss();
        };
    }

    @NonNull
    @Override
    public OnClickListener getNegativeOnClickListener() {
        return () -> {
            gameFragment.setGoHome(true);
            dismiss();
        };
    }

    @Nullable
    @Override
    public OnClickListener getNeutralOnClickListener() {
        return null;
    }

    @Override
    public float getPositiveXPercent() {
        return 0.7234375f;
    }

    @Override
    public float getPositiveYPercent() {
        return 0.70442708f;
    }

    @Override
    public float getPositiveSideLengthPercent() {
        return 0.090625f;
    }

    @Override
    public float getNegativeXPercent() {
        return 0.18359375f;
    }

    @Override
    public float getNegativeYPercent() {
        return 0.47005208f;
    }

    @Override
    public float getNegativeSideLengthPercent() {
        return 0.074609375f;
    }

    @Override
    public float getNeutralXPercent() {
        return 0.4921875f;
    }

    @Override
    public float getNeutralYPercent() {
        return 0.3125f;
    }

    @Override
    public float getNeutralSideLengthPercent() {
        return 0.178125f;
    }
}
