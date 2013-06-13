package com.sam.hex.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Window;

import com.sam.hex.R;

public class HexDialog extends Activity {
    private HexDialogView view;

    private Context context;

    private HexDialogView.Button.OnClickListener positiveOnClickListener;
    private String positiveText;
    private Drawable positiveDrawable;
    private HexDialogView.Button.OnClickListener negativeOnClickListener;
    private String negativeText;
    private Drawable negativeDrawable;
    private HexDialogView.Button.OnClickListener neutralOnClickListener;
    private String neutralText;
    private Drawable neutralDrawable;

    public HexDialog(Context context) {
        this.context = context;
    }

    public HexDialog() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        view = new HexDialogView(this, this);
        view.setBackgroundResource(android.R.color.transparent);

        setContentView(view);

        HexDialogView.Button positive = view.getButtons()[2];
        HexDialogView.Button negative = view.getButtons()[0];
        HexDialogView.Button neutral = view.getButtons()[1];

        positive.setText(positiveText);
        positive.setDrawable(getResources().getDrawable(R.drawable.play_again));
        negative.setText(negativeText);
        negative.setDrawable(getResources().getDrawable(R.drawable.home));
        neutral.setText(neutralText);
        neutral.setDrawable(neutralDrawable);
    }

    public void setPositiveButton(String text, Drawable drawable, HexDialogView.Button.OnClickListener positiveOnClickListener) {
        positiveText = text;
        positiveDrawable = drawable;
        if(positiveOnClickListener == null) {
            positiveOnClickListener = new HexDialogView.Button.OnClickListener() {
                @Override
                public void onClick() {
                    dismiss();
                }
            };
        }
        this.positiveOnClickListener = positiveOnClickListener;
    }

    public void setNegativeButton(String text, Drawable drawable, HexDialogView.Button.OnClickListener negativeOnClickListener) {
        this.negativeText = text;
        this.negativeDrawable = drawable;
        if(negativeOnClickListener == null) {
            negativeOnClickListener = new HexDialogView.Button.OnClickListener() {
                @Override
                public void onClick() {
                    dismiss();
                }
            };
        }
        this.negativeOnClickListener = negativeOnClickListener;
    }

    public void setNeutralButton(String text, Drawable drawable, HexDialogView.Button.OnClickListener neutralOnClickListener) {
        this.neutralText = text;
        this.neutralDrawable = drawable;
        if(neutralOnClickListener == null) {
            neutralOnClickListener = new HexDialogView.Button.OnClickListener() {
                @Override
                public void onClick() {
                    dismiss();
                }
            };
        }
        this.neutralOnClickListener = neutralOnClickListener;
    }

    public void dismiss() {
        finish();
    }

    public void show() {
        context.startActivity(new Intent(context, this.getClass()));
    }
}
