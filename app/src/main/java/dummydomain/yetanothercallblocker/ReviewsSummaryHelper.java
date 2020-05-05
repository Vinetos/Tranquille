package dummydomain.yetanothercallblocker;

import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;

public class ReviewsSummaryHelper {

    public static void populateSummary(View reviewsSummary, CommunityDatabaseItem item) {
        reviewsSummary.setVisibility(View.VISIBLE);

        Map<Integer, Integer> map = new HashMap<>(3);
        map.put(R.id.summary_text_negative, item != null ? item.getNegativeRatingsCount() : 0);
        map.put(R.id.summary_text_neutral, item != null ? item.getNeutralRatingsCount() : 0);
        map.put(R.id.summary_text_positive, item != null ? item.getPositiveRatingsCount() : 0);
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            ((TextView) reviewsSummary.findViewById(e.getKey())).setText(
                    String.valueOf(e.getValue()));
        }
    }

}
