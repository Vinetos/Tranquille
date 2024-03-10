package fr.vinetos.tranquille.data;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CallLogItemGroup {

    private List<CallLogItem> items = new LinkedList<>();

    public CallLogItemGroup() {}

    public List<CallLogItem> getItems() {
        return items;
    }

    public static List<CallLogItemGroup> noGrouping(Iterable<CallLogItem> items) {
        List<CallLogItemGroup> groups = new ArrayList<>();

        for (CallLogItem item : items) {
            CallLogItemGroup group = new CallLogItemGroup();
            group.getItems().add(item);
            groups.add(group);
        }

        return groups;
    }

    public static List<CallLogItemGroup> groupConsecutive(Iterable<CallLogItem> items) {
        List<CallLogItemGroup> groups = new ArrayList<>();

        CallLogItem currentItem = null;
        CallLogItemGroup currentGroup = null;

        for (CallLogItem item : items) {
            if (currentItem == null || !TextUtils.equals(item.number, currentItem.number)) {
                currentGroup = new CallLogItemGroup();
                groups.add(currentGroup);

                currentItem = item;
            }
            currentGroup.getItems().add(item);
        }

        return groups;
    }

    public static List<CallLogItemGroup> groupInDay(Iterable<CallLogItem> items) {
        List<CallLogItemGroup> groups = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        long dayStartTimestamp = -1;

        Map<String, CallLogItemGroup> groupMap = new HashMap<>();

        for (CallLogItem item : items) {
            if (dayStartTimestamp == -1 || item.timestamp - dayStartTimestamp < 0) {
                calendar.setTimeInMillis(item.timestamp);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                dayStartTimestamp = calendar.getTimeInMillis();

                groupMap.clear();
            }

            CallLogItemGroup group = groupMap.get(item.number);
            if (group == null) {
                group = new CallLogItemGroup();
                groupMap.put(item.number, group);
                groups.add(group);
            }
            group.getItems().add(item);
        }

        return groups;
    }

}
