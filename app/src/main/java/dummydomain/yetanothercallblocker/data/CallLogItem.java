package dummydomain.yetanothercallblocker.data;

import android.provider.CallLog;

import java.util.Objects;

public class CallLogItem {

    public enum Type {
        INCOMING, OUTGOING, MISSED, REJECTED, OTHER;

        public static Type fromProviderType(int type) {
            switch (type) {
                case CallLog.Calls.INCOMING_TYPE: return INCOMING;
                case CallLog.Calls.OUTGOING_TYPE: return OUTGOING;
                case CallLog.Calls.MISSED_TYPE: return MISSED;
                case CallLog.Calls.REJECTED_TYPE: return REJECTED;
                default: return OTHER;
            }
        }
    }

    public Type type;
    public String number;
    public long timestamp;
    public long duration;
    public NumberInfo numberInfo;

    public CallLogItem(Type type, String number, long timestamp, long duration) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(number);
        this.type = type;
        this.number = number;
        this.timestamp = timestamp;
        this.duration = duration;
    }
}
