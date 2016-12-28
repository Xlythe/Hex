package com.sam.hex.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hex.ai.AiTypes;
import com.hex.ai.GameAI;
import com.hex.core.Game;
import com.hex.core.Game.GameListener;
import com.hex.core.Game.GameOptions;
import com.hex.core.GameAction;
import com.hex.core.Player;
import com.hex.core.PlayerObject;
import com.hex.core.PlayingEntity;
import com.hex.core.Timer;
import com.hex.network.NetworkPlayer;
import com.sam.hex.FileUtil;
import com.sam.hex.MainActivity;
import com.sam.hex.MainActivity.Stat;
import com.sam.hex.R;
import com.sam.hex.Settings;
import com.sam.hex.Stats;
import com.sam.hex.view.BoardView;
import com.sam.hex.view.GameOverDialog;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Will Harmon
 **/
public class GameFragment extends HexFragment {
    public static final String GAME = "game";
    public static final String PLAYER1 = "player1";
    public static final String PLAYER2 = "player2";
    public static final String PLAYER1_TYPE = "player1_type";
    public static final String PLAYER2_TYPE = "player2_type";
    public static final String REPLAY = "replay";
    public static final String NET = "net";
    private static final SimpleDateFormat SAVE_FORMAT = new SimpleDateFormat("MMM dd, yyyy hh:mm", Locale.getDefault());

    @Nullable
    private Game game;
    @Nullable
    private Player player1Type;
    @Nullable
    private Player player2Type;
    private boolean replay;
    private int replayDuration;
    private long timeGamePaused;
    private long whenGamePaused;

    private boolean goHome = false;
    private boolean leaveRoom = true;

    /**
     * Set at the end of onWin, or when a game is loaded. Use this to avoid auto-saving replayed games or unlocking achievements that weren't earned.
     */
    private boolean gameHasEnded = false;

    private BoardView board;
    private Button exit;
    private Button newGame;
    private Button undo;

