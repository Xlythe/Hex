package com.sam.hex.fragment;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.sam.hex.FileUtil;
import com.sam.hex.MainActivity;
import com.sam.hex.R;

public class HistoryFragment extends SherlockListFragment {

    // Stores names of traversed directories
    ArrayList<String> str = new ArrayList<String>();

    // Check if the first level of the directory structure is the one showing
    private Boolean firstLvl = true;

    private static final String TAG = "F_PATH";

    private Item[] fileList;
    public static File path = new File(Environment.getExternalStorageDirectory() + File.separator + "Hex" + File.separator);
    public static String chosenFile;

    ListAdapter adapter;
    ListView view;
    Handler handle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        rootView.setBackgroundResource(R.color.background);
        handle = new Handler();
        try {
            loadFileList();
            refreshView();
        }
        catch(NullPointerException e) {
            e.printStackTrace();
        }
        return rootView;
    }

    private void loadFileList() {
        try {
            path.mkdirs();
        }
        catch(SecurityException e) {
            Log.e(TAG, "unable to write on the sd card ");
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
                fileList[i] = new Item(fList[i], R.drawable.file_icon);

                // Convert into file path
                File sel = new File(path, fList[i]);

                // Set drawables
                if(sel.isDirectory()) {
                    fileList[i].icon = R.drawable.directory_icon;
                    Log.d("DIRECTORY", fileList[i].file);
                }
                else if(fList[i].endsWith(".rhex")) {
                    fileList[i].icon = R.drawable.file_hex_icon;
                    Log.d("HEX FILE", fileList[i].file);
                }
                else {
                    Log.d("FILE", fileList[i].file);
                }
            }

            if(!firstLvl) {
                Item temp[] = new Item[fileList.length + 1];
                for(int i = 0; i < fileList.length; i++) {
                    temp[i + 1] = fileList[i];
                }
                temp[0] = new Item("Up", R.drawable.directory_up);
                fileList = temp;
            }
        }
        else {
            Log.e(TAG, "path does not exist");
        }

        Arrays.sort(fileList, new Comparator<Item>() {
            public int compare(Item f1, Item f2) {
                if(f1.file.equals("Up")) return -1;
                if(f2.file.equals("Up")) return 1;

                File file1 = new File(path, f1.file);
                File file2 = new File(path, f2.file);
                if(file1.isDirectory() != file2.isDirectory()) {
                    return file1.isDirectory() ? -1 : 1;
                }
                return f1.file.compareTo(f2.file);
            }
        });
        adapter = new ArrayAdapter<Item>(getSherlockActivity(), android.R.layout.select_dialog_item, android.R.id.text1, fileList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // creates view
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);

                // put the image on the text view
                textView.setCompoundDrawablesWithIntrinsicBounds(fileList[position].icon, 0, 0, 0);

                // add margin between image and text (support various screen
                // densities)
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                textView.setCompoundDrawablePadding(dp5);

                return view;
            }
        };

    }

    private class Item {
        public String file;
        public int icon;

        public Item(String file, Integer icon) {
            this.file = file;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return file;
        }
    }

    private void refreshView() {
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        chosenFile = fileList[position].file;
        File sel = new File(path + "/" + chosenFile);
        if(sel.isDirectory()) {
            firstLvl = false;

            // Adds chosen directory to list
            str.add(chosenFile);
            fileList = null;
            path = new File(sel + "");

            loadFileList();

            refreshView();
        }

        // Checks if 'up' was clicked
        else if(chosenFile.equalsIgnoreCase("up") && !sel.exists()) {

            // present directory removed from list
            String s = str.remove(str.size() - 1);

            // path modified to exclude present directory
            path = new File(path.toString().substring(0, path.toString().lastIndexOf(s)));
            fileList = null;

            // if there are no more directories in the list, then
            // its the first level
            if(str.isEmpty()) {
                firstLvl = true;
            }
            loadFileList();
            refreshView();
        }
        // File picked
        else {
            try {
                Bundle b = new Bundle();
                b.putString(GameFragment.GAME, FileUtil.loadGameAsString(path + File.separator + chosenFile));
                b.putBoolean(GameFragment.REPLAY, true);

                getMainActivity().setGameFragment(new GameFragment());
                getMainActivity().getGameFragment().setArguments(b);
                getMainActivity().swapFragmentWithoutBackStack(getMainActivity().getGameFragment());
            }
            catch(IOException e) {
                e.printStackTrace();
                Toast.makeText(getMainActivity(), R.string.failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getSherlockActivity();
    }
}
