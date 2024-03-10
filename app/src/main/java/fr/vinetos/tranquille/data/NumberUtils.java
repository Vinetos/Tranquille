package fr.vinetos.tranquille.data;

import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class NumberUtils {

    private static final Set<String> HIDDEN_NUMBERS = new HashSet<>(Arrays.asList(
            "-1", "-2", "UNAVAILABLE", "ABSENT NUMBER", "NNN", "PRIVATE NUMBER", "ANONYMOUS"
    ));

    public static boolean isHiddenNumber(String number) {
        if (TextUtils.isEmpty(number) || TextUtils.getTrimmedLength(number) == 0) return true;
        return HIDDEN_NUMBERS.contains(number.toUpperCase(Locale.ENGLISH));
    }

    public static String normalizeNumber(String number, String countryCode) {
        String normalizedNumber = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // TODO: Android 4.* support
            normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, countryCode);
        }

        if (normalizedNumber == null) {
            normalizedNumber = PhoneNumberUtils.stripSeparators(number);
        }

        return normalizedNumber;
    }

}
