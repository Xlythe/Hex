package com.sam.hex.fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.hex.ai.BeeGameAI;
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
import com.sam.hex.R;
import com.sam.hex.Settings;
import com.sam.hex.Stats;
import com.sam.hex.view.BoardView;

public class GameFragment extends SherlockFragment {
    public static final String GAME = "game";
    public static final String REPLAY = "replay";
    private static final SimpleDateFormat SAVE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    boolean mIsSignedIn = false;

    private Game game;
    private Player player1Type;
    private Player player2Type;
    private boolean replay;
    private int replayDuration;
    private long timeGamePaused;
    private long whenGamePaused;

    private BoardView board;
    private Button exit;
    private Button newGame;
    private Button undo;

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getMainActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
            initializeNewGame();
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

        board = (BoardView) v.findViewById(R.id.board);
        board.setGame(game);
        if(game.gameOptions.timer.type == 0 || game.isGameOver()) {
            board.setShowTimerText(false);
        }
        if(game.isGameOver() && game.getGameListener() != null) game.getGameListener().onWin(game.getCurrentPlayer());

        exit = (Button) v.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quit();
            }
        });
        newGame = (Button) v.findViewById(R.id.reload);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame();
            }
        });
        undo = (Button) v.findViewById(R.id.undo);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undo();
            }
        });

        return v;
    }

    protected void initializeNewGame() {
        // Stop the old game
        stopGame(game);
        timeGamePaused = 0;

        // Create a new game object
        GameOptions go = new GameOptions();
        go.gridSize = Settings.getGridSize(getMainActivity());
        go.swap = Settings.getSwap(getMainActivity());

        int timerType = Settings.getTimerType(getMainActivity());
        go.timer = new Timer(Settings.getTimeAmount(getMainActivity()), 0, timerType);

        GameListener gl = createGameListener();

        game = new Game(go, getPlayer(1, go.gridSize), getPlayer(2, go.gridSize));
        game.setGameListener(gl);

        setNames();
        setColors();

        game.gameOptions.timer.start(game);
    }

    private GameListener createGameListener() {
        return new GameListener() {
            @Override
            public void onWin(final PlayingEntity player) {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.setWinText(String.format(getString(R.string.winner), player.getName()));
                        board.setShowWinText(true);
                        board.setShowTimerText(false);
                        board.invalidate();

                        if(replay) return;

                        // Auto save completed game
                        if(Settings.getAutosave(getMainActivity())) {
                            try {
                                String fileName = String.format(getString(R.string.auto_saved_game_name), SAVE_FORMAT.format(new Date()), game.getPlayer1()
                                        .getName(), game.getPlayer2().getName());
                                FileUtil.autoSaveGame(fileName, game.save());
                            }
                            catch(IOException e) {
                                e.printStackTrace();
                            }
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
                        if(game.isGameOver()) board.setShowWinText(false);
                        board.postInvalidate();
                    }
                });
            }

            @Override
            public void onStop() {

            }

            @Override
            public void onTurn(PlayingEntity player) {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.postInvalidate();
                    }
                });
            }

            @Override
            public void onReplayStart() {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.setShowTimerText(false);
                        board.setShowWinText(false);
                        board.postInvalidate();
                    }
                });
            }

            @Override
            public void onReplayEnd() {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(game.gameOptions.timer.type != 0) board.setShowTimerText(true);
                        board.postInvalidate();
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
                        board.setShowTimerText(true);
                        board.postInvalidate();
                    }
                });
            }

            @Override
            public void displayTime(final int minutes, final int seconds) {
                if(getSherlockActivity() != null) getSherlockActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.setTimerText(String.format(getString(R.string.timer), String.format("%d:%02d", minutes, seconds)));
                        board.postInvalidate();
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
            initializeNewGame();
        }
        else {// Apply minor changes without stopping the current game
            setColors();
            setNames();
            game.getMoveList().replay(0, game);
            GameAction.checkedFlagReset(game);
            GameAction.checkWinPlayer(1, game);
            GameAction.checkWinPlayer(2, game);
            GameAction.checkedFlagReset(game);

            // Apply everything
            board.invalidate();
        }
    }

    protected void showSavingDialog() {
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
    protected void setNames() {
        game.getPlayer1().setName(Settings.getPlayer1Name(getMainActivity(), getMainActivity().getGamesClient()));
        game.getPlayer2().setName(Settings.getPlayer2Name(getMainActivity()));
    }

    /**
     * Refreshes both player's colors Does not invalidate the board
     * */
    protected void setColors() {
        game.getPlayer1().setColor(Settings.getPlayer1Color(getMainActivity()));
        game.getPlayer2().setColor(Settings.getPlayer2Color(getMainActivity()));
    }

    public PlayingEntity getPlayer(int team, int gridSize) {
        Player p = (team == 1) ? player1Type : player2Type;
        switch(p) {
        case AI:
            return new BeeGameAI(team, gridSize);
        case Human:
            return new PlayerObject(team);
        case Net:
            return new PlayerObject(team);
        default:
            return new PlayerObject(team);
        }
    }

    protected void undo() {
        GameAction.undo(GameAction.LOCAL_GAME, game);
    }

    protected void newGame() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    if(game.getPlayer1().supportsNewgame() && game.getPlayer2().supportsNewgame()) {
                        game.replayRunning = false;
                        stopGame(game);
                        game.clearBoard();
                        game.start();
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

    public void setPlayer1Type(Player player1Type) {
        this.player1Type = player1Type;
    }

    public void setPlayer2Type(Player player2Type) {
        this.player2Type = player2Type;
    }
}
