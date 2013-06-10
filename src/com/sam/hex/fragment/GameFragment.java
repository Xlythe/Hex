package com.sam.hex.fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.hex.ai.BeeGameAI;
import com.hex.ai.GameAI;
import com.hex.core.Game;
import com.hex.core.Game.GameListener;
import com.hex.core.Game.GameOptions;
import com.hex.core.GameAction;
import com.hex.core.Player;
import com.hex.core.PlayerObject;
import com.hex.core.PlayingEntity;
import com.hex.core.Timer;
import com.sam.hex.FileUtil;
import com.sam.hex.MainActivity;
import com.sam.hex.PreferencesActivity;
import com.sam.hex.R;
import com.sam.hex.Stats;
import com.sam.hex.view.BoardView;

public class GameFragment extends SherlockFragment {
    public static final String GAME = "game";
    public static final String REPLAY = "replay";
    private static final SimpleDateFormat SAVE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    boolean mIsSignedIn = false;

    private Game game;
    private boolean replay;
    private int replayDuration;
    private long timeGamePaused;
    private long whenGamePaused;

    BoardView board;
    ImageButton player1Icon;
    ImageButton player2Icon;
    TextView timerText;
    TextView winnerText;
    ImageButton replayForward;
    ImageButton replayPlayPause;
    ImageButton replayBack;
    RelativeLayout replayButtons;

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getSherlockActivity().getSupportActionBar().show();
        ((SherlockFragmentActivity) getSherlockActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
        getSherlockActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(savedInstanceState != null && savedInstanceState.containsKey(GAME)) {
            // Resume a game if one exists
            game = Game.load(savedInstanceState.getString(GAME));
            game.setGameListener(createGameListener());
            replay = true;
            replayDuration = 0;

            if(savedInstanceState.containsKey(REPLAY) && savedInstanceState.getBoolean(REPLAY)) {
                replayDuration = 900;
            }
        }
        else if(getArguments() != null && getArguments().containsKey(GAME)) {
            // Resume a game if one exists
            game = Game.load(getArguments().getString(GAME));
            game.setGameListener(createGameListener());
            replay = true;
            replayDuration = 0;

            if(getArguments().containsKey(REPLAY) && getArguments().getBoolean(REPLAY)) {
                replayDuration = 900;
            }
        }
        else {
            // Create a new game
            initializeNewGame(inflater);
        }

        // Load the UI
        return applyBoard(inflater);
    }

    @Override
    public void onPause() {
        super.onPause();
        whenGamePaused = System.currentTimeMillis();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopGame(game);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        game.start();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if(game != null) savedInstanceState.putString(GAME, game.save());
    }

