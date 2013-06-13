package com.sam.hex;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabHelper.OnIabPurchaseFinishedListener;
import com.android.vending.billing.util.IabHelper.OnIabSetupFinishedListener;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Inventory;
import com.android.vending.billing.util.Purchase;

public abstract class PurchaseActivity extends FragmentActivity implements OnIabSetupFinishedListener, OnIabPurchaseFinishedListener {
    private static final String KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzQ+l7jJokYE9r6/mNo9JHr6CORtkxkAD22H57FJfsl0kOtHRZhFy5Lx58MmxFQFKVxhrcm824pz7sdI2L/O+qXIH2c/xU0e4YQt8RovBOUn9w58lR7yZjnSOMkRJ5vnUG8IrJUL5LTpw7juOcodACsdLm8wMpoMmwtDvAfNkANvO1Ui810WhwG5aaMyQMcfyb/HjCGxNAIAgQX+KjcndAZ8CGjV6stRFrOy0VSposeeItzihgM1MEJlWbRK3Ndgzh8fuyZkbOy6nC05fV1nYUs3kMPaywNT9BV4oDpkmJM+16HXWV34wx74e9C0HTZKvrQGrKhehTpzS6MLSK7U8VQIDAQAB";
    public static final String ITEM_SKU_BASIC = "hex.basic";
    public static final String ITEM_SKU_INTERMEDIATE = "hex.intermediate";
    public static final String ITEM_SKU_ADVANCED = "hex.advanced";
    private IabHelper billingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        billingHelper = new IabHelper(this, KEY);
        billingHelper.startSetup(this);
    }

    @Override
    public void onIabSetupFinished(IabResult result) {
        if(result.isSuccess()) {
            dealWithIabSetupSuccess();
        }
        else {
            dealWithIabSetupFailure();
        }

        // Check to see if the item has already been purchased
        IabHelper.QueryInventoryFinishedListener gotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                if(inventory.hasPurchase(ITEM_SKU_BASIC)) {
                    dealWithPurchaseSuccess(result, ITEM_SKU_BASIC);
                }
                else if(inventory.hasPurchase(ITEM_SKU_INTERMEDIATE)) {
                    dealWithPurchaseSuccess(result, ITEM_SKU_INTERMEDIATE);
                }
                else if(inventory.hasPurchase(ITEM_SKU_ADVANCED)) {
                    dealWithPurchaseSuccess(result, ITEM_SKU_ADVANCED);
                }
            }
        };
        List<String> skuList = new ArrayList<String>();
        skuList.add(ITEM_SKU_BASIC);
        skuList.add(ITEM_SKU_INTERMEDIATE);
        skuList.add(ITEM_SKU_ADVANCED);
        billingHelper.queryInventoryAsync(true, skuList, gotInventoryListener);
    }

    protected abstract void dealWithIabSetupSuccess();

    protected abstract void dealWithIabSetupFailure();

    protected abstract void dealWithPurchaseSuccess(IabResult result, String sku);

    protected abstract void dealWithPurchaseFailed(IabResult result);

    public void purchaseItem(String sku) {
        billingHelper.launchPurchaseFlow(this, sku, 123, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        billingHelper.handleActivityResult(requestCode, resultCode, data);
    }

    /**
     * Security Recommendation: When you receive the purchase response from
     * Google Play, make sure to check the returned data signature, the orderId,
     * and the developerPayload string in the Purchase object to make sure that
     * you are getting the expected values. You should verify that the orderId
     * is a unique value that you have not previously processed, and the
     * developerPayload string matches the token that you sent previously with
     * the purchase request. As a further security precaution, you should
     * perform the verification on your own secure server.
     */
    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase info) {
        if(result.isFailure()) {
            dealWithPurchaseFailed(result);
        }
        else {
            dealWithPurchaseSuccess(result, info.getSku());
        }
    }

    @Override
    protected void onDestroy() {
        disposeBillingHelper();
        super.onDestroy();
    }

    private void disposeBillingHelper() {
        if(billingHelper != null) {
            billingHelper.dispose();
        }
        billingHelper = null;
    }
}
