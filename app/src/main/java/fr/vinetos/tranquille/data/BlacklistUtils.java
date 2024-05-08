package fr.vinetos.tranquille.data;

import java.util.regex.Pattern;

public class BlacklistUtils {

    private static final Pattern BLACKLIST_ITEM_VALID_PATTERN = Pattern.compile("\\+?[0-9%_]+");
    private static final Pattern PATTERN_CLEANING_PATTERN = Pattern.compile("[^+0-9%_*#]");
    private static final Pattern NUMBER_CLEANING_PATTERN = Pattern.compile("[^+0-9]");

    public static String cleanPattern(String pattern) {
        return PATTERN_CLEANING_PATTERN.matcher(pattern).replaceAll("");
    }

    public static String cleanNumber(String number) {
        return NUMBER_CLEANING_PATTERN.matcher(number).replaceAll("");
    }

    public static boolean isValidPattern(String pattern) {
        return BLACKLIST_ITEM_VALID_PATTERN.matcher(pattern).matches();
    }

}
