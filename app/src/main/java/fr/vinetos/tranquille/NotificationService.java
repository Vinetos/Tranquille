package fr.vinetos.tranquille;

import android.content.Context;

import fr.vinetos.tranquille.data.NumberInfo;

public class NotificationService {

    private final Context context;

    public NotificationService(Context context) {
        this.context = context;
    }

    public void startCallIndication(NumberInfo numberInfo) {
        NotificationHelper.showIncomingCallNotification(context, numberInfo);
    }

    public void stopAllCallsIndication() {
        NotificationHelper.hideIncomingCallNotification(context);
    }

    public void notifyCallBlocked(NumberInfo numberInfo) {
        NotificationHelper.showBlockedCallNotification(context, numberInfo);
    }

}
