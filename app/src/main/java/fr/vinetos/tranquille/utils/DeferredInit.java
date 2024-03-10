package fr.vinetos.tranquille.utils;

import org.conscrypt.Conscrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;

public class DeferredInit {

    private static final Logger LOG = LoggerFactory.getLogger(DeferredInit.class);

    private static boolean networkInitialized;
    private static final Object NETWORK_INIT_LOCK = new Object();

    public static void initNetwork() {
        if (networkInitialized) return;

        synchronized (NETWORK_INIT_LOCK) {
            if (networkInitialized) return;

            try {
                Security.insertProviderAt(Conscrypt.newProvider(), 1);
            } catch (Throwable t) {
                LOG.warn("initNetwork()", t);
            }

            networkInitialized = true;
        }
    }

}
