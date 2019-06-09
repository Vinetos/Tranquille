package dummydomain.yetanothercallblocker;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private static final String PREF_BLOCK_CALLS = "blockCalls";

    private final SharedPreferences pref;

    public Settings(Context context) {
        pref = context.getSharedPreferences("yacb_preferences", Context.MODE_PRIVATE);
    }

    public boolean getBlockCalls() {
        return pref.getBoolean(PREF_BLOCK_CALLS, false);
    }

    public void setBlockCalls(boolean block) {
        pref.edit().putBoolean(PREF_BLOCK_CALLS, block).apply();
    }

}
