package com.sam.hex.fragment;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.sam.hex.R;

/**
 * @author Will Harmon
 **/
public class InstructionsFragment extends SherlockFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getSherlockActivity().getSupportActionBar().show();
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        View v = inflater.inflate(R.layout.instructions, null);

        TextView view = (TextView) v.findViewById(R.id.rules);
        view.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }
}
