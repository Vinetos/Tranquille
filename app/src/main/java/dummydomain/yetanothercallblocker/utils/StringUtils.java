package dummydomain.yetanothercallblocker.utils;

public class StringUtils {

    public static String quote(String s) {
        return s == null ? null : '"' + s + '"';
    }

}
