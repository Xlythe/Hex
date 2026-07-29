package com.xlythe.hex.compat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.hex.ai.BeeGameAI;
import com.hex.ai.GameAI;
import com.hex.core.AI;
import com.hex.core.Game;
import com.hex.core.GameAction;
import com.hex.core.Move;
import com.hex.core.PlayerObject;
import com.hex.core.PlayingEntity;
import com.hex.core.Point;
import com.hex.core.Timer;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class AndroidBotContractTest {
    private static final int BOARD_SIZE = 5;

    @Test
    public void rosterMatchesTheThreeAdvertisedDifficulties() {
        assertEquals(3, AndroidBotFactory.BOT_COUNT);
        assertTrue(AndroidBotFactory.create(
                AndroidBotFactory.EASY, 1, BOARD_SIZE) instanceof GameAI);
        assertTrue(AndroidBotFactory.create(
                AndroidBotFactory.MEDIUM, 1, BOARD_SIZE) instanceof BeeGameAI);
        assertTrue(AndroidBotFactory.create(
                AndroidBotFactory.HARD, 1, BOARD_SIZE) instanceof BeeGameAI);
        assertThrows(IllegalArgumentException.class,
                () -> AndroidBotFactory.create(99, 1, BOARD_SIZE));
    }

    @Test
    public void beeSearchParametersAreTheAndroidSourceOfTruth() throws Exception {
        assertSearchParameters(AndroidBotFactory.MEDIUM, 2, 5);
        assertSearchParameters(AndroidBotFactory.HARD, 3, 4);
    }

    @Test
    public void everyBotOpensInTheCenter() {
        for (int difficulty = AndroidBotFactory.EASY;
             difficulty <= AndroidBotFactory.HARD;
             difficulty++) {
            Move move = openingMove(difficulty);
            assertEquals("difficulty " + difficulty, 2, move.getX());
            assertEquals("difficulty " + difficulty, 2, move.getY());
        }
    }

    @Test
    public void everyBotReturnsALegalReplyToACornerOpening() {
        for (int difficulty = AndroidBotFactory.EASY;
             difficulty <= AndroidBotFactory.HARD;
             difficulty++) {
            Move move = replyTo(difficulty, new Point(0, 0));
            assertTrue(move.getX() >= 0 && move.getX() < BOARD_SIZE);
            assertTrue(move.getY() >= 0 && move.getY() < BOARD_SIZE);
            assertFalse(move.getX() == 0 && move.getY() == 0);
            assertEquals(2, move.getTeam());
            assertEquals(2, move.getX());
            assertEquals(2, move.getY());
        }
    }

    @Test
    public void beeBotsExposeReferenceGameLines() {
        Point[] opponentLine = {
                new Point(2, 2),
                new Point(0, 0),
                new Point(4, 4),
                new Point(0, 4)
        };
        assertLine(
                playLine(AndroidBotFactory.MEDIUM, opponentLine),
                new int[][] {{0, 3}, {4, 1}, {1, 2}, {2, 3}});
        assertLine(
                playLine(AndroidBotFactory.HARD, opponentLine),
                new int[][] {{0, 3}, {3, 2}, {3, 1}, {2, 4}});
    }

    private static void assertSearchParameters(
            int difficulty,
            int expectedDepth,
            int expectedBeam) throws Exception {
        AI bot = AndroidBotFactory.create(difficulty, 1, BOARD_SIZE);
        assertEquals(expectedDepth, readInt(bot, "maxDepth"));
        assertEquals(expectedBeam, readInt(bot, "beamSize"));
    }

    private static int readInt(AI bot, String fieldName) throws Exception {
        Field field = BeeGameAI.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getInt(bot);
    }

    private static Move openingMove(int difficulty) {
        AI bot = AndroidBotFactory.create(difficulty, 1, BOARD_SIZE);
        Game game = game(bot, new PlayerObject(2));
        bot.getPlayerTurn(game);
        return game.getMoveList().getMove();
    }

    private static Move replyTo(int difficulty, Point opening) {
        PlayerObject human = new PlayerObject(1);
        AI bot = AndroidBotFactory.create(difficulty, 2, BOARD_SIZE);
        Game game = game(human, bot);
        assertTrue(GameAction.makeMove(human, opening, game));
        bot.getPlayerTurn(game);
        Move reply = game.getMoveList().getMove();
        assertNotEquals(1, reply.getMoveNumber());
        return reply;
    }

    private static List<Move> playLine(int difficulty, Point[] opponentMoves) {
        PlayerObject human = new PlayerObject(1);
        AI bot = AndroidBotFactory.create(difficulty, 2, BOARD_SIZE);
        Game game = game(human, bot);
        List<Move> replies = new ArrayList<>();
        for (Point opponentMove : opponentMoves) {
            assertTrue("opponent fixture must stay legal",
                    GameAction.makeMove(human, opponentMove, game));
            bot.getPlayerTurn(game);
            replies.add(game.getMoveList().getMove());
        }
        return replies;
    }

    private static void assertLine(List<Move> actual, int[][] expected) {
        assertEquals(expected.length, actual.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals("x at move " + i, expected[i][0], actual.get(i).getX());
            assertEquals("y at move " + i, expected[i][1], actual.get(i).getY());
        }
    }

    private static Game game(
            PlayingEntity playerOne,
            PlayingEntity playerTwo) {
        Game.GameOptions options = new Game.GameOptions();
        options.gridSize = BOARD_SIZE;
        options.swap = false;
        options.timer = new Timer(0, 0, Timer.NO_TIMER);
        return new Game(options, playerOne, playerTwo);
    }
}
