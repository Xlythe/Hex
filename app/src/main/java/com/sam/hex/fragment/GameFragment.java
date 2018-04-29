package com.sam.hex.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.JsonSyntaxException;
import com.hex.ai.AiTypes;
import com.hex.ai.GameAI;
import com.hex.core.Game.GameListener;
import com.hex.core.GameAction;
import com.hex.core.Player;
import com.hex.core.PlayerObject;
import com.hex.core.PlayingEntity;
import com.hex.core.Timer;
import com.sam.hex.FileUtil;
import com.sam.hex.MainActivity.Stat;
import com.sam.hex.R;
import com.sam.hex.Settings;
import com.sam.hex.Stats;
import com.sam.hex.compat.Game;
import com.sam.hex.compat.GameOptions;
import com.sam.hex.view.BoardView;
import com.sam.hex.view.GameOverDialog;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.sam.hex.Settings.TAG;

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
    public static final String PRELOADED_GAME = "preloaded_game";
    private static final SimpleDateFormat SAVE_FORMAT = new SimpleDateFormat("yyyy-mm-dd hh:mm", Locale.getDefault());

    private Game game;
    private Player player1Type;
    private Player player2Type;
    private boolean replay;
    private int replayDuration;
    private long timeGamePaused;
    private long whenGamePaused;

    private boolean goHome = false;

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
        keepScreenOn(true);

        loadGame(savedInstanceState);
        return applyBoard(inflater, container);
    }

    @Override
    public void onPause() {
        super.onPause();
        whenGamePaused = System.currentTimeMillis();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopGame();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (game != null && game.getPlayer1().supportsSave() && game.getPlayer2().supportsSave()) {
            savedInstanceState.putString(GAME, game.save());
            savedInstanceState.putSerializable(PLAYER1, game.getPlayer1().getSaveState());
            savedInstanceState.putSerializable(PLAYER2, game.getPlayer2().getSaveState());
            savedInstanceState.putSerializable(PLAYER1_TYPE, player1Type);
            savedInstanceState.putSerializable(PLAYER2_TYPE, player2Type);
        }
    }

    private void loadGame(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(GAME)) {
            String gameState = savedInstanceState.getString(GAME);

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
                game = Game.load(gameState, createPlayer(1, gridSize), createPlayer(2, gridSize));
                game.getPlayer1().setSaveState(savedInstanceState.getSerializable(PLAYER1));
                game.getPlayer2().setSaveState(savedInstanceState.getSerializable(PLAYER2));
            } else {
                // Load a game with 2 humans
                game = Game.load(gameState);
            }
            game.setGameListener(createGameListener());
            replay = true;
            replayDuration = 0;

            if (savedInstanceState.containsKey(REPLAY) && savedInstanceState.getBoolean(REPLAY)) {
                replayDuration = 900;
            }
        } else if (getArguments() != null && getArguments().containsKey(GAME)) {
            String gameState = getArguments().getString(GAME);

            // Load a game
            player1Type = Player.Human;
            player2Type = Player.Human;
            try {
                game = Game.load(gameState);
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
        } else if (getArguments() != null && getArguments().containsKey(PRELOADED_GAME) && getArguments().getBoolean(PRELOADED_GAME)) {
            // Net game (game should have already been passed in)
            if (game == null) {
                returnHome();
                initializeNewGame();
            } else {
                game.setGameListener(createGameListener());
            }
            replay = true;
            replayDuration = 0;
        } else {
            // Create a new game
            initializeNewGame();
        }
    }

    private void startGame() {
        if (game.hasTimer()) {
            game.startTimer();
        }
        game.start();
    }

    private View applyBoard(@NonNull LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        board = view.findViewById(R.id.board);
        board.setGame(game);
        board.setTitleText(getString(R.string.game_turn_title));
        board.setActionText(getString(R.string.game_turn_msg));
        if (game.hasTimer())
            board.setTimerText(getString(R.string.game_timer_msg));
        if (game.isGameOver() && game.getGameListener() != null)
            game.getGameListener().onWin(game.getCurrentPlayer());

        exit = view.findViewById(R.id.exit);
        exit.setOnClickListener(v -> quit());
        newGame = view.findViewById(R.id.reload);
        newGame.setOnClickListener(v -> newGame());
        undo = view.findViewById(R.id.undo);
        undo.setOnClickListener(v -> undo());

        undo.setNextFocusRightId(R.id.board);
        board.setNextFocusLeftId(R.id.undo);

        newGame.setVisibility(supportsNewGame() ? View.VISIBLE : View.GONE);
        undo.setVisibility(supportsUndo() ? View.VISIBLE : View.GONE);

        return view;
    }

    protected void initializeNewGame() {
        // Stop the old game
        stopGame();
        timeGamePaused = 0;
        gameHasEnded = false;

        // Create a new game object
        GameOptions gameOptions = new GameOptions.Builder()
                .setGridSize(Settings.getGridSize(getMainActivity()))
                .setSwapEnabled(Settings.getSwap(getMainActivity()))
                .setTimer(new Timer(Settings.getTimeAmount(getMainActivity()), 0, Settings.getTimerType(getMainActivity())))
                .build();

        GameListener gameListener = createGameListener();

        game = new Game(gameOptions, createPlayer(1, gameOptions.gridSize), createPlayer(2, gameOptions.gridSize));
        game.setGameListener(gameListener);

        setName(game.getPlayer1());
        setName(game.getPlayer2());
        setColor(game.getPlayer1());
        setColor(game.getPlayer2());
    }

    @NonNull
    private GameListener createGameListener() {
        return new GameListener() {
            @Override
            public void onWin(@NonNull final PlayingEntity player) {
                runOnUiThread(() -> {
                    board.invalidate();

                    Log.v(TAG, player.getName() + " won!");

                    new GameOverDialog.Builder(getContext())
                            .setGameFragment(GameFragment.this)
                            .setWinner(player)
                            .show();

                    if (gameHasEnded) return;
                    else gameHasEnded = true;

                    new Thread(() -> {
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

                        if (isSignedIn()) {
                            // Net is async and can disconnect at any time
                            try {
                                // Backup stats
                                Stat stat = new Stat();
                                stat.setTimePlayed(Stats.getTimePlayed(getMainActivity()));
                                stat.setGamesWon(Stats.getGamesWon(getMainActivity()));
                                stat.setGamesPlayed(Stats.getGamesPlayed(getMainActivity()));
                                stat.setDonationRank(Stats.getDonationRank(getMainActivity()));

                                // Unlock the quick play achievements!
                                if (game.getGameLength() < 30 * 1000) {
                                    getAchievementsClient().unlock(getString(R.string.achievement_30_seconds));
                                }
                                if (game.getGameLength() < 10 * 1000) {
                                    getAchievementsClient().unlock(getString(R.string.achievement_10_seconds));
                                }

                                // Unlock the fill the board achievement!
                                boolean boardFilled = true;
                                for (int i = 0; i < game.getGridSize(); i++) {
                                    for (int j = 0; j < game.getGridSize(); j++) {
                                        if (game.gamePieces[i][j].getTeam() == 0)
                                            boardFilled = false;
                                    }
                                }
                                if (boardFilled) {
                                    getAchievementsClient().unlock(getString(R.string.achievement_fill_the_board));
                                }

                                // Unlock the monitor smasher achievement!
                                if (isVsAi() && player.getType().equals(Player.Human)) {
                                    getAchievementsClient().unlock(getString(R.string.achievement_monitor_smasher));
                                }

                                // Unlock the speed demon achievement!
                                if (game.hasTimer()) {
                                    getAchievementsClient().unlock(getString(R.string.achievement_speed_demon));
                                }

                                // Unlock the Novice achievement!
                                getAchievementsClient().increment(getString(R.string.achievement_novice), 1);

                                // Unlock the Intermediate achievement!
                                getAchievementsClient().increment(getString(R.string.achievement_intermediate), 1);

                                // Unlock the Expert achievement!
                                if (player.getType().equals(Player.Human)) {
                                    getAchievementsClient().increment(getString(R.string.achievement_expert), 1);
                                }

                                // Unlock the Insane achievement!
                                if (player.getType().equals(Player.Human)) {
                                    getAchievementsClient().increment(getString(R.string.achievement_insane), 1);
                                }

                                // Unlock the Net achievement
                                if (isNetGame()) {
                                    getAchievementsClient().unlock(getString(R.string.achievement_net));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                });
            }

            @Override
            public void onClear() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void onStart() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void onStop() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void onTurn(PlayingEntity player) {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void onReplayStart() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void onReplayEnd() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void onUndo() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void startTimer() {
                runOnUiThread(() -> board.postInvalidate());
            }

            @Override
            public void displayTime(int minutes, int seconds) {
                runOnUiThread(() -> board.postInvalidate());
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
            returnHome();
            return;
        }

        // Note: Calling replay will start the game for us.
        if (replay) {
            replay = false;
            replay(replayDuration);
            return;
        }

        if (!game.hasStarted()) {
            startGame();
            return;
        }
    }

    /**
     * Terminates the game
     */
    private void stopGame() {
        if (game != null) {
            game.stop();
        }
    }

    /**
     * Refreshes both player's names Does not invalidate the board
     */
    protected void setName(@NonNull PlayingEntity player) {
        if (isPassToPlay()) {
            if (player.getTeam() == 1) {
                player.setName(Settings.getPlayer1Name(getMainActivity(), getGoogleSignInAccount()));
            } else {
                player.setName(Settings.getPlayer2Name(getMainActivity()));
            }
        } else if (player.getType() == Player.Human) {
            player.setName(Settings.getPlayer1Name(getMainActivity(), getGoogleSignInAccount()));
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

    private PlayingEntity createPlayer(int team, int gridSize) {
        Player p = (team == 1) ? player1Type : player2Type;
        switch (p) {
            case AI:
                int difficulty = Settings.getComputerDifficulty(getMainActivity());
                if (difficulty == 0) return new GameAI(team);
                return AiTypes.newAI(AiTypes.BeeAI, team, gridSize, difficulty + 1);
            case Human:
                return new PlayerObject(team);
            default:
                return new PlayerObject(team);
        }
    }

    protected void undo() {
        GameAction.undo(GameAction.LOCAL_GAME, game);
    }

    protected void newGame() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    startNewGame();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // Do nothing
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getMainActivity());
        builder.setMessage(getString(R.string.confirmNewgame)).setPositiveButton(getString(R.string.yes), dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    public void startNewGame() {
        stopGame();

        // Net games are handled differently, because we need to inform the remote device.
        if (isNetGame()) {
            getNetPlayer().newgameCalled();
            return;
        }

        // Local games can just recreate both players (to get them into a clean state) before
        // starting a new game.
        PlayingEntity p1 = createPlayer(1, game.getGridSize());
        p1.setName(game.getPlayer1().getName());
        p1.setColor(game.getPlayer1().getColor());
        PlayingEntity p2 = createPlayer(2, game.getGridSize());
        p2.setName(game.getPlayer2().getName());
        p2.setColor(game.getPlayer2().getColor());

        switchToGame(new Game(game.gameOptions, p1, p2));
    }

    private void replay(int time) {
        game.replay(time);
    }

    private void quit() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    // Yes button clicked
                    stopGame();
                    returnHome();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    // Do nothing
                    break;
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

    @Nullable
    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;

        if (newGame != null) {
            newGame.setVisibility(supportsNewGame() ? View.VISIBLE : View.GONE);
        }
        if (undo != null) {
            undo.setVisibility(supportsUndo() ? View.VISIBLE : View.GONE);
        }
    }

    private boolean supportsNewGame() {
        if (game == null) {
            return true;
        }
        return game.getPlayer1().supportsNewgame() && game.getPlayer2().supportsNewgame();
    }

    private boolean supportsUndo() {
        if (game == null) {
            return true;
        }
        return game.getPlayer1().supportsUndo(game) && game.getPlayer2().supportsUndo(game);
    }

    private boolean isVsAi() {
        return player1Type.equals(Player.AI) || player2Type.equals(Player.AI);
    }

    private boolean isPassToPlay() {
        return player1Type.equals(Player.Human) && player2Type.equals(Player.Human);
    }

    private boolean isNetGame() {
        return player1Type.equals(Player.Net) || player2Type.equals(Player.Net);
    }

    private PlayingEntity getNetPlayer() {
        if (player1Type.equals(Player.Net)) {
            return game.getPlayer1();
        }

        if (player2Type.equals(Player.Net)) {
            return game.getPlayer2();
        }

        throw new IllegalStateException("Cannot get a net player in a non-net game");
    }
}
