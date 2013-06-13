package com.sam.hex.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.android.vending.billing.util.IabResult;
import com.sam.hex.MainActivity;
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
        TextView tv = new TextView(this);
        tv.setGravity(Gravity.CENTER);
        tv.setText("$5");
        return tv;
    }

    @Override
    public View getNegativeView() {
        TextView tv = new TextView(this);
        tv.setGravity(Gravity.CENTER);
        tv.setText("$1");
        return tv;
    }

    @Override
    public View getNeutralView() {
        TextView tv = new TextView(this);
        tv.setGravity(Gravity.CENTER);
        tv.setText("$3");
        return tv;
    }

    @Override
    public OnClickListener getPositiveOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick() {
                purchaseItem(MainActivity.ITEM_SKU_ADVANCED);
            }
        };
    }

    @Override
    public OnClickListener getNegativeOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick() {
                purchaseItem(MainActivity.ITEM_SKU_BASIC);
            }
        };
    }

    @Override
    public OnClickListener getNeutralOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick() {
                purchaseItem(MainActivity.ITEM_SKU_INTERMEDIATE);
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
    }

    @Override
    protected void dealWithPurchaseFailed(IabResult result) {}
}