    private View applyBoard(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.game, null);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            timerText = (TextView) v.findViewById(R.id.timer);
            winnerText = (TextView) v.findViewById(R.id.winner);
        }
        else {
            View message = inflater.inflate(R.layout.actionbar_message, null);
            getSherlockActivity().getSupportActionBar().setCustomView(message);
            getSherlockActivity().getSupportActionBar().setDisplayShowCustomEnabled(true);

            winnerText = (TextView) message.findViewById(R.id.winner);
            timerText = (TextView) message.findViewById(R.id.timer);
        }
        board = (BoardView) v.findViewById(R.id.board);
        board.setGame(game);
        player1Icon = (ImageButton) v.findViewById(R.id.p1);
        player2Icon = (ImageButton) v.findViewById(R.id.p2);
        if(game.gameOptions.timer.type == 0 || game.isGameOver()) {
            timerText.setVisibility(View.GONE);
        }
        if(game.isGameOver() && game.getGameListener() != null) game.getGameListener().onWin(game.getCurrentPlayer());

        replayForward = (ImageButton) v.findViewById(R.id.replayForward);
        replayPlayPause = (ImageButton) v.findViewById(R.id.replayPlayPause);
        replayBack = (ImageButton) v.findViewById(R.id.replayBack);
        replayButtons = (RelativeLayout) v.findViewById(R.id.replayButtons);

        return v;
    }

    private void initializeNewGame(LayoutInflater inflater) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());

        // Stop the old game
        stopGame(game);
        timeGamePaused = 0;

        // Create a new game object
        GameOptions go = new GameOptions();
        go.gridSize = setGrid(prefs, GameAction.LOCAL_GAME);
        go.swap = prefs.getBoolean("swapPref", true);

        int timerType = Integer.parseInt(prefs.getString("timerTypePref", "0"));
        go.timer = new Timer(Integer.parseInt(prefs.getString("timerPref", "0")), 0, timerType);

        GameListener gl = createGameListener();

        game = new Game(go, getPlayer(getPlayer1Type(prefs, GameAction.LOCAL_GAME), 1, go.gridSize), getPlayer(getPlayer2Type(prefs, GameAction.LOCAL_GAME), 2,
                go.gridSize));
        game.setGameListener(gl);

        setNames(prefs, GameAction.LOCAL_GAME, game);
        setColors(prefs, GameAction.LOCAL_GAME, game);

        applyBoard(inflater);
        game.gameOptions.timer.start(game);
    }

    private GameListener createGameListener() {
        return new GameListener() {
            @Override
            public void onWin(final PlayingEntity player) {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String winnerMsg = String.format(getString(R.string.winner), player.getName());
                        winnerText.setText(winnerMsg);
                        winnerText.setVisibility(View.VISIBLE);
                        winnerText.invalidate();
                        timerText.setVisibility(View.GONE);
                        timerText.invalidate();
                        board.invalidate();

                        if(replay) return;

                        // Auto save completed game
                        try {
                            String fileName = String.format(getString(R.string.auto_saved_game_name), game.getPlayer1().getName(), game.getPlayer2().getName(),
                                    SAVE_FORMAT.format(new Date()), player.getName());
                            FileUtil.autoSaveGame(fileName, game.save());
                        }
                        catch(IOException e) {
                            e.printStackTrace();
                        }

                        Stats.incrementTimePlayed(getSherlockActivity(), game.getGameLength() - timeGamePaused);
                        Stats.incrementGamesPlayed(getSherlockActivity());
                        if(player.getTeam() == 1) Stats.incrementGamesWon(getSherlockActivity());

                        if(getMainActivity().isSignedIn()) {
                            // Unlock the quick play achievements!
                            if(game.getGameLength() < 30 * 1000) {
                                getMainActivity().getGamesClient().unlockAchievement(getString(R.string.achievement_30_seconds));
                            }
                            if(game.getGameLength() < 10 * 1000) {
                                getMainActivity().getGamesClient().unlockAchievement(getString(R.string.achievement_10_seconds));
                            }

                            // Unlock the fill the board achievement!
                            boolean boardFilled = true;
                            for(int i = 0; i < game.gameOptions.gridSize; i++) {
                                for(int j = 0; j < game.gameOptions.gridSize; j++) {
                                    if(game.gamePieces[i][j].getTeam() == 0) boardFilled = false;
                                }
                            }
                            if(boardFilled) {
                                getMainActivity().getGamesClient().unlockAchievement(getString(R.string.achievement_fill_the_board));
                            }

                            // Unlock the montior smasher achievement!
                            if(player.getTeam() == 1 && game.getPlayer2().getType().equals(Player.AI)) {
                                getMainActivity().getGamesClient().unlockAchievement(getString(R.string.achievement_monitor_smasher));
                            }

                            // Unlock the speed demon achievement!
                            if(game.gameOptions.timer.type != 0) {
                                getMainActivity().getGamesClient().unlockAchievement(getString(R.string.achievement_monitor_smasher));
                            }

                            // Unlock the Novice achievement!
                            getMainActivity().getGamesClient().incrementAchievement(getString(R.string.achievement_novice), 1);

                            // Unlock the Intermediate achievement!
                            getMainActivity().getGamesClient().incrementAchievement(getString(R.string.achievement_intermediate), 1);

                            // Unlock the Expert achievement!
                            if(player.getTeam() == 1) getMainActivity().getGamesClient().incrementAchievement(getString(R.string.achievement_expert), 1);

                            // Unlock the Expert achievement!
                            if(player.getTeam() == 1) getMainActivity().getGamesClient().incrementAchievement(getString(R.string.achievement_insane), 1);
                        }
                    }
                });
            }

            @Override
            public void onClear() {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.postInvalidate();
                    }
                });
            }

            @Override
            public void onStart() {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(game.isGameOver()) winnerText.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onStop() {

            }

            @Override
            public void onTurn(PlayingEntity player) {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void run() {
                        board.postInvalidate();
                        player1Icon.setColorFilter(game.getPlayer1().getColor());
                        player2Icon.setColorFilter(game.getPlayer2().getColor());
                        if(game.getCurrentPlayer().getTeam() == 1 && !game.isGameOver()) {
                            player1Icon.setAlpha(255);
                            player2Icon.setAlpha(80);
                        }
                        else if(game.getCurrentPlayer().getTeam() == 2 && !game.isGameOver()) {
                            player1Icon.setAlpha(80);
                            player2Icon.setAlpha(255);
                        }
                        else {
                            player1Icon.setAlpha(80);
                            player2Icon.setAlpha(80);
                        }
                    }
                });
            }

            @Override
            public void onReplayStart() {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.postInvalidate();
                        timerText.setVisibility(View.GONE);
                        winnerText.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onReplayEnd() {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.postInvalidate();
                        if(game.gameOptions.timer.type != 0) timerText.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onUndo() {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.postInvalidate();
                    }
                });
            }

            @Override
            public void startTimer() {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timerText.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void displayTime(final int minutes, final int seconds) {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timerText.setText(String.format(getString(R.string.timer), String.format("%d:%02d", minutes, seconds)));
                        timerText.invalidate();
                    }
                });
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if(whenGamePaused != 0) {
            timeGamePaused += System.currentTimeMillis() - whenGamePaused;
            whenGamePaused = 0;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());

        // Check if settings were changed and we need to run a new game
        if(game != null && game.replayRunning) {
            // Do nothing
        }
        else if(replay) {
            replay = false;
            replay(replayDuration);
        }
        else if(somethingChanged(prefs, GameAction.LOCAL_GAME, game)) {
            LayoutInflater inflater = getLayoutInflater(null);
            initializeNewGame(inflater);
            applyBoard(inflater);
        }
        else {// Apply minor changes without stopping the current game
            setColors(prefs, GameAction.LOCAL_GAME, game);
            setNames(prefs, GameAction.LOCAL_GAME, game);
            game.getMoveList().replay(0, game);
            GameAction.checkedFlagReset(game);
            GameAction.checkWinPlayer(1, game);
            GameAction.checkWinPlayer(2, game);
            GameAction.checkedFlagReset(game);

            // Apply everything
            board.invalidate();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_game_local, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch(item.getItemId()) {
        case android.R.id.home:
            getFragmentManager().popBackStackImmediate();
            return true;
        case R.id.settings:
            game.replayRunning = false;
            startActivity(new Intent(getSherlockActivity(), PreferencesActivity.class));
            return true;
        case R.id.undo:
            undo();
            return true;
        case R.id.newgame:
            newGame();
            return true;
        case R.id.replay:
            replay(900);
            return true;
        case R.id.save:
            showSavingDialog();
            return true;
        case R.id.quit:
            quit();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void showSavingDialog() {
        final EditText editText = new EditText(getSherlockActivity());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setText(SAVE_FORMAT.format(new Date()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
        builder.setTitle(R.string.enterFilename).setView(editText).setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    FileUtil.saveGame(editText.getText().toString(), game.save());
                    Toast.makeText(getSherlockActivity(), R.string.saved, Toast.LENGTH_SHORT).show();
                }
                catch(IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getSherlockActivity(), R.string.failed, Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton(R.string.cancel, null).show();
    }

    /**
     * Terminates the game
     * */
    public void stopGame(Game game) {
        if(game != null) {
            game.stop();
        }
    }

    /**
     * Refreshes both player's names Does not invalidate the board
     * */
    public void setNames(SharedPreferences prefs, int gameLocation, Game game) {
        if(gameLocation == GameAction.LOCAL_GAME) {
            // Playing on the same phone
            String p1 = prefs.getString("player1Name", getString(R.string.DEFAULT_P1_NAME));
            if(getMainActivity().isSignedIn()) p1 = getMainActivity().getGamesClient().getCurrentPlayer().getDisplayName();
            game.getPlayer1().setName(p1);
            game.getPlayer2().setName(prefs.getString("player2Name", getString(R.string.DEFAULT_P2_NAME)));
        }
        else if(gameLocation == GameAction.NET_GAME) {
            // // Playing over the net
            // for(int i = 0; i < NetGlobal.members.size(); i++) {
            // if(NetGlobal.members.get(i).place == 1) {
            // game.player1.setName(NetGlobal.members.get(i).name);
            // }
            // else if(NetGlobal.members.get(i).place == 2) {
            // game.player2.setName(NetGlobal.members.get(i).name);
            // }
            // }
        }
    }

    /**
     * Refreshes both player's colors Does not invalidate the board
     * */
    public void setColors(SharedPreferences prefs, int gameLocation, Game game) {
        if(gameLocation == GameAction.LOCAL_GAME) {
            // Playing on the same phone
            game.getPlayer1().setColor(prefs.getInt("player1Color", getResources().getInteger(R.integer.DEFAULT_P1_COLOR)));
            game.getPlayer2().setColor(prefs.getInt("player2Color", getResources().getInteger(R.integer.DEFAULT_P2_COLOR)));
        }
        else if(gameLocation == GameAction.NET_GAME) {
            // Playing on the net
            game.getPlayer1().setColor(0);
            game.getPlayer2().setColor(0);
        }
    }

    public int setGrid(SharedPreferences prefs, int gameLocation) {
        int gridSize = 0;
        if(gameLocation == GameAction.LOCAL_GAME) {
            // Playing on the same phone
            gridSize = Integer.valueOf(prefs.getString("gameSizePref", getString(R.integer.DEFAULT_BOARD_SIZE)));
            if(gridSize == 0) gridSize = Integer.valueOf(prefs.getString("customGameSizePref", getString(R.integer.DEFAULT_BOARD_SIZE)));
        }
        else if(gameLocation == GameAction.NET_GAME) {
            // Playing over the net
            gridSize = 7;
        }

        // We don't want 0x0 games
        if(gridSize <= 0) gridSize = 1;

        return gridSize;
    }

    public int getPlayer1Type(SharedPreferences prefs, int gameLocation) {
        if(gameLocation == GameAction.LOCAL_GAME) {
            return Integer.parseInt(prefs.getString("player1Type", "1"));
        }
        return 0;
    }

    public int getPlayer2Type(SharedPreferences prefs, int gameLocation) {
        if(gameLocation == GameAction.LOCAL_GAME) {
            return Integer.parseInt(prefs.getString("player2Type", "0"));
        }
        return 0;
    }

    public PlayingEntity getPlayer(int type, int team, int gridSize) {
        switch(type) {
        case 0:
            return new PlayerObject(team);
        case 1:
            return new GameAI(team);
        case 4:
            return new BeeGameAI(team, gridSize);
        }
        return null;
    }

    private void undo() {
        GameAction.undo(GameAction.LOCAL_GAME, game);
    }

    private void newGame() {
 
    	
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {        	
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    if(game.getPlayer1().supportsNewgame() && game.getPlayer2().supportsNewgame()) {
                    	final View insertPoint =getView();
                        game.replayRunning = false;
                        LayoutInflater inflater = getLayoutInflater(null);
                        initializeNewGame(inflater);
                        View v = applyBoard(inflater);  
                        ((FrameLayout)insertPoint).removeAllViews();
                        ((FrameLayout)insertPoint).addView(v);
                        GameListener gl = createGameListener();
                        game.setGameListener(gl);

                       
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    // Do nothing
                    break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
        builder.setMessage(getString(R.string.confirmNewgame)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    /**
     * Returns true if a major setting was changed
     * */
    public boolean somethingChanged(SharedPreferences prefs, int gameLocation, Game game) {
        if(game == null) return true;
        if(game.gameOptions.gridSize == 1) return true;
        if(gameLocation == GameAction.LOCAL_GAME) {
            return (Integer.valueOf(prefs.getString("gameSizePref", getString(R.integer.DEFAULT_BOARD_SIZE))) != game.gameOptions.gridSize && Integer
                    .valueOf(prefs.getString("gameSizePref", getString(R.integer.DEFAULT_BOARD_SIZE))) != 0)
                    || (Integer.valueOf(prefs.getString("customGameSizePref", getString(R.integer.DEFAULT_BOARD_SIZE))) != game.gameOptions.gridSize && Integer
                            .valueOf(prefs.getString("gameSizePref", getString(R.integer.DEFAULT_BOARD_SIZE))) == 0)
                    || Integer.valueOf(prefs.getString("timerTypePref", getString(R.integer.DEFAULT_TIMER_TYPE))) != game.gameOptions.timer.type
                    || Integer.valueOf(prefs.getString("timerPref", getString(R.integer.DEFAULT_TIMER_TIME))) * 60 * 1000 != game.gameOptions.timer.totalTime;
        }
        else if(gameLocation == GameAction.NET_GAME) {
            return(game != null && game.isGameOver());
        }
        else {
            return true;
        }
    }

    private void replay(int time) {
        game.replay(time);
    }

    private void quit() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    stopGame(game);
                    getMainActivity().swapFragmentWithoutBackStack(getMainActivity().getMainFragment());
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    // Do nothing
                    break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
        builder.setMessage(getString(R.string.confirmExit)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    public MainActivity getMainActivity() {
        return (MainActivity) getSherlockActivity();
    }
}
