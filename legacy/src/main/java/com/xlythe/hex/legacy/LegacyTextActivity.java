package com.xlythe.hex.legacy;

import android.app.Activity;
import android.os.Build;
import android.util.SparseArray;
import android.util.Xml;

import com.xlythe.hex.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/** Android 2.1 cannot decode the UTF-8 string pools emitted by AAPT2. */
abstract class LegacyTextActivity extends Activity {
    private SparseArray<String> eclairStrings;

    protected String text(int id) {
        if (Build.VERSION.SDK_INT >= 8) return getString(id);
        if (eclairStrings == null) {
            eclairStrings = new SparseArray<>();
            try (InputStream input = getAssets().open("strings.xml")) {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(input, "UTF-8");
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType() == XmlPullParser.START_TAG && "string".equals(parser.getName())) {
                        String name = parser.getAttributeValue(null, "name");
                        eclairStrings.put(R.string.class.getField(name).getInt(null), parser.nextText());
                    }
                }
            } catch (IOException | XmlPullParserException | NoSuchFieldException | IllegalAccessException error) {
                throw new IllegalStateException("Could not load legacy string resources", error);
            }
        }
        return eclairStrings.get(id);
    }

    protected String text(int id, Object... args) {
        return String.format(Locale.getDefault(), text(id), args);
    }
}
