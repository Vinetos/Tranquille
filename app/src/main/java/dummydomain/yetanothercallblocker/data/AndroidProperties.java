package dummydomain.yetanothercallblocker.data;

import android.content.Context;

import dummydomain.yetanothercallblocker.GenericSettings;
import dummydomain.yetanothercallblocker.sia.Properties;

public class AndroidProperties extends GenericSettings implements Properties {

    public AndroidProperties(Context context, String name) {
        super(context, name);
    }

}
