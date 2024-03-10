package fr.vinetos.tranquille.data;

import android.provider.CallLog;

public class CallLogItem {

    public enum Type {
        INCOMING, OUTGOING, MISSED, REJECTED, OTHER;

        public static Type fromProviderType(int type) {
            switch (type) {
                case CallLog.Calls.INCOMING_TYPE: return INCOMING;
                case CallLog.Calls.OUTGOING_TYPE: return OUTGOING;
                case CallLog.Calls.MISSED_TYPE:
                case CallLog.Calls.VOICEMAIL_TYPE:
                    return MISSED;
                case CallLog.Calls.REJECTED_TYPE:
                case CallLog.Calls.BLOCKED_TYPE:
                    return REJECTED;
                default: return OTHER;
            }
        }
    }

    public long id;
    public Type type;
    public String number;
    public long timestamp;
    public long duration;
    public NumberInfo numberInfo;

    public CallLogItem(long id, Type type, String number, long timestamp, long duration) {
        this.id = id;
        this.type = type;
        this.number = number;
        this.timestamp = timestamp;
        this.duration = duration;
    }
}
