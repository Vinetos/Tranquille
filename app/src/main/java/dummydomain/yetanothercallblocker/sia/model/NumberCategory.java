package dummydomain.yetanothercallblocker.sia.model;

import android.content.Context;
import androidx.annotation.StringRes;

import dummydomain.yetanothercallblocker.R;

public enum NumberCategory {

    NONE(0, R.string.sia_category_none),
    TELEMARKETER(1, R.string.sia_category_telemarketer),
    DEPT_COLLECTOR(2, R.string.sia_category_dept_collector),
    SILENT_CALL(3, R.string.sia_category_silent),
    NUISANCE_CALL(4, R.string.sia_category_nuisance),
    UNSOLICITED_CALL(5, R.string.sia_category_unsolicited),
    CALL_CENTER(6, R.string.sia_category_call_center),
    FAX_MACHINE(7, R.string.sia_category_fax),
    NON_PROFIT(8, R.string.sia_category_nonprofit),
    POLITICAL(9, R.string.sia_category_political),
    SCAM(10, R.string.sia_category_scam),
    PRANK(11, R.string.sia_category_prank),
    SMS(12, R.string.sia_category_sms),
    SURVEY(13, R.string.sia_category_survey),
    OTHER(14, R.string.sia_category_other),
    FINANCE_SERVICE(15, R.string.sia_category_financial_service),
    COMPANY(16, R.string.sia_category_company),
    SERVICE(17, R.string.sia_category_service),
    ROBOCALL(18, R.string.sia_category_robocall),
    // TODO: check: these are probably not present in the db
    SAFE_PERSONAL(100, R.string.sia_category_safe_personal),
    SAFE_COMPANY(101, R.string.sia_category_safe_company),
    SAFE_NONPROFIT(102, R.string.sia_category_safe_nonprofit);

    private int id;
    private @StringRes int stringId;

    NumberCategory(int id, int stringId) {
        this.id = id;
        this.stringId = stringId;
    }

    public int getId() {
        return id;
    }

    public @StringRes int getStringId() {
        return stringId;
    }

    public static NumberCategory getById(int id) {
        for (NumberCategory category : values()) {
            if (category.getId() == id) return category;
        }
        return null;
    }

    public static String getString(Context context, NumberCategory category) {
        @StringRes int stringId = category != null
                ? category.getStringId() : R.string.sia_category_none;

        return context.getString(stringId);
    }

}
