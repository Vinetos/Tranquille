package fr.vinetos.tranquille;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.Connection;
import android.telecom.GatewayInfo;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.vinetos.tranquille.data.NumberInfo;
import fr.vinetos.tranquille.data.NumberInfoService;
import fr.vinetos.tranquille.data.YacbHolder;
import fr.vinetos.tranquille.event.CallEndedEvent;

import static fr.vinetos.tranquille.EventUtils.postEvent;

@RequiresApi(Build.VERSION_CODES.N)
public class CallScreeningServiceImpl extends CallScreeningService {

    private static final Logger LOG = LoggerFactory.getLogger(CallScreeningServiceImpl.class);

    private NumberInfoService numberInfoService = YacbHolder.getNumberInfoService();

    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {
        LOG.info("onScreenCall({})", callDetails);

        boolean shouldBlock = false;
        NumberInfo numberInfo = null;

        try {
            boolean ignore = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (callDetails.getCallDirection() != Call.Details.DIRECTION_INCOMING) {
                    ignore = true;
                }
            }

            if (!ignore && !App.getSettings().getCallBlockingEnabled()) {
                ignore = true;
            }

            extraLogging(callDetails); // TODO: make optional or remove

            String number = null;

            if (!ignore) {
                Uri handle = callDetails.getHandle();
                LOG.trace("onScreenCall() handle: {}", handle);

                if (handle != null && PhoneAccount.SCHEME_TEL.equals(handle.getScheme())) {
                    number = handle.getSchemeSpecificPart();
                    LOG.debug("onScreenCall() number from handle: {}", number);
                }

                if (number == null) {
                    Bundle intentExtras = callDetails.getIntentExtras();
                    if (intentExtras != null) {
                        Object o = intentExtras.get(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS);
                        LOG.trace("onScreenCall() EXTRA_INCOMING_CALL_ADDRESS={}", o);

                        if (o instanceof Uri) {
                            Uri uri = (Uri) o;
                            if (PhoneAccount.SCHEME_TEL.equals(uri.getScheme())) {
                                number = uri.getSchemeSpecificPart();
                            }
                        }

                        if (number == null && intentExtras.containsKey(
                                "com.google.android.apps.hangouts.telephony.hangout_info_bundle")) {
                            // NB: SIA doesn't block (based on number) hangouts if there's no number in intentExtras
                            number = "YACB_hangouts_stub";
                        }
                    }

                    if (number == null && callDetails.getExtras() != null) {
                        // NB: this part is broken in SIA
                        number = callDetails.getExtras().getString(Connection.EXTRA_CHILD_ADDRESS);
                        LOG.trace("onScreenCall() EXTRA_CHILD_ADDRESS={}", number);
                    }
                }

                if (TextUtils.isEmpty(number)
                        && !PermissionHelper.hasNumberInfoPermissions(this)) {
                    ignore = true;
                    LOG.warn("onScreenCall() no info permissions");
                }
            }

            if (!ignore) {
                numberInfo = numberInfoService.getNumberInfo(number,
                        App.getSettings().getCachedAutoDetectedCountryCode(), false);

                shouldBlock = numberInfoService.shouldBlock(numberInfo);
            }
        } finally {
            LOG.debug("onScreenCall() blocking call: {}", shouldBlock);

            CallScreeningService.CallResponse.Builder responseBuilder = new CallResponse.Builder();

            if (shouldBlock) {
                responseBuilder
                        .setDisallowCall(true)
                        .setRejectCall(true)
                        .setSkipNotification(true);
            }

            boolean blocked = shouldBlock;
            try {
                respondToCall(callDetails, responseBuilder.build());
            } catch (Exception e) {
                LOG.error("onScreenCall() error invoking respondToCall()", e);
                blocked = false;
            }

            if (blocked) {
                LOG.info("onScreenCall() blocked call");

                NotificationHelper.showBlockedCallNotification(this, numberInfo);

                numberInfoService.blockedCall(numberInfo);

                postEvent(new CallEndedEvent());
            }
        }

        LOG.debug("onScreenCall() finished");
    }

    private void extraLogging(Call.Details callDetails) {
        LOG.trace("extraLogging() handle={}", callDetails.getHandle());

        if (callDetails.getStatusHints() != null) {
            LOG.trace("extraLogging() statusHints.label={}",
                    callDetails.getStatusHints().getLabel());
        }

        GatewayInfo gatewayInfo = callDetails.getGatewayInfo();
        if (gatewayInfo != null) {
            LOG.trace("extraLogging() gatewayInfo provider={}," +
                            "gatewayAddress={}, originalAddress={}",
                    gatewayInfo.getGatewayProviderPackageName(),
                    gatewayInfo.getGatewayAddress(),
                    gatewayInfo.getOriginalAddress());
        }

        Bundle intentExtras = callDetails.getIntentExtras();
        if (intentExtras != null) {
            LOG.trace("extraLogging() intentExtras:");
            for (String k : intentExtras.keySet()) {
                LOG.trace("extraLogging() key={}, value={}", k, intentExtras.get(k));
            }
        }

        Bundle extras = callDetails.getExtras();
        if (intentExtras != null) {
            LOG.trace("extraLogging() intentExtras:");
            for (String k : extras.keySet()) {
                LOG.trace("extraLogging() key={}, value={}", k, extras.get(k));
            }
        }
    }

}
