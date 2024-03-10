package fr.vinetos.tranquille.data;

import java.util.List;

public class NumberFilter
        implements dummydomain.yetanothercallblocker.sia.model.database.NumberFilter {

    private final String[] prefixesToKeep;
    private final int keepLength;
    private final boolean detailed;

    public NumberFilter(List<String> prefixesToKeep, boolean detailed, int keepLength) {
        this.prefixesToKeep = prefixesToKeep.toArray(new String[0]);
        this.detailed = detailed;
        this.keepLength = keepLength;
    }

    @Override
    public boolean keepPrefix(String prefix) {
        for (String prefixToKeep : prefixesToKeep) {
            if (prefixMatch(prefix, prefixToKeep)) return true;
        }
        return false;
    }

    @Override
    public boolean keepNumber(String number) {
        if (detailed && keepLength > 0 && number.length() <= keepLength) return true;

        for (String prefixToKeep : prefixesToKeep) {
            if (number.startsWith(prefixToKeep)) return true;
        }

        return false;
    }

    @Override
    public boolean isDetailed() {
        return detailed;
    }

    private boolean prefixMatch(String prefix, String keepPrefix) {
        if (prefix.length() < keepPrefix.length()) {
            return keepPrefix.startsWith(prefix);
        } else {
            return prefix.startsWith(keepPrefix);
        }
    }

}
