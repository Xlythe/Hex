package com.sam.hex.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

public class HexDialog extends Activity {
    private HexDialogView view;

    private Context context;

    private HexDialogView.Button.OnClickListener positiveOnClickListener;
    private String positiveText;
    private HexDialogView.Button.OnClickListener negativeOnClickListener;
    private String negativeText;
    private HexDialogView.Button.OnClickListener neutralOnClickListener;
    private String neutralText;

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

        view = new HexDialogView(this);
        view.setBackgroundResource(android.R.color.transparent);

        setContentView(view);

        HexDialogView.Button positive = view.getButtons()[0];
        HexDialogView.Button negative = view.getButtons()[1];
        HexDialogView.Button neutral = view.getButtons()[2];

        positive.setText(positiveText);
        negative.setText(negativeText);
        neutral.setText(neutralText);
    }

    public void setPositiveButton(String text, HexDialogView.Button.OnClickListener positiveOnClickListener) {
        this.positiveText = text;
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

    public void setNegativeButton(String text, HexDialogView.Button.OnClickListener negativeOnClickListener) {
        this.negativeText = text;
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

    public void setNeutralButton(String text, HexDialogView.Button.OnClickListener neutralOnClickListener) {
        this.neutralText = text;
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
