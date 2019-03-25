package dummydomain.yetanothercallblocker;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.List;

import dummydomain.yetanothercallblocker.sia.model.CommunityReview;
import dummydomain.yetanothercallblocker.sia.model.CommunityReviewsLoader;
import dummydomain.yetanothercallblocker.sia.model.NumberCategory;

public class ReviewsActivity extends AppCompatActivity {

    private static final String PARAM_NUMBER = "param_number";

    public static Intent getNumberIntent(Context context, String number) {
        Intent intent = new Intent(context, ReviewsActivity.class);
        intent.putExtra(PARAM_NUMBER, number);
        return intent;
    }

    public static void startForNumber(Context context, String number) {
        context.startActivity(getNumberIntent(context, number));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        setSupportActionBar(findViewById(R.id.toolbar));

        final String paramNumber = getIntent().getStringExtra(PARAM_NUMBER);

        setText("Loading");

        new AsyncTask<Void, Void, List<CommunityReview>>() {
            @Override
            protected List<CommunityReview> doInBackground(Void... voids) {
                return CommunityReviewsLoader.loadReviews(paramNumber);
            }

            @Override
            protected void onPostExecute(List<CommunityReview> reviews) {
                setText(reviewsToString(ReviewsActivity.this, reviews));
            }
        }.execute();
    }

    private void setText(String mainPart) {
        TextView textView = findViewById(R.id.text_view);
        textView.setText(getHeader() + "\n\n" + mainPart);
    }

    private String getHeader() {
        return "Reviews for " + getIntent().getStringExtra(PARAM_NUMBER);
    }

    private static String reviewsToString(Context context, List<CommunityReview> reviews) {
        StringBuilder sb = new StringBuilder();

        for (CommunityReview review : reviews) {
            sb.append(review.getAuthor()).append('\n');
            sb.append(review.getRating()).append('\n');
            sb.append(NumberCategory.getString(context, review.getCategory())).append('\n');
            sb.append(review.getTitle()).append('\n');
            sb.append(review.getComment()).append('\n');
            sb.append('\n');
        }

        return sb.toString();
    }

}
