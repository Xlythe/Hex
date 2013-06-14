package com.sam.hex.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.android.vending.billing.util.IabResult;
import com.sam.hex.MainActivity;
import com.sam.hex.R;
import com.sam.hex.Stats;
import com.sam.hex.view.HexDialogView.Button.OnClickListener;

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
        ImageView iv = (ImageView) View.inflate(this, R.layout.donate_dialog_textview, null);
        iv.setImageResource(R.drawable.donate_gold_d);
        return iv;
    }

    @Override
    public View getNegativeView() {
        ImageView iv = (ImageView) View.inflate(this, R.layout.donate_dialog_textview, null);
        iv.setImageResource(R.drawable.donate_bronze_d);
        return iv;
    }

    @Override
    public View getNeutralView() {
        ImageView iv = (ImageView) View.inflate(this, R.layout.donate_dialog_textview, null);
        iv.setImageResource(R.drawable.donate_silver_d);
        return iv;
    }

    @Override
    public OnClickListener getPositiveOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick() {
                purchaseItem(MainActivity.ITEM_SKU_ADVANCED);
                dismiss();
            }
        };
    }

    @Override
    public OnClickListener getNegativeOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick() {
                purchaseItem(MainActivity.ITEM_SKU_BASIC);
                dismiss();
            }
        };
    }

    @Override
    public OnClickListener getNeutralOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick() {
                purchaseItem(MainActivity.ITEM_SKU_INTERMEDIATE);
                dismiss();
            }
        };
    }

    @Override
    protected void dealWithIabSetupSuccess() {}

    @Override
    protected void dealWithIabSetupFailure() {}

    @Override
    protected void dealWithPurchaseSuccess(IabResult result, String sku) {
        int amount = 0;
        if(sku.equals(ITEM_SKU_BASIC)) {
            amount = 1;
        }
        else if(sku.equals(ITEM_SKU_INTERMEDIATE)) {
            amount = 3;
        }
        else if(sku.equals(ITEM_SKU_ADVANCED)) {
            amount = 5;
        }
        Stats.incrementDonationAmount(this, amount);
        dismiss();
    }

    @Override
    protected void dealWithPurchaseFailed(IabResult result) {}

    @Override
    public float getPositiveXPercent() {
        return 0.75f;
    }

    @Override
    public float getPositiveYPercent() {
        return 0.50f;
    }

    @Override
    public float getPositiveSideLengthPercent() {
        return 0.10f;
    }

    @Override
    public float getNegativeXPercent() {
        return 0.25f;
    }

    @Override
    public float getNegativeYPercent() {
        return 0.50f;
    }

    @Override
    public float getNegativeSideLengthPercent() {
        return 0.10f;
    }

    @Override
    public float getNeutralXPercent() {
        return 0.50f;
    }

    @Override
    public float getNeutralYPercent() {
        return 0.25f;
    }

    @Override
    public float getNeutralSideLengthPercent() {
        return 0.10f;
    }
}
