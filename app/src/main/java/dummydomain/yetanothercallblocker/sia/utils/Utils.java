package dummydomain.yetanothercallblocker.sia.utils;

import android.content.Context;
import android.util.Base64;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.ByteBuffer;
import java.util.UUID;

import dummydomain.yetanothercallblocker.App;
import dummydomain.yetanothercallblocker.sia.DatabaseSingleton;
import dummydomain.yetanothercallblocker.sia.SiaSettings;

public class Utils {

    public static Context getContext() {
        return App.getInstance();
    }

    public static SiaSettings getSettings() {
        return new SiaSettings(getContext());
    }

    public static String md5String(String str) {
        return new String(Hex.encodeHex(DigestUtils.md5(str)));
    }

    public static int getAppVersion() {
        return DatabaseSingleton.getCommunityDatabase().getSiaAppVersion();
    }

    public static int getEffectiveDbVersion() {
        return DatabaseSingleton.getCommunityDatabase().getEffectiveDbVersion();
    }

    public static String getAppId() {
        return "qQq0O9nCRNy_aVdPgU9WOA";
    }

    public static String generateAppId() {
        UUID randomUUID = UUID.randomUUID();

        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(randomUUID.getMostSignificantBits());
        buffer.putLong(randomUUID.getLeastSignificantBits());

        return Base64.encodeToString(buffer.array(), Base64.NO_PADDING)
                .replace('/', '_')
                .replace('\\', '-')
                .replaceAll("\n", "");
    }

}
