package dummydomain.yetanothercallblocker.sia.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dummydomain.yetanothercallblocker.sia.network.WebService;

public class CommunityReviewsLoader {

    private static final Logger LOG = LoggerFactory.getLogger(CommunityReviewsLoader.class);

    public static List<CommunityReview> loadReviews(String number) {
        LOG.debug("loadReviews({}) started", number);

        if (number.startsWith("+")) {
            number = number.substring(1);
        }

        Map<String, String> params = new HashMap<>();
        params.put("number", number);
        params.put("country", "RU"); // TODO: fix: hardcode

        WebService.WSResponse response = WebService.callForJson(WebService.getGetReviewsUrlPart(), params);

        List<CommunityReview> reviews = new ArrayList<>();

        if (response == null || !response.getSuccessful()) {
            LOG.warn("loadReviews() response is not successful");
            return reviews;
        }

        try {
            // response.getJsonObject():
            // "success": boolean
            // "nn": String number starting with "+"
            // "count": int number of items
            // "items": the following array

            JSONArray items = response.getJsonObject().getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);

                CommunityReview review = new CommunityReview();
                review.setId(item.getInt("id"));
                review.setRating(NumberRating.getById(item.getInt("rating")));
                review.setCategory(NumberCategory.getById(item.getInt("category_id")));
                review.setAuthor(item.getString("nick"));
                review.setTitle(item.getString("title"));
                review.setComment(item.getString("comment"));

                reviews.add(review);
            }

            LOG.trace("loadReviews() loaded {} reviews", reviews.size());
        } catch (JSONException e) {
            LOG.error("loadReviews()", e);
        }
        return reviews;
    }

}
