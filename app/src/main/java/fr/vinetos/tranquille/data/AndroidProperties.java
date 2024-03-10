package fr.vinetos.tranquille.data;

import android.content.Context;

import fr.vinetos.tranquille.GenericSettings;
import dummydomain.yetanothercallblocker.sia.Properties;

public class AndroidProperties extends GenericSettings implements Properties {

    public AndroidProperties(Context context, String name) {
        super(context, name);
    }

}
