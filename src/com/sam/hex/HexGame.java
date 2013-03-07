package com.sam.hex;

import java.io.File;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.sam.hex.activity.GameActivity;
import com.sam.hex.ai.bee.BeeGameAI;
import com.sam.hex.ai.will.GameAI;
import com.sam.hex.net.NetGlobal;
import com.sam.hex.net.NetPlayerObject;
import com.sam.hex.replay.FileExplore;
import com.sam.hex.replay.Load;
import com.sam.hex.replay.Replay;
import com.sam.hex.replay.Save;

public class HexGame extends GameActivity {
    public static boolean startNewGame = true;
    public static boolean replay = false;
    private static Intent intent;
    private Thread replayThread;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(HexGame.startNewGame) {
            initializeNewGame();// Must be set up immediately
        }

        if(intent != getIntent()) {
            intent = getIntent();
            if(intent.getData() != null) {
                Thread loading = new Thread(new ThreadGroup("Load"), new Load(new File(intent.getData().getPath())), "loading", 200000);
                loading.start();
                try {
                    loading.join();
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        applyBoard();
    }

    private void applyBoard() {
        Global.viewLocation = Global.GAME_LOCATION;
        setContentView(R.layout.game);
        game.views.board = (BoardView) findViewById(R.id.board);

        game.views.player1Icon = (ImageButton) findViewById(R.id.p1);
        game.views.player2Icon = (ImageButton) findViewById(R.id.p2);

        game.views.timerText = (TextView) findViewById(R.id.timer);
        if(game.timer.type == 0 || game.gameOver) {
            game.views.timerText.setVisibility(View.GONE);
        }
        game.views.winnerText = (TextView) findViewById(R.id.winner);
        if(game.gameOver) game.views.winnerText.setText(game.winnerMsg);
        game.views.handler = new Handler();

        game.views.replayForward = (ImageButton) findViewById(R.id.replayForward);
        game.views.replayPlayPause = (ImageButton) findViewById(R.id.replayPlayPause);
        game.views.replayBack = (ImageButton) findViewById(R.id.replayBack);
        game.views.replayButtons = (RelativeLayout) findViewById(R.id.replayButtons);
    }

    private void initializeNewGame() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        startNewGame = false;

        // Stop the old game
        stopGame(game);

        // Create a new game object
        game = new GameObject(setGrid(prefs, Global.GAME_LOCATION), prefs.getBoolean("swapPref", true));

        // Set players
        setType(prefs, Global.GAME_LOCATION, game);
        setPlayer1(game, new Runnable() {
            public void run() {
                initializeNewGame();
            }
        });
        setPlayer2(game, new Runnable() {
            public void run() {
                initializeNewGame();
            }
        });
        setNames(prefs, Global.GAME_LOCATION, game);
        setColors(prefs, Global.GAME_LOCATION, game);
        int timerType = Integer.parseInt(prefs.getString("timerTypePref", "0"));
        game.timer = new Timer(game, Integer.parseInt(prefs.getString("timerPref", "0")), 0, timerType);

        applyBoard();
        game.timer.start();
        game.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Check if settings were changed and we need to run a new game
        if(game.views.board != null && game.views.board.replayRunning) {
            // Do nothing
        }
        else if(replay) {
            replay = false;
            replay(800);
        }
        else if(HexGame.startNewGame || somethingChanged(prefs, Global.GAME_LOCATION, game)) {
            initializeNewGame();
            applyBoard();
        }
        else {// Apply minor changes without stopping the current game
            setColors(prefs, Global.GAME_LOCATION, game);
            setNames(prefs, Global.GAME_LOCATION, game);
            game.moveList.replay(0, game);
            GameAction.checkedFlagReset(game);
            GameAction.checkWinPlayer(1, game);
            GameAction.checkWinPlayer(2, game);
            GameAction.checkedFlagReset(game);

            // Apply everything
            game.views.board.invalidate();
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
        case R.id.settings:
            game.views.board.replayRunning = false;
            startActivity(new Intent(getBaseContext(), Preferences.class));
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
        case R.id.loadReplay:
            startActivity(new Intent(getBaseContext(), FileExplore.class));
            return true;
        case R.id.saveReplay:
            Save save = new Save(game);
            save.showSavingDialog();
            return true;
        case R.id.quit:
            quit();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // If the board's empty, just trigger "startNewGame"
        if(game == null || (game.moveNumber == 1 && game.timer.type == 0)) HexGame.startNewGame = true;
    }

    /**
     * Terminates the game
     * */
    public static void stopGame(GameObject game) {
        if(game != null) {
            game.stop();
        }
    }

    /**
     * Refreshes both player's names Does not invalidate the board
     * */
    public static void setNames(SharedPreferences prefs, int gameLocation, GameObject game) {
        if(gameLocation == Global.GAME_LOCATION) {
            // Playing on the same phone
            game.player1.setName(prefs.getString("player1Name", "Player1"));
            game.player2.setName(prefs.getString("player2Name", "Player2"));
        }
        else if(gameLocation == NetGlobal.GAME_LOCATION) {
            // Playing over the net
            for(int i = 0; i < NetGlobal.members.size(); i++) {
                if(NetGlobal.members.get(i).place == 1) {
                    game.player1.setName(NetGlobal.members.get(i).name);
                }
                else if(NetGlobal.members.get(i).place == 2) {
                    game.player2.setName(NetGlobal.members.get(i).name);
                }
            }
        }
    }

    /**
     * Refreshes both player's colors Does not invalidate the board
     * */
    public static void setColors(SharedPreferences prefs, int gameLocation, GameObject game) {
        if(gameLocation == Global.GAME_LOCATION) {
            // Playing on the same phone
            game.player1.setColor(prefs.getInt("player1Color", Global.player1DefaultColor));
            game.player2.setColor(prefs.getInt("player2Color", Global.player2DefaultColor));
        }
        else if(gameLocation == NetGlobal.GAME_LOCATION) {
            // Playing on the net
            game.player1.setColor(Global.player1DefaultColor);
            game.player2.setColor(Global.player2DefaultColor);
        }
    }

    public static int setGrid(SharedPreferences prefs, int gameLocation) {
        int gridSize = 0;
        if(gameLocation == Global.GAME_LOCATION) {
            // Playing on the same phone
            gridSize = Integer.decode(prefs.getString("gameSizePref", "7"));
            if(gridSize == 0) gridSize = Integer.decode(prefs.getString("customGameSizePref", "7"));
        }
        else if(gameLocation == NetGlobal.GAME_LOCATION) {
            // Playing over the net
            gridSize = NetGlobal.gridSize;
        }

        // We don't want 0x0 games
        if(gridSize <= 0) gridSize = 1;

        return gridSize;
    }

    public static void setType(SharedPreferences prefs, int gameLocation, GameObject game) {
        if(gameLocation == Global.GAME_LOCATION) {
            game.player1Type = (byte) Integer.parseInt(prefs.getString("player1Type", "1"));
            game.player2Type = (byte) Integer.parseInt(prefs.getString("player2Type", "0"));
        }
        else if(gameLocation == NetGlobal.GAME_LOCATION) {
            // Playing over the net
            for(int i = 0; i < NetGlobal.members.size(); i++) {
                if(NetGlobal.members.get(i).place == 1) {
                    if(prefs.getString("netUsername", "").toLowerCase(Locale.US).equals(NetGlobal.members.get(i).name.toLowerCase())) {
                        game.player1Type = (byte) 0;
                    }
                    else {
                        game.player1Type = (byte) 3;
                    }
                }
                else if(NetGlobal.members.get(i).place == 2) {
                    if(prefs.getString("netUsername", "").toLowerCase(Locale.US).equals(NetGlobal.members.get(i).name.toLowerCase())) {
                        game.player2Type = (byte) 0;
                    }
                    else {
                        game.player2Type = (byte) 3;
                    }
                }
            }
        }
    }

    public static void setPlayer1(GameObject game, Runnable newgame) {
        if(game.player1Type == 0) game.player1 = new PlayerObject(1, game);
        else if(game.player1Type == 1) game.player1 = new GameAI(1, game);
        else if(game.player1Type == 3) game.player1 = new NetPlayerObject(1, game, new Handler(), newgame);
        else if(game.player1Type == 4) game.player1 = new BeeGameAI(1, game);
    }

    public static void setPlayer2(GameObject game, Runnable newgame) {
        if(game.player2Type == 0) game.player2 = new PlayerObject(2, game);
        else if(game.player2Type == 1) game.player2 = new GameAI(2, game);
        else if(game.player2Type == 3) game.player2 = new NetPlayerObject(2, game, new Handler(), newgame);
        else if(game.player2Type == 4) game.player2 = new BeeGameAI(2, game);
    }

    private void undo() {
        GameAction.undo(Global.GAME_LOCATION, game);
    }

    private void newGame() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    if(game.player1.supportsNewgame() && game.player2.supportsNewgame()) {
                        game.views.board.replayRunning = false;
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

        AlertDialog.Builder builder = new AlertDialog.Builder(HexGame.this);
        builder.setMessage(getString(R.string.confirmNewgame)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    /**
     * Returns true if a major setting was changed
     * */
    public static boolean somethingChanged(SharedPreferences prefs, int gameLocation, GameObject game) {
        if(game == null) return true;
        if(game.gridSize == 1) return true;
        if(gameLocation == Global.GAME_LOCATION) {
            return (Integer.decode(prefs.getString("gameSizePref", "7")) != game.gridSize && Integer.decode(prefs.getString("gameSizePref", "7")) != 0)
                    || (Integer.decode(prefs.getString("customGameSizePref", "7")) != game.gridSize && Integer.decode(prefs.getString("gameSizePref", "7")) == 0)
                    || Integer.decode(prefs.getString("player1Type", "1")) != (int) game.player1Type
                    || Integer.decode(prefs.getString("player2Type", "0")) != (int) game.player2Type
                    || Integer.decode(prefs.getString("timerTypePref", "0")) != game.timer.type
                    || Integer.decode(prefs.getString("timerPref", "0")) * 60 * 1000 != game.timer.totalTime;
        }
        else if(gameLocation == NetGlobal.GAME_LOCATION) {
            return(game != null && game.gameOver);
        }
        else {
            return true;
        }
    }

    private void replay(int time) {
        applyBoard();
        game.clearBoard();

        replayThread = new Thread(new Replay(time, new Handler(), new Runnable() {
            public void run() {
                game.views.timerText.setVisibility(View.GONE);
                game.views.winnerText.setVisibility(View.GONE);
                // Global.replayButtons.setVisibility(View.VISIBLE);
            }
        }, new Runnable() {
            public void run() {
                if(game.timer.type != 0) game.views.timerText.setVisibility(View.VISIBLE);
                // Global.replayButtons.setVisibility(View.GONE);
            }
        }, game), "replay");
        replayThread.start();
    }

    private void quit() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    stopGame(game);
                    startNewGame = true;
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
}
