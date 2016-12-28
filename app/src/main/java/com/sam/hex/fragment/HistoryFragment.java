package com.sam.hex.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.hex.core.Game;
import com.hex.core.Game.GameListener;
import com.hex.core.PlayingEntity;
import com.sam.hex.FileUtil;
import com.sam.hex.R;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * @author Will Harmon
 **/
public class HistoryFragment extends HexFragment {
    private Item[] fileList;
    @NonNull
    public static File path = new File(Environment.getExternalStorageDirectory() + File.separator + "Hex" + File.separator);
    public static String chosenFile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_history, null);

        GridView games = (GridView) v.findViewById(R.id.games);
        try {
            loadFileList();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        games.setAdapter(new HistoryAdapter(getMainActivity(), fileList));
        games.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                openFile(path + File.separator + fileList[position].file);
            }
        });

        return v;
    }

    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // Checks whether path exists
        if (path.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, @NonNull String filename) {
                    File sel = new File(dir, filename);
                    // Filters based on whether the file is hidden or not
                    return (sel.isFile() || sel.isDirectory()) && !sel.isHidden();

                }
            };

            String[] fList = path.list(filter);
            fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i]);
            }
        }

        Arrays.sort(fileList, new Comparator<Item>() {
            @Override
            public int compare(@NonNull Item f1, @NonNull Item f2) {
                return f2.file.compareTo(f1.file);
            }
        });
    }

    private class Item {
        public final String file;
        public String title;
        public String date;
        public int color;

        public Item(String file) {
            this.file = file;
        }
    }

    private void openFile(String fileName) {
        try {
            Bundle b = new Bundle();
            b.putString(GameFragment.GAME, FileUtil.loadGameAsString(fileName));
            b.putBoolean(GameFragment.REPLAY, true);

            getMainActivity().setGameFragment(new GameFragment());
            getMainActivity().getGameFragment().setArguments(b);
            getMainActivity().swapFragment(getMainActivity().getGameFragment());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getMainActivity(), R.string.game_toast_failed, Toast.LENGTH_SHORT).show();
        }
    }

    public static class HistoryAdapter extends BaseAdapter {
        private Context context;
        private final Item[] files;
        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        public HistoryAdapter(Context context, Item[] files) {
            this.context = context;
            this.files = files;
        }

        @Nullable
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
            final Item i = files[position];
            final View v = convertView != null ? convertView : View.inflate(context, R.layout.view_history_item, null);

            // Load up the game so we can get information
            if (i.title == null || i.date == null || i.color == 0) {
                try {
                    final Handler h = new Handler();
                    Game g = Game.load(FileUtil.loadGameAsString(path + File.separator + i.file));
                    g.setGameListener(new GameListener() {
                        @Override
                        public void startTimer() {
                        }

                        @Override
                        public void onWin(@NonNull PlayingEntity player) {
                            i.color = player.getTeam();
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (i.color == 1) {
                                        v.setBackgroundResource(R.drawable.history_background_red);
                                    } else if (i.color == 2) {
                                        v.setBackgroundResource(R.drawable.history_background_blue);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onUndo() {
                        }

                        @Override
                        public void onTurn(PlayingEntity player) {
                        }

                        @Override
                        public void onStop() {
                        }

                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onReplayStart() {
                        }

                        @Override
                        public void onReplayEnd() {
                        }

                        @Override
                        public void onClear() {
                        }

                        @Override
                        public void displayTime(int minutes, int seconds) {
                        }
                    });
                    g.replay(0);
                    i.title = context.getString(R.string.auto_saved_title, g.getPlayer1().getName(), g.getPlayer2().getName());
                    i.date = DATE_FORMAT.format(new Date(g.getGameStart()));
                    i.color = -1;
                } catch (Exception e) {
                    e.printStackTrace();
                    i.title = "";
                    i.date = context.getString(R.string.game_toast_failed);
                    i.color = -1;
                }
            }

            TextView title = (TextView) v.findViewById(R.id.title);
            TextView date = (TextView) v.findViewById(R.id.date);

            title.setText(i.title);
            date.setText(i.date);

            if (i.color == 1) {
                v.setBackgroundResource(R.drawable.history_background_red);
            } else if (i.color == 2) {
                v.setBackgroundResource(R.drawable.history_background_blue);
            } else {
                v.setBackgroundResource(R.drawable.history_background_black);
            }

            return v;
        }

        @Override
        public int getCount() {
            if (files != null) return files.length;
            else return 0;
        }

        @Override
        public Object getItem(int position) {
            return files[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }
}
