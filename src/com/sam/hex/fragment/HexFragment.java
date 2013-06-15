package com.sam.hex.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sam.hex.MainActivity;

/**
 * @author Will Harmon
 **/
public class HexFragment extends Fragment {
    private ViewGroup mContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mContainer = container;
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mContainer.requestFocus();
    }

    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
