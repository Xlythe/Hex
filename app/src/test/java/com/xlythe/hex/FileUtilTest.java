package com.xlythe.hex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileUtilTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void savesUtf8ReplayAndReplacesRatherThanAppending() throws Exception {
        File directory = temporaryFolder.newFolder("history");

        FileUtil.saveGame(directory, "Alice vs Bob", "first");
        FileUtil.saveGame(directory, "Alice vs Bob", "second ♡");

        assertEquals("second ♡\n", FileUtil.loadGameAsString(
                directory, "Alice vs Bob.rhex"));
        assertFalse(new File(directory, "Alice vs Bob.rhex.tmp").exists());
        assertFalse(new File(directory, "Alice vs Bob.rhex.bak").exists());
    }

    @Test
    public void rejectsTraversalAndAbsolutePaths() throws Exception {
        File directory = temporaryFolder.newFolder("history");

        assertThrows(IOException.class, () ->
                FileUtil.saveGame(directory, "../outside", "game"));
        assertThrows(IOException.class, () ->
                FileUtil.loadGameAsString(directory, new File(directory, "game.rhex").getPath()));
    }

    @Test
    public void recoversInterruptedReplacementFromBackup() throws Exception {
        File directory = temporaryFolder.newFolder("history");
        File backup = new File(directory, "game.rhex.bak");
        Files.write(backup.toPath(), "safe".getBytes(StandardCharsets.UTF_8));

        assertEquals("safe\n", FileUtil.loadGameAsString(directory, "game.rhex"));
        assertTrue(new File(directory, "game.rhex").exists());
    }
}
