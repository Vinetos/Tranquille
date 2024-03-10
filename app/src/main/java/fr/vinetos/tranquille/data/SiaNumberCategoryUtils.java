package fr.vinetos.tranquille.data;

import android.content.Context;

import androidx.annotation.StringRes;

import fr.vinetos.tranquille.R;
import dummydomain.yetanothercallblocker.sia.model.NumberCategory;

public class SiaNumberCategoryUtils {

    public static String getName(Context context, NumberCategory category) {
        return context.getString(getNameResId(category));
    }

    @StringRes
    public static int getNameResId(NumberCategory category) {
        if (category == null) {
            return R.string.sia_category_none;
        }

        switch (category) {
            case NONE: return R.string.sia_category_none;
            case TELEMARKETER: return R.string.sia_category_telemarketer;
            case DEPT_COLLECTOR: return R.string.sia_category_dept_collector;
            case SILENT_CALL: return R.string.sia_category_silent;
            case NUISANCE_CALL: return R.string.sia_category_nuisance;
            case UNSOLICITED_CALL: return R.string.sia_category_unsolicited;
            case CALL_CENTER: return R.string.sia_category_call_center;
            case FAX_MACHINE: return R.string.sia_category_fax;
            case NON_PROFIT: return R.string.sia_category_nonprofit;
            case POLITICAL: return R.string.sia_category_political;
            case SCAM: return R.string.sia_category_scam;
            case PRANK: return R.string.sia_category_prank;
            case SMS: return R.string.sia_category_sms;
            case SURVEY: return R.string.sia_category_survey;
            case OTHER: return R.string.sia_category_other;
            case FINANCE_SERVICE: return R.string.sia_category_financial_service;
            case COMPANY: return R.string.sia_category_company;
            case SERVICE: return R.string.sia_category_service;
            case ROBOCALL: return R.string.sia_category_robocall;
            // TODO: check: these are probably not present in the db
            case SAFE_PERSONAL: return R.string.sia_category_safe_personal;
            case SAFE_COMPANY: return R.string.sia_category_safe_company;
            case SAFE_NONPROFIT: return R.string.sia_category_safe_nonprofit;
            default: throw new RuntimeException("Category not implemented: " + category);
        }
    }

}
