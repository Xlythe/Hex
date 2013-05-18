package com.sam.hex;

import java.io.File;

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
import com.sam.hex.Game.GameListener;
import com.sam.hex.Game.GameOptions;
import com.sam.hex.activity.DefaultActivity;
import com.sam.hex.ai.bee.BeeGameAI;
import com.sam.hex.ai.will.GameAI;
import com.sam.hex.replay.FileExplore;
import com.sam.hex.replay.Load;
import com.sam.hex.replay.Replay;
import com.sam.hex.replay.Save;

public class HexGame extends DefaultActivity {
    private static final String GAME = "game";

    private Game game;
    private Thread replayThread;
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
            GameListener gl = game.gameListener;
            game = (Game) savedInstanceState.getSerializable(GAME);
            game.gameListener = gl;
        }
        else {
            // Check to see if we should load a game
            Intent intent = getIntent();
            if(intent.getData() != null) {
                Load load = new Load(new File(intent.getData().getPath()));
                game = load.run(game.gameListener);
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
        if(game.gameOptions.timer.type == 0 || game.gameOver) {
            timerText.setVisibility(View.GONE);
        }
        winnerText = (TextView) findViewById(R.id.winner);
        if(game.gameOver) game.gameListener.onWin(GameAction.getPlayer(game.currentPlayer, game));

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
        go.timer = new Timer(null, Integer.parseInt(prefs.getString("timerPref", "0")), 0, timerType);

        GameListener gl = new GameListener() {
            @Override
            public void onWin(final PlayingEntity player) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String winnerMsg = GameAction.insert(getString(R.string.winner), player.getName());
                        winnerText.setText(winnerMsg);
                        winnerText.setVisibility(View.VISIBLE);
                        winnerText.invalidate();
                        timerText.setVisibility(View.GONE);
                        timerText.invalidate();
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
                        if(game.gameOver) winnerText.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onStop() {

            }

            @Override
            public void onTurn(PlayingEntity player) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.postInvalidate();
                        player1Icon.setColorFilter(game.player1.getColor());
                        player2Icon.setColorFilter(game.player2.getColor());
                        if(game.currentPlayer == 1 && !game.gameOver) {
                            player1Icon.setAlpha(255);
                            player2Icon.setAlpha(80);
                        }
                        else if(game.currentPlayer == 2 && !game.gameOver) {
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
            public void onReplay() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.postInvalidate();
                    }
                });
            }

            @Override
            public void onTeamSet() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        board.postInvalidate();
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
                        timerText.setText(GameAction.insert(getString(R.string.timer), String.format("%d:%02d", minutes, seconds)));
                        timerText.invalidate();
                    }
                });
            }
        };

        game = new Game(go, gl);

        // Set players
        setType(prefs, GameAction.LOCAL_GAME, game);
        setPlayer1(game, new Runnable() {
            @Override
            public void run() {
                initializeNewGame();
            }
        });
        setPlayer2(game, new Runnable() {
            @Override
            public void run() {
                initializeNewGame();
            }
        });
        setNames(prefs, GameAction.LOCAL_GAME, game);
        setColors(prefs, GameAction.LOCAL_GAME, game);

        applyBoard();
        game.gameOptions.timer.start();
        game.start();
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
            game.moveList.replay(0, game);
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
        case R.id.settings:
            game.replayRunning = false;
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
    public static void stopGame(Game game) {
        if(game != null) {
            game.stop();
        }
    }

    /**
     * Refreshes both player's names Does not invalidate the board
     * */
    public static void setNames(SharedPreferences prefs, int gameLocation, Game game) {
        if(gameLocation == GameAction.LOCAL_GAME) {
            // Playing on the same phone
            game.player1.setName(prefs.getString("player1Name", "Player1"));
            game.player2.setName(prefs.getString("player2Name", "Player2"));
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
    public static void setColors(SharedPreferences prefs, int gameLocation, Game game) {
        if(gameLocation == GameAction.LOCAL_GAME) {
            // Playing on the same phone
            game.player1.setColor(prefs.getInt("player1Color", 0));
            game.player2.setColor(prefs.getInt("player2Color", 0));
        }
        else if(gameLocation == GameAction.NET_GAME) {
            // Playing on the net
            game.player1.setColor(0);
            game.player2.setColor(0);
        }
    }

    public static int setGrid(SharedPreferences prefs, int gameLocation) {
        int gridSize = 0;
        if(gameLocation == GameAction.LOCAL_GAME) {
            // Playing on the same phone
            gridSize = Integer.decode(prefs.getString("gameSizePref", "7"));
            if(gridSize == 0) gridSize = Integer.decode(prefs.getString("customGameSizePref", "7"));
        }
        else if(gameLocation == GameAction.NET_GAME) {
            // Playing over the net
            gridSize = 7;
        }

        // We don't want 0x0 games
        if(gridSize <= 0) gridSize = 1;

        return gridSize;
    }

    public static void setType(SharedPreferences prefs, int gameLocation, Game game) {
        if(gameLocation == GameAction.LOCAL_GAME) {
            game.player1Type = (byte) Integer.parseInt(prefs.getString("player1Type", "1"));
            game.player2Type = (byte) Integer.parseInt(prefs.getString("player2Type", "0"));
        }
        else if(gameLocation == GameAction.NET_GAME) {
            // // Playing over the net
            // for(int i = 0; i < NetGlobal.members.size(); i++) {
            // if(NetGlobal.members.get(i).place == 1) {
            // if(prefs.getString("netUsername",
            // "").toLowerCase(Locale.US).equals(NetGlobal.members.get(i).name.toLowerCase()))
            // {
            // game.player1Type = (byte) 0;
            // }
            // else {
            // game.player1Type = (byte) 3;
            // }
            // }
            // else if(NetGlobal.members.get(i).place == 2) {
            // if(prefs.getString("netUsername",
            // "").toLowerCase(Locale.US).equals(NetGlobal.members.get(i).name.toLowerCase()))
            // {
            // game.player2Type = (byte) 0;
            // }
            // else {
            // game.player2Type = (byte) 3;
            // }
            // }
            // }
        }
    }

    public static void setPlayer1(Game game, Runnable newgame) {
        if(game.player1Type == 0) game.player1 = new PlayerObject(1, game);
        else if(game.player1Type == 1) game.player1 = new GameAI(1, game);
        // else if(game.player1Type == 3) game.player1 = new NetPlayerObject(1,
        // game, new Handler(), newgame);
        else if(game.player1Type == 4) game.player1 = new BeeGameAI(1, game);
    }

    public static void setPlayer2(Game game, Runnable newgame) {
        if(game.player2Type == 0) game.player2 = new PlayerObject(2, game);
        else if(game.player2Type == 1) game.player2 = new GameAI(2, game);
        // else if(game.player2Type == 3) game.player2 = new NetPlayerObject(2,
        // game, new Handler(), newgame);
        else if(game.player2Type == 4) game.player2 = new BeeGameAI(2, game);
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
                    if(game.player1.supportsNewgame() && game.player2.supportsNewgame()) {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(HexGame.this);
        builder.setMessage(getString(R.string.confirmNewgame)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    /**
     * Returns true if a major setting was changed
     * */
    public static boolean somethingChanged(SharedPreferences prefs, int gameLocation, Game game) {
        if(game == null) return true;
        if(game.gameOptions.gridSize == 1) return true;
        if(gameLocation == GameAction.LOCAL_GAME) {
            return (Integer.decode(prefs.getString("gameSizePref", "7")) != game.gameOptions.gridSize && Integer.decode(prefs.getString("gameSizePref", "7")) != 0)
                    || (Integer.decode(prefs.getString("customGameSizePref", "7")) != game.gameOptions.gridSize && Integer.decode(prefs.getString(
                            "gameSizePref", "7")) == 0)
                    || Integer.decode(prefs.getString("player1Type", "1")) != game.player1Type
                    || Integer.decode(prefs.getString("player2Type", "0")) != game.player2Type
                    || Integer.decode(prefs.getString("timerTypePref", "0")) != game.gameOptions.timer.type
                    || Integer.decode(prefs.getString("timerPref", "0")) * 60 * 1000 != game.gameOptions.timer.totalTime;
        }
        else if(gameLocation == GameAction.NET_GAME) {
            return (game != null && game.gameOver);
        }
        else {
            return true;
        }
    }

    private void replay(int time) {
        applyBoard();
        game.clearBoard();

        replayThread = new Thread(new Replay(time, new Handler(), new Runnable() {
            @Override
            public void run() {
                timerText.setVisibility(View.GONE);
                winnerText.setVisibility(View.GONE);
            }
        }, new Runnable() {
            @Override
            public void run() {
                if(game.gameOptions.timer.type != 0) timerText.setVisibility(View.VISIBLE);
            }
        }, game), "replay");
        replayThread.start();
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
}
