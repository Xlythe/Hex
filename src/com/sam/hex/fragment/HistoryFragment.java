package com.sam.hex.fragment;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.sam.hex.FileUtil;
import com.sam.hex.R;

/**
 * @author Will Harmon
 **/
public class HistoryFragment extends HexFragment {
    private Item[] fileList;
    public static File path = new File(Environment.getExternalStorageDirectory() + File.separator + "Hex" + File.separator);
    public static String chosenFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_history, null);

        GridView games = (GridView) v.findViewById(R.id.games);
        try {
            loadFileList();
        }
        catch(NullPointerException e) {
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
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }

        // Checks whether path exists
        if(path.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    // Filters based on whether the file is hidden or not
                    return (sel.isFile() || sel.isDirectory()) && !sel.isHidden();

                }
            };

            String[] fList = path.list(filter);
            fileList = new Item[fList.length];
            for(int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i]);
            }
        }

        Arrays.sort(fileList, new Comparator<Item>() {
            @Override
            public int compare(Item f1, Item f2) {
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
        }
        catch(IOException e) {
            e.printStackTrace();
            Toast.makeText(getMainActivity(), R.string.game_toast_failed, Toast.LENGTH_SHORT).show();
        }
    }

    public static class HistoryAdapter extends BaseAdapter {
        private Context context;
        private final Item[] files;

        public HistoryAdapter(Context context, Item[] files) {
            this.context = context;
            this.files = files;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Item i = files[position];
            View v = convertView;
            if(v == null) {
                v = View.inflate(context, R.layout.view_history_item, null);
            }

            // TODO determine winner for color, grab p1 and p2 names, grab
            // date
            if(i.title == null) {
                i.title = i.file;
            }
            if(i.date == null) {
                i.date = i.file;
            }
            if(i.color == 0) {
                i.color = (int) (Math.random() * 2) + 1;
            }

            TextView title = (TextView) v.findViewById(R.id.title);
            TextView date = (TextView) v.findViewById(R.id.date);

            title.setText(i.title);
            date.setText(i.date);

            if(i.color == 1) {
                v.setBackgroundResource(R.drawable.history_background_red);
            }
            else {
                v.setBackgroundResource(R.drawable.history_background_blue);
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
            return 0;
        }
    }
}
