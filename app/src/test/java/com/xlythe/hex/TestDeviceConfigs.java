package com.xlythe.hex;

import com.android.resources.ScreenOrientation;

import app.cash.paparazzi.DeviceConfig;

/** Device configurations shared by golden and responsive layout tests. */
final class TestDeviceConfigs {
    static final DeviceConfig PHONE_LANDSCAPE = landscape(DeviceConfig.PIXEL_5);
    static final DeviceConfig TABLET_LANDSCAPE = landscape(DeviceConfig.PIXEL_TABLET);

    private TestDeviceConfigs() {}

    static DeviceConfig landscape(DeviceConfig source) {
        return source.copy(
                source.getScreenHeight(),
                source.getScreenWidth(),
                source.getXdpi(),
                source.getYdpi(),
                ScreenOrientation.LANDSCAPE,
                source.getUiMode(),
                source.getNightMode(),
                source.getDensity(),
                source.getFontScale(),
                source.getLayoutDirection(),
                source.getLocale(),
                source.getRatio(),
                source.getSize(),
                source.getKeyboard(),
                source.getTouchScreen(),
                source.getKeyboardState(),
                source.getSoftButtons(),
                source.getNavigation(),
                source.getScreenRound());
    }
}
