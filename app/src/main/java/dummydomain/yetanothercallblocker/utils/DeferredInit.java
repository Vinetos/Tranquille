package dummydomain.yetanothercallblocker.utils;

import org.conscrypt.Conscrypt;

import java.security.Security;

public class DeferredInit {

    private static boolean networkInitialized;
    private static final Object NETWORK_INIT_LOCK = new Object();

    public static void initNetwork() {
        if (networkInitialized) return;

        synchronized (NETWORK_INIT_LOCK) {
            if (networkInitialized) return;

            Security.insertProviderAt(Conscrypt.newProvider(), 1);

            networkInitialized = true;
        }
    }

}
