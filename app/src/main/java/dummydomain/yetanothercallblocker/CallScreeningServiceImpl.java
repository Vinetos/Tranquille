package dummydomain.yetanothercallblocker;

import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.PhoneAccount;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dummydomain.yetanothercallblocker.data.DatabaseSingleton;
import dummydomain.yetanothercallblocker.data.NumberInfo;
import dummydomain.yetanothercallblocker.event.CallEndedEvent;

import static dummydomain.yetanothercallblocker.EventUtils.postEvent;

@RequiresApi(Build.VERSION_CODES.N)
public class CallScreeningServiceImpl extends CallScreeningService {

    private static final Logger LOG = LoggerFactory.getLogger(CallScreeningServiceImpl.class);

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

            if (!ignore && !App.getSettings().getBlockCalls()) {
                ignore = true;
            }

            if (!ignore) {
                Uri handle = callDetails.getHandle();
                if (PhoneAccount.SCHEME_TEL.equals(handle.getScheme())) {
                    String number = handle.getSchemeSpecificPart();
                    LOG.debug("onScreenCall() number={}", number);

                    numberInfo = DatabaseSingleton.getNumberInfo(number);

                    if (numberInfo.rating == NumberInfo.Rating.NEGATIVE
                            && numberInfo.contactItem == null) {
                        shouldBlock = true;
                    }
                }
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

                postEvent(new CallEndedEvent());
            }
        }
    }

}
