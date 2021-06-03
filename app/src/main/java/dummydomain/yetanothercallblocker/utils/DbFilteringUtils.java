package dummydomain.yetanothercallblocker.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dummydomain.yetanothercallblocker.Settings;
import dummydomain.yetanothercallblocker.data.NumberFilter;

public class DbFilteringUtils {

    public static NumberFilter getNumberFilter(Settings settings) {
        if (!settings.isDbFilteringEnabled()) return null;

        return new NumberFilter(getPrefixesToKeep(settings),
                settings.isDbFilteringThorough(),
                settings.getDbFilteringKeepShortNumbers()
                        ? settings.getDbFilteringKeepShortNumbersMaxLength() : 0);
    }

    public static List<String> getPrefixesToKeep(Settings settings) {
        return parsePrefixes(settings.getDbFilteringPrefixesToKeep());
    }

    public static List<String> parsePrefixes(String prefixesString) {
        if (TextUtils.isEmpty(prefixesString)) return Collections.emptyList();

        List<String> prefixList = new ArrayList<>();

        for (String prefix : prefixesString.split("[,;]")) {
            prefix = prefix.replaceAll("[^0-9]", "");
            if (!prefix.isEmpty() && !prefixList.contains(prefix)) prefixList.add(prefix);
        }

        return prefixList;
    }

}
