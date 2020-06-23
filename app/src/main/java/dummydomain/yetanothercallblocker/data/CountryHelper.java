package dummydomain.yetanothercallblocker.data;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.core.os.ConfigurationCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountryHelper {

    private static final Logger LOG = LoggerFactory.getLogger(CountryHelper.class);

    public static String detectCountry(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            if (tm != null) {
                String countryCode = tm.getNetworkCountryIso();
                if (!TextUtils.isEmpty(countryCode)) return countryCode.toUpperCase();

                countryCode = tm.getSimCountryIso();
                if (!TextUtils.isEmpty(countryCode)) return countryCode.toUpperCase();
            }

            String countryCode = ConfigurationCompat
                    .getLocales(context.getResources().getConfiguration())
                    .get(0).getCountry();
            if (countryCode.length() == 2) return countryCode;
        } catch (Exception e) {
            LOG.warn("detectCountry()", e);
        }

        return null;
    }

}
