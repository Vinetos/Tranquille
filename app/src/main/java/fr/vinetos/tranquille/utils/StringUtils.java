package fr.vinetos.tranquille.utils;

public class StringUtils {

    public static String quote(String s) {
        return s == null ? null : '"' + s + '"';
    }

}
