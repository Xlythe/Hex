package com.sam.hex.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.sam.hex.R;

/**
 * @author Will Harmon
 **/
public class DonateDialog extends HexDialog {
    public DonateDialog(Context context) {
        super(context);
    }

    public DonateDialog() {
        super();
    }

    @Override
    public View getPositiveView() {
        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.play_again);
        return iv;
    }

    @Override
    public View getNegativeView() {
        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.home);
        return iv;
    }

    @Override
    public View getNeutralView() {
        return null;
    }
}
