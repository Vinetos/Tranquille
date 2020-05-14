package dummydomain.yetanothercallblocker.sia.utils;

import android.util.Base64;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Utils {

    public static String md5String(String str) {
        return new String(Hex.encodeHex(DigestUtils.md5(str)));
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
