package com.xlythe.hex.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.hex.core.Game;
import com.xlythe.hex.FileUtil;
import com.xlythe.hex.R;
import com.xlythe.hex.AppExecutors;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import static com.xlythe.hex.Settings.TAG;

/**
 * @author Will Harmon
 **/
public class HistoryFragment extends HexFragment {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private HistoryAdapter historyAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        GridView games = view.findViewById(R.id.games);
        Item[] fileList = loadFileList();
        historyAdapter = new HistoryAdapter(
                requireContext().getApplicationContext(),
                fileList,
                mainHandler);
        games.setAdapter(historyAdapter);
        games.setOnItemClickListener((parent, v, position, id) -> openFile(fileList[position].file));

        return view;
    }

    @Override
    public void onDestroyView() {
        historyAdapter = null;
        super.onDestroyView();
    }

    @NonNull
    private Item[] loadFileList() {
        File folder = FileUtil.historyDirectory(requireContext());

        // Create the path if able.
        if (folder.mkdirs()) {
            Log.d(TAG, "Successfully made the directory for history");
        }

        Item[] items = new Item[0];
        if (folder.exists()) {
            FilenameFilter filter = (dir, filename) -> {
                File sel = new File(dir, filename);
                // Filters based on whether the file is hidden or not
                return sel.isFile()
                        && !sel.isHidden()
                        && filename.toLowerCase(Locale.ROOT).endsWith(".rhex");
            };

            String[] files = folder.list(filter);
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
        boolean loading;
        boolean loaded;

        Item(@NonNull String file) {
            this.file = file;
        }

        @UiThread
        synchronized void initialize(
                Context context,
                Handler mainHandler,
                Runnable onLoadedCallback) {
            if (loading || loaded) return;
            loading = true;
            AppExecutors.io().execute(() -> {
                Game game;
                try {
                    game = Game.load(FileUtil.loadGameAsString(context, file));
                } catch (IOException | RuntimeException error) {
                    Log.w(TAG, "Could not load replay " + file, error);
                    synchronized (this) {
                        loading = false;
                        loaded = true;
                        title = file;
                        date = "";
                    }
                    mainHandler.post(onLoadedCallback);
                    return;
                }

                // The last player to make the move is considered the winner.
                int team = game.getMoveList().size() == 0
                        ? 0
                        : game.getMoveList().getMove().getTeam();
                String title = context.getString(R.string.auto_saved_title, game.getPlayer1().getName(), game.getPlayer2().getName());
                String date = new SimpleDateFormat(
                        "MMM dd, yyyy", Locale.getDefault())
                        .format(new Date(game.getGameStart()));

                mainHandler.post(() -> {
                    synchronized (this) {
                        this.game = game;
                        this.team = team;
                        this.title = title;
                        this.date = date;
                        this.loading = false;
                        this.loaded = true;
                    }
                    onLoadedCallback.run();
                });
            });
        }
    }

    private void openFile(String fileName) {
        Context applicationContext = requireContext().getApplicationContext();
        AppExecutors.io().execute(() -> {
            try {
                String replay = FileUtil.loadGameAsString(applicationContext, fileName);
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    Bundle arguments = new Bundle();
                    arguments.putString(GameFragment.GAME, replay);
                    arguments.putBoolean(GameFragment.REPLAY, true);

                    GameFragment gameFragment = new GameFragment();
                    gameFragment.setArguments(arguments);
                    swapFragment(gameFragment);
                });
            } catch (IOException error) {
                Log.w(TAG, "Could not open replay " + fileName, error);
                mainHandler.post(() -> {
                    if (isAdded()) {
                        Toast.makeText(
                                requireContext(),
                                R.string.game_toast_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    static class HistoryAdapter extends BaseAdapter {
        private final Context context;
        private final Item[] files;
        private final Handler mainHandler;

        HistoryAdapter(Context context, @NonNull Item[] files, Handler mainHandler) {
            this.context = context;
            this.files = files;
            this.mainHandler = mainHandler;
        }

        @Nullable
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
            final Item i = files[position];
            final View v = convertView != null ? convertView : View.inflate(context, R.layout.view_history_item, null);

            // Load up the game so we can get information
            if (i.title == null || i.date == null || i.team == 0) {
                i.initialize(context, mainHandler, this::notifyDataSetChanged);
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
