package com.xlythe.hex;

import android.content.Context;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import androidx.annotation.NonNull;

/**
 * @author Will Harmon
 **/
public class FileUtil {
    private static final String FOLDER = "Hex";
    private static final String EXTENSION = ".rhex";
    private static final int MAX_FILE_NAME_LENGTH = 120;

    public static String loadGameAsString(Context context, @NonNull String fileName) throws IOException {
        return loadGameAsString(historyDirectory(context), fileName);
    }

    static String loadGameAsString(File directory, @NonNull String fileName) throws IOException {
        File file = resolveReplay(directory, fileName);
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append('\n');
            }
        }
        return text.toString();
    }

    public static void autoSaveGame(Context context, @NonNull String fileName, String gameState) throws IOException {
        saveGame(historyDirectory(context), fileName, gameState);
    }

    static void saveGame(
            File directory,
            @NonNull String fileName,
            @NonNull String gameState) throws IOException {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create replay directory");
        }
        File destination = resolveReplay(directory, ensureExtension(fileName));
        File temporary = new File(directory, destination.getName() + ".tmp");
        File backup = new File(directory, destination.getName() + ".bak");

        try (FileOutputStream stream = new FileOutputStream(temporary, false);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                     stream, StandardCharsets.UTF_8))) {
            writer.write(gameState);
            writer.flush();
            stream.getFD().sync();
        }

        if (backup.exists() && !backup.delete()) {
            throw new IOException("Could not remove stale replay backup");
        }
        if (destination.exists() && !destination.renameTo(backup)) {
            throw new IOException("Could not prepare replay replacement");
        }
        if (!temporary.renameTo(destination)) {
            if (backup.exists()) backup.renameTo(destination);
            throw new IOException("Could not commit replay");
        }
        if (backup.exists() && !backup.delete()) {
            backup.deleteOnExit();
        }
    }

    public static File historyDirectory(Context context) {
        return new File(context.getFilesDir(), FOLDER);
    }

    private static File resolveReplay(File directory, String fileName) throws IOException {
        String leafName = new File(fileName).getName();
        if (!leafName.equals(fileName)
                || leafName.contains("..")
                || leafName.trim().isEmpty()
                || leafName.length() > MAX_FILE_NAME_LENGTH) {
            throw new IOException("Invalid replay file name");
        }
        File resolved = new File(directory, leafName);
        String parent = directory.getCanonicalPath();
        String child = resolved.getCanonicalPath();
        if (!child.startsWith(parent + File.separator)) {
            throw new IOException("Replay path escapes its directory");
        }
        if (!resolved.exists()) {
            File backup = new File(directory, leafName + ".bak");
            if (backup.exists() && !backup.renameTo(resolved)) {
                throw new IOException("Could not recover replay backup");
            }
        }
        return resolved;
    }

    private static String ensureExtension(String fileName) {
        return fileName.toLowerCase(java.util.Locale.ROOT).endsWith(EXTENSION)
                ? fileName
                : fileName + EXTENSION;
    }
}
