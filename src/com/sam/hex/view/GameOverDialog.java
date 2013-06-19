package com.sam.hex.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.vending.billing.util.IabResult;
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
    private static GameFragment FRAGMENT;
    private static PlayingEntity WINNER;

    public GameOverDialog(Context context, GameFragment fragment, PlayingEntity winner) {
        super(context);
        FRAGMENT = fragment;
        WINNER = winner;
    }

    public GameOverDialog() {
        super();
    }

    @Override
    public View getPositiveView() {
        View v = View.inflate(this, R.layout.dialog_view_game_over_icon, null);

        ImageView iv = (ImageView) v.findViewById(R.id.image);
        iv.setImageResource(R.drawable.play_again);

        return v;
    }

    @Override
    public View getNegativeView() {
        View v = View.inflate(this, R.layout.dialog_view_game_over_icon, null);

        ImageView iv = (ImageView) v.findViewById(R.id.image);
        iv.setImageResource(R.drawable.home);

        return v;
    }

    @Override
    public View getNeutralView() {
        View v = View.inflate(this, R.layout.dialog_view_game_over, null);
        TextView action = (TextView) v.findViewById(R.id.action);
        TextView time = (TextView) v.findViewById(R.id.time);

        Game game = FRAGMENT.getGame();
        PlayingEntity winner = WINNER;

        String actionText = winner.getType().equals(Player.Human) ? getString(R.string.game_over_won) : getString(R.string.game_over_lose);
        long hours = game.getGameLength() / (60 * 60 * 1000);
        long minutes = game.getGameLength() / (60 * 1000) - hours * 60;
        long seconds = game.getGameLength() / (1000) - minutes * 60 - hours * 60 * 60;

        action.setText(getString(R.string.game_over_action, actionText));
        time.setText(getString(R.string.game_over_length, hours, minutes, seconds));

        if(game.getPlayer1().getType().equals(Player.Human) && game.getPlayer2().getType().equals(Player.Human)) {
            TextView player = (TextView) v.findViewById(R.id.player);
            player.setText(winner.getName());
        }

        return v;
    }

    @Override
    public OnClickListener getPositiveOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick() {
                FRAGMENT.startNewGame();
                dismiss();
            }
        };
    }

    @Override
    public OnClickListener getNegativeOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick() {
                FRAGMENT.setGoHome(true);
                dismiss();
            }
        };
    }

    @Override
    public OnClickListener getNeutralOnClickListener() {
        return null;
    }

    @Override
    protected void dealWithIabSetupSuccess() {}

    @Override
    protected void dealWithIabSetupFailure() {}

    @Override
    protected void dealWithPurchaseSuccess(IabResult result, String sku) {}

    @Override
    protected void dealWithPurchaseFailed(IabResult result) {}

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
