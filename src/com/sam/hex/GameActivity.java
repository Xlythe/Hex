package com.sam.hex;

import java.io.File;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.hex.ai.AI;
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
import com.sam.hex.replay.Load;
import com.sam.hex.replay.Save;
import com.sam.hex.view.BoardView;

public class GameActivity extends BaseGameActivity {
    private static final String GAME = "game";

    private Game game;
    private boolean replay;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Must be set up immediately
        initializeNewGame();

        if(savedInstanceState != null) {
            // Resume a game if one exists
            game = (Game) savedInstanceState.getSerializable(GAME);
            game.setGameListener(createGameListener());
            replay = true;
        }
        else {
            // Check to see if we should load a game
            Intent intent = getIntent();
            if(intent.getData() != null) {
                Load load = new Load(new File(intent.getData().getPath()));
                game = load.run();
                game.setGameListener(createGameListener());
                replay = true;
            }
        }

        // Load the UI
        applyBoard();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(GAME, game);
    }

    private void applyBoard() {
        setContentView(R.layout.game);

        board = (BoardView) findViewById(R.id.board);
        board.setGame(game);
        player1Icon = (ImageButton) findViewById(R.id.p1);
        player2Icon = (ImageButton) findViewById(R.id.p2);
        timerText = (TextView) findViewById(R.id.timer);
        if(game.gameOptions.timer.type == 0 || game.isGameOver()) {
            timerText.setVisibility(View.GONE);
        }
        winnerText = (TextView) findViewById(R.id.winner);
        if(game.isGameOver() && game.getGameListener() != null) game.getGameListener().onWin(game.getCurrentPlayer());

        replayForward = (ImageButton) findViewById(R.id.replayForward);
        replayPlayPause = (ImageButton) findViewById(R.id.replayPlayPause);
        replayBack = (ImageButton) findViewById(R.id.replayBack);
        replayButtons = (RelativeLayout) findViewById(R.id.replayButtons);
    }

    private void initializeNewGame() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Stop the old game
        stopGame(game);

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

        applyBoard();
        game.gameOptions.timer.start(game);
        game.start();
    }

    private GameListener createGameListener() {
        return new GameListener() {
            @Override
            public void onWin(final PlayingEntity player) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String winnerMsg = String.format(getString(R.string.winner), player.getName());
                        winnerText.setText(winnerMsg);
                        winnerText.setVisibility(View.VISIBLE);
                        winnerText.invalidate();
                        timerText.setVisibility(View.GONE);
                        timerText.invalidate();
                        board.invalidate();

                        Stats.incrementTimePlayed(getApplicationContext(), game.getGameLength());
                        Stats.incrementGamesPlayed(getApplicationContext());
                        if(player.getTeam() == 1) Stats.incrementGamesWon(getApplicationContext());

                        if(mIsSignedIn) {
                            // Unlock the quick play achievement!
                            if(game.getGameLength() < 30 * 1000) {
                                getGamesClient().unlockAchievement(getString(R.string.achievement_30_seconds));
                            }

                            // Unlock the fill the board achievement!
                            boolean boardFilled = true;
                            for(int i = 0; i < game.gameOptions.gridSize; i++) {
                                for(int j = 0; j < game.gameOptions.gridSize; j++) {
                                    if(game.gamePieces[i][j].getTeam() == 0) boardFilled = false;
                                }
                            }
                            if(boardFilled) {
                                getGamesClient().unlockAchievement(getString(R.string.achievement_fill_the_board));
                            }
                        }
                    }
                });
            }

            @Override
            public void onClear() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.postInvalidate();
                    }
                });
            }

            @Override
            public void onStart() {
                runOnUiThread(new Runnable() {
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
                runOnUiThread(new Runnable() {
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
                runOnUiThread(new Runnable() {
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.postInvalidate();
                        if(game.gameOptions.timer.type != 0) timerText.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onUndo() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.postInvalidate();
                    }
                });
            }

            @Override
            public void startTimer() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timerText.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void displayTime(final int minutes, final int seconds) {
                runOnUiThread(new Runnable() {
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Check if settings were changed and we need to run a new game
        if(game != null && game.replayRunning) {
            // Do nothing
        }
        else if(replay) {
            replay = false;
            replay(800);
        }
        else if(somethingChanged(prefs, GameAction.LOCAL_GAME, game)) {
            initializeNewGame();
            applyBoard();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu_game_local, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch(item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.settings:
            game.replayRunning = false;
            startActivity(new Intent(getBaseContext(), PreferencesActivity.class));
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
        case R.id.saveReplay:
            Save save = new Save(game);
            save.showSavingDialog(this);
            return true;
        case R.id.quit:
            quit();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
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
            game.getPlayer1().setName(prefs.getString("player1Name", "Player1"));
            game.getPlayer2().setName(prefs.getString("player2Name", "Player2"));
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
                        game.replayRunning = false;
                        initializeNewGame();
                        applyBoard();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    // Do nothing
                    break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                    || !sameType(game.getPlayer1(), Integer.valueOf(prefs.getString("player1Type", getString(R.integer.DEFAULT_P1_ENTITY))))
                    || !sameType(game.getPlayer2(), Integer.valueOf(prefs.getString("player2Type", getString(R.integer.DEFAULT_P2_ENTITY))))
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

    private boolean sameType(PlayingEntity player, int type) {
        if(player.getType().equals(Player.Human)) {
            return type == 0;
        }
        else if(player.getType().equals(Player.AI)) {
            return ((AI) player).getAIType() == type;
        }
        return false;
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
                    finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    // Do nothing
                    break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirmExit)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    boolean mIsSignedIn = false;

    @Override
    public void onSignInSucceeded() {
        System.out.println("Signed in");
        mIsSignedIn = true;
    }

    @Override
    public void onSignInFailed() {
        System.out.println("Sign in failed");
    }
}