    /**
     * Called when the activity is first created.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getMainActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (savedInstanceState != null && savedInstanceState.containsKey(GAME)) {
            // Resume a game if one exists
            boolean keys = savedInstanceState.containsKey(PLAYER1_TYPE);
            keys &= savedInstanceState.containsKey(PLAYER2_TYPE);
            keys &= savedInstanceState.containsKey(PLAYER1);
            keys &= savedInstanceState.containsKey(PLAYER2);
            if (keys) {
                // We have additional information about the player's state
                int gridSize = Settings.getGridSize(getMainActivity());
                player1Type = (Player) savedInstanceState.getSerializable(PLAYER1_TYPE);
                player2Type = (Player) savedInstanceState.getSerializable(PLAYER2_TYPE);
                game = Game.load(savedInstanceState.getString(GAME), getPlayer(1, gridSize), getPlayer(2, gridSize));
                game.getPlayer1().setSaveState(savedInstanceState.getSerializable(PLAYER1));
                game.getPlayer2().setSaveState(savedInstanceState.getSerializable(PLAYER2));
            } else {
                // Load a game with 2 humans
                game = Game.load(savedInstanceState.getString(GAME));
            }
            game.setGameListener(createGameListener());
            replay = true;
            replayDuration = 0;

            if (savedInstanceState.containsKey(REPLAY) && savedInstanceState.getBoolean(REPLAY)) {
                replayDuration = 900;
            }
        } else if (getArguments() != null && getArguments().containsKey(GAME)) {
            // Load a game
            player1Type = Player.Human;
            player2Type = Player.Human;
            try {
                game = Game.load(getArguments().getString(GAME));
                game.setGameListener(createGameListener());
                replay = true;
                replayDuration = 0;
                gameHasEnded = true;

                if (getArguments().containsKey(REPLAY) && getArguments().getBoolean(REPLAY)) {
                    replayDuration = 900;
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                // Create a new game
                initializeNewGame();
            }
        } else if (getArguments() != null && getArguments().containsKey(NET) && getArguments().getBoolean(NET)) {
            // Net game (game should have already been passed in)
            if (game == null) {
                ((MainActivity) getActivity()).returnHome();
                initializeNewGame();
            } else {
                game.setGameListener(createGameListener());
            }
        } else {
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
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (game != null) {
            savedInstanceState.putString(GAME, game.save());
            savedInstanceState.putSerializable(PLAYER1, game.getPlayer1().getSaveState());
            savedInstanceState.putSerializable(PLAYER2, game.getPlayer2().getSaveState());
            savedInstanceState.putSerializable(PLAYER1_TYPE, player1Type);
            savedInstanceState.putSerializable(PLAYER2_TYPE, player2Type);
        }
    }

    private View applyBoard(@NonNull LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.fragment_game, null);

        board = (BoardView) v.findViewById(R.id.board);
        board.setGame(game);
        board.setTitleText(getString(R.string.game_turn_title));
        board.setActionText(getString(R.string.game_turn_msg));
        if (game.gameOptions.timer.type != Timer.NO_TIMER)
            board.setTimerText(getString(R.string.game_timer_msg));
        if (game.isGameOver() && game.getGameListener() != null)
            game.getGameListener().onWin(game.getCurrentPlayer());

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

        undo.setNextFocusRightId(R.id.board);
        board.setNextFocusLeftId(R.id.undo);

        return v;
    }

    protected void initializeNewGame() {
        // Stop the old game
        stopGame(game);
        timeGamePaused = 0;
        gameHasEnded = false;

        // Create a new game object
        GameOptions go = new GameOptions();
        go.gridSize = Settings.getGridSize(getMainActivity());
        go.swap = Settings.getSwap(getMainActivity());

        int timerType = Settings.getTimerType(getMainActivity());
        go.timer = new Timer(Settings.getTimeAmount(getMainActivity()), 0, timerType);

        GameListener gl = createGameListener();

        game = new Game(go, getPlayer(1, go.gridSize), getPlayer(2, go.gridSize));
        game.setGameListener(gl);

        setName(game.getPlayer1(), player1Type.equals(Player.Human) && player2Type.equals(Player.Human));
        setName(game.getPlayer2(), player1Type.equals(Player.Human) && player2Type.equals(Player.Human));
        setColor(game.getPlayer1());
        setColor(game.getPlayer2());

        game.gameOptions.timer.start(game);
    }

    @Nullable
    private GameListener createGameListener() {
        return new GameListener() {
            @Override
            public void onWin(@NonNull final PlayingEntity player) {
                if (getMainActivity() != null && !isDetached())
                    getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            board.invalidate();

                            GameOverDialog dialog = new GameOverDialog(getMainActivity(), GameFragment.this, player);
                            dialog.show();

                            if (gameHasEnded) return;
                            else gameHasEnded = true;

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    // Auto save completed game
                                    if (Settings.getAutosave(getMainActivity())) {
                                        try {
                                            String fileName = String.format(getString(R.string.auto_saved_file_name), SAVE_FORMAT.format(new Date()), game.getPlayer1().getName(), game.getPlayer2().getName());
                                            FileUtil.autoSaveGame(fileName, game.save());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    Stats.incrementTimePlayed(getMainActivity(), game.getGameLength() - timeGamePaused);
                                    Stats.incrementGamesPlayed(getMainActivity());
                                    if (player.getType().equals(Player.Human))
                                        Stats.incrementGamesWon(getMainActivity());

                                    if (getMainActivity().isSignedIn()) {
                                        // Net is async and can disconnect at any
                                        // time
                                        try {
                                            // Backup stats
                                            Gson gson = new Gson();
                                            Stat stat = new Stat();
                                            stat.setTimePlayed(Stats.getTimePlayed(getMainActivity()));
                                            stat.setGamesWon(Stats.getGamesWon(getMainActivity()));
                                            stat.setGamesPlayed(Stats.getGamesPlayed(getMainActivity()));
                                            stat.setDonationRank(Stats.getDonationRank(getMainActivity()));
                                            //getMainActivity().getAppStateClient().updateState(MainActivity.STAT_STATE, gson.toJson(stat).getBytes());

                                            // Unlock the quick play achievements!
                                            if (game.getGameLength() < 30 * 1000) {
                                                Games.Achievements.unlock(getGamesClient(), getString(R.string.achievement_30_seconds));
                                            }
                                            if (game.getGameLength() < 10 * 1000) {
                                                Games.Achievements.unlock(getGamesClient(), getString(R.string.achievement_10_seconds));
                                            }

                                            // Unlock the fill the board
                                            // achievement!
                                            boolean boardFilled = true;
                                            for (int i = 0; i < game.gameOptions.gridSize; i++) {
                                                for (int j = 0; j < game.gameOptions.gridSize; j++) {
                                                    if (game.gamePieces[i][j].getTeam() == 0)
                                                        boardFilled = false;
                                                }
                                            }
                                            if (boardFilled) {
                                                Games.Achievements.unlock(getGamesClient(), getString(R.string.achievement_fill_the_board));
                                            }

                                            // Unlock the montior smasher
                                            // achievement!
                                            if (player.getType().equals(Player.Human) && game.getOtherPlayer(player).getType().equals(Player.AI)) {
                                                Games.Achievements.unlock(getGamesClient(), getString(R.string.achievement_monitor_smasher));
                                            }

                                            // Unlock the speed demon achievement!
                                            if (game.gameOptions.timer.type != Timer.NO_TIMER) {
                                                Games.Achievements.unlock(getGamesClient(), getString(R.string.achievement_speed_demon));
                                            }

                                            // Unlock the Novice achievement!
                                            Games.Achievements.increment(getGamesClient(), getString(R.string.achievement_novice), 1);

                                            // Unlock the Intermediate achievement!
                                            Games.Achievements.increment(getGamesClient(), getString(R.string.achievement_intermediate), 1);

                                            // Unlock the Expert achievement!
                                            if (player.getType().equals(Player.Human)) {
                                                Games.Achievements.increment(getGamesClient(), getString(R.string.achievement_expert), 1);
                                            }

                                            // Unlock the Insane achievement!
                                            if (player.getType().equals(Player.Human)) {
                                                Games.Achievements.increment(getGamesClient(), getString(R.string.achievement_insane), 1);
                                            }

                                            // Unlock the Net achievement
                                            if (player.getType().equals(Player.Net) || game.getOtherPlayer(player).getType().equals(Player.Net)) {
                                                Games.Achievements.unlock(getGamesClient(), getString(R.string.achievement_net));
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }).start();
                        }
                    });
            }

            @Override
            public void onClear() {
                if (getMainActivity() != null && !isDetached())
                    getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            board.postInvalidate();
                        }
                    });
            }

            @Override
            public void onStart() {
                if (getMainActivity() != null && !isDetached())
                    getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            board.postInvalidate();
                        }
                    });
            }

            @Override
            public void onStop() {

            }

            @Override
            public void onTurn(final PlayingEntity player) {
                if (getMainActivity() != null && !isDetached())
                    getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (!game.isGameOver()) {
                                    board.postInvalidate();
                                }
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            }

            @Override
            public void onReplayStart() {
                if (getMainActivity() != null && !isDetached())
                    getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            board.postInvalidate();
                        }
                    });
            }

            @Override
            public void onReplayEnd() {
                if (getMainActivity() != null && !isDetached())
                    getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            board.postInvalidate();
                        }
                    });
            }

            @Override
            public void onUndo() {
                if (getMainActivity() != null && !isDetached())
                    getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            board.postInvalidate();
                        }
                    });
            }

            @Override
            public void startTimer() {
                if (getMainActivity() != null && !isDetached())
                    getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
            }

            @Override
            public void displayTime(final int minutes, final int seconds) {
                if (getMainActivity() != null && !isDetached())
                    getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            board.postInvalidate();
                        }
                    });
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (whenGamePaused != 0) {
            timeGamePaused += System.currentTimeMillis() - whenGamePaused;
            whenGamePaused = 0;
        }

        if (goHome) {
            getMainActivity().returnHome();
            return;
        }

        // Check if settings were changed and we need to run a new game
        if (game != null && game.replayRunning) {
            // Do nothing
            return;
        } else if (replay) {
            replay = false;
            replay(replayDuration);
        }
    }

    protected void showSavingDialog() {
        final EditText editText = new EditText(getMainActivity());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setText(SAVE_FORMAT.format(new Date()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getMainActivity());
        builder.setTitle(R.string.enterFilename).setView(editText).setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    FileUtil.saveGame(editText.getText().toString(), game.save());
                    Toast.makeText(getMainActivity(), R.string.game_toast_saved, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getMainActivity(), R.string.game_toast_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton(R.string.cancel, null).show();
    }

    /**
     * Terminates the game
     */
    public void stopGame(@Nullable Game game) {
        if (game != null) {
            game.stop();
        }
    }

