package com.sam.hex.fragment;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;
import com.hex.core.Game;
import com.sam.hex.FileUtil;
import com.sam.hex.R;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import static com.sam.hex.PermissionUtils.hasPermissions;
import static com.sam.hex.Settings.TAG;

/**
 * @author Will Harmon
 **/
public class HistoryFragment extends HexFragment {
    private static final File PATH = new File(Environment.getExternalStorageDirectory() + File.separator + "Hex");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        GridView games = view.findViewById(R.id.games);
        Item[] fileList = loadFileList();
        games.setAdapter(new HistoryAdapter(getMainActivity(), fileList));
        games.setOnItemClickListener((parent, v, position, id) -> openFile(PATH + File.separator + fileList[position].file));

        return view;
    }

    @NonNull
    private Item[] loadFileList() {
        // We cannot look up the saved games without read permission.
        if (!hasPermissions(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return new Item[0];
        }

        // Create the path if able.
        if (hasPermissions(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (PATH.mkdirs()) {
                Log.d(TAG, "Successfully made the directory for history");
            }
        }

        Item[] items = new Item[0];
        if (PATH.exists()) {
            FilenameFilter filter = (dir, filename) -> {
                File sel = new File(dir, filename);
                // Filters based on whether the file is hidden or not
                return (sel.isFile() || sel.isDirectory()) && !sel.isHidden();
            };

            String[] files = PATH.list(filter);
            if (files != null) {
                items = new Item[files.length];
                for (int i = 0; i < files.length; i++) {
                    items[i] = new Item(files[i]);
                }
            }
        }

        Arrays.sort(items, (f1, f2) -> f2.file.compareTo(f1.file));
        return items;
    }

    private static class Item {
        @NonNull
        final String file;

        @Nullable
        String title;
        @Nullable
        String date;
        int team;

        @Nullable
        Game game;

        Item(@NonNull String file) {
            this.file = file;
        }

        @UiThread
        void initialize(Context context, Runnable onLoadedCallback) {
            final Handler h = new Handler();
            new Thread(() -> {
                Game game;
                try {
                    game = Game.load(FileUtil.loadGameAsString(PATH + File.separator + file));
                } catch (IOException | JsonSyntaxException e) {
                    e.printStackTrace();
                    return;
                }

                // The last player to make the move is considered the winner.
                int team = game.getMoveList().getMove().getTeam();
                String title = context.getString(R.string.auto_saved_title, game.getPlayer1().getName(), game.getPlayer2().getName());
                String date = DATE_FORMAT.format(new Date(game.getGameStart()));

                h.post(() -> {
                    this.game = game;
                    this.team = team;
                    this.title = title;
                    this.date = date;
                    onLoadedCallback.run();
                });
            }).start();
        }
    }

    private void openFile(String fileName) {
        try {
            Bundle b = new Bundle();
            b.putString(GameFragment.GAME, FileUtil.loadGameAsString(fileName));
            b.putBoolean(GameFragment.REPLAY, true);

            GameFragment gameFragment = new GameFragment();
            gameFragment.setArguments(b);
            swapFragment(gameFragment);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getMainActivity(), R.string.game_toast_failed, Toast.LENGTH_SHORT).show();
        }
    }

    static class HistoryAdapter extends BaseAdapter {
        private final Context context;
        private final Item[] files;

        HistoryAdapter(Context context, @NonNull Item[] files) {
            this.context = context;
            this.files = files;
        }

        @Nullable
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
            final Item i = files[position];
            final View v = convertView != null ? convertView : View.inflate(context, R.layout.view_history_item, null);

            // Load up the game so we can get information
            if (i.title == null || i.date == null || i.team == 0) {
                i.initialize(context, this::notifyDataSetChanged);
            }

            TextView title = v.findViewById(R.id.title);
            TextView date = v.findViewById(R.id.date);

            title.setText(i.title);
            date.setText(i.date);

            if (i.team == 1) {
                v.setBackgroundResource(R.drawable.history_background_red);
            } else if (i.team == 2) {
                v.setBackgroundResource(R.drawable.history_background_blue);
            } else {
                v.setBackgroundResource(R.drawable.history_background_black);
            }

            return v;
        }

        @Override
        public int getCount() {
            return files.length;
        }

        @Override
        public Object getItem(int position) {
            return files[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}
