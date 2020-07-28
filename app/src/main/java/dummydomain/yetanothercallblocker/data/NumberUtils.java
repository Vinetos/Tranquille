package dummydomain.yetanothercallblocker.data;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class NumberUtils {

    private static final Set<String> HIDDEN_NUMBERS = new HashSet<>(Arrays.asList(
            "-1", "-2", "UNAVAILABLE", "ABSENT NUMBER", "NNN", "PRIVATE NUMBER"
    ));

    public static boolean isHiddenNumber(String number) {
        if (TextUtils.isEmpty(number) || TextUtils.getTrimmedLength(number) == 0) return true;
        return HIDDEN_NUMBERS.contains(number.toUpperCase(Locale.ENGLISH));
    }

}