    /**
     * Refreshes both player's names Does not invalidate the board
     */
    protected void setName(@NonNull PlayingEntity player, boolean guest) {
        if (guest) {
            if (player.getTeam() == 1) {
                player.setName(Settings.getPlayer1Name(getMainActivity(), getGamesClient()));
            } else {
                player.setName(Settings.getPlayer2Name(getMainActivity()));
            }
        } else {
            player.setName(Settings.getPlayer1Name(getMainActivity(), getGamesClient()));
        }
    }

    /**
     * Refreshes both player's colors Does not invalidate the board
     */
    protected void setColor(@NonNull PlayingEntity player) {
        if (player.getTeam() == 1) {
            player.setColor(Settings.getPlayer1Color(getMainActivity()));
        } else {
            player.setColor(Settings.getPlayer2Color(getMainActivity()));
        }
    }

    @Nullable
    public PlayingEntity getPlayer(int team, int gridSize) {
        Player p = (team == 1) ? player1Type : player2Type;
        switch (p) {
            case AI:
                int difficulty = Settings.getComputerDifficulty(getMainActivity());
                if (difficulty == 0) return new GameAI(team);
                return AiTypes.newAI(AiTypes.BeeAI, team, gridSize, difficulty + 1);
            case Human:
                return new PlayerObject(team);
            case Net:
                return new NetworkPlayer(team, null);
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
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked
                        startNewGame();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked
                        // Do nothing
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getMainActivity());
        builder.setMessage(getString(R.string.confirmNewgame)).setPositiveButton(getString(R.string.yes), dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    public void startNewGame() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (game.getPlayer1().supportsNewgame() && game.getPlayer2().supportsNewgame()) {
                    if (getMainActivity() != null && !isDetached()) {
                        getMainActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopGame(game);

                                PlayingEntity p1 = getPlayer(1, game.gameOptions.gridSize);
                                p1.setName(game.getPlayer1().getName());
                                p1.setColor(game.getPlayer1().getColor());
                                PlayingEntity p2 = getPlayer(2, game.gameOptions.gridSize);
                                p2.setName(game.getPlayer2().getName());
                                p2.setColor(game.getPlayer2().getColor());

                                getMainActivity().switchToGame(new Game(game.gameOptions, p1, p2), false);
                            }
                        });
                    }
                }
            }
        }).start();
    }

    /**
     * Returns true if a major setting was changed
     */
    public boolean somethingChanged(@NonNull SharedPreferences prefs, int gameLocation, @Nullable Game game) {
        if (game == null) return true;
        if (game.gameOptions.gridSize == 1) return true;
        if (gameLocation == GameAction.LOCAL_GAME) {
            return (Integer.valueOf(prefs.getString("gameSizePref", Integer.toString(getResources().getInteger(R.integer.DEFAULT_BOARD_SIZE)))) != game.gameOptions.gridSize
                    && Integer.valueOf(prefs.getString("gameSizePref", Integer.toString(getResources().getInteger(R.integer.DEFAULT_BOARD_SIZE)))) != 0)
                    || (Integer.valueOf(prefs.getString("customGameSizePref", Integer.toString(getResources().getInteger(R.integer.DEFAULT_BOARD_SIZE)))) != game.gameOptions.gridSize
                    && Integer.valueOf(prefs.getString("gameSizePref", Integer.toString(getResources().getInteger(R.integer.DEFAULT_BOARD_SIZE)))) == 0)
                    || Integer.valueOf(prefs.getString("timerTypePref", getString(R.integer.DEFAULT_TIMER_TYPE))) != game.gameOptions.timer.type
                    || Integer.valueOf(prefs.getString("timerPref", Integer.toString(getResources().getInteger(R.integer.DEFAULT_TIMER_TIME)))) * 60 * 1000 != game.gameOptions.timer.totalTime;
        } else if (gameLocation == GameAction.NET_GAME) {
            return (game != null && game.isGameOver());
        } else {
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
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked
                        stopGame(game);
                        getMainActivity().returnHome();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked
                        // Do nothing
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getMainActivity());
        builder.setMessage(getString(R.string.confirmExit)).setPositiveButton(getString(R.string.yes), dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    public void setPlayer1Type(Player player1Type) {
        this.player1Type = player1Type;
    }

    public void setPlayer2Type(Player player2Type) {
        this.player2Type = player2Type;
    }

    public void setGoHome(boolean goHome) {
        this.goHome = goHome;
    }

    public void setLeaveRoom(boolean leaveRoom) {
        this.leaveRoom = leaveRoom;
    }

    @Nullable
    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (leaveRoom) {
            getMainActivity().leaveRoom();
        }
    }
}
