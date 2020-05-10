package dummydomain.yetanothercallblocker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dummydomain.yetanothercallblocker.sia.model.CommunityReview;
import dummydomain.yetanothercallblocker.sia.model.CommunityReviewsLoader;

public class ReviewsActivity extends AppCompatActivity {

    private static final String PARAM_NUMBER = "param_number";

    private CustomListViewAdapter listViewAdapter;

    private AsyncTask<Void, Void, List<CommunityReview>> loadReviewsTask;

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

        findViewById(R.id.reviews_summary).setVisibility(View.GONE);
        setTitle(paramNumber);
        setText(getString(R.string.reviews_loading));

        listViewAdapter = new CustomListViewAdapter();
        RecyclerView reviewsList = findViewById(R.id.reviews_list);
        reviewsList.setLayoutManager(new LinearLayoutManager(this));
        reviewsList.setAdapter(listViewAdapter);
        reviewsList.addItemDecoration(new CustomVerticalDivider(this));

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, List<CommunityReview>> asyncTask = loadReviewsTask
                = new AsyncTask<Void, Void, List<CommunityReview>>() {
            @Override
            protected List<CommunityReview> doInBackground(Void... voids) {
                return CommunityReviewsLoader.loadReviews(paramNumber);
            }

            @Override
            protected void onPostExecute(List<CommunityReview> reviews) {
                setText("");
                handleReviews(reviews);
            }
        };
        asyncTask.execute();
    }

    @Override
    protected void onDestroy() {
        cancelReviewsLoadingTask();

        super.onDestroy();
    }

    private void cancelReviewsLoadingTask() {
        if (loadReviewsTask != null) {
            loadReviewsTask.cancel(true);
            loadReviewsTask = null;
        }
    }

    private void setText(String text) {
        TextView textView = this.findViewById(R.id.text_view);
        textView.setText(text);
        textView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }

    private void handleReviews(List<CommunityReview> reviews) {
        listViewAdapter.setItems(reviews);
        listViewAdapter.notifyDataSetChanged();
        displaySummary(reviews);
    }

    private void displaySummary(List<CommunityReview> reviews) {
        int[] ratings = {0, 0, 0};
        int[] resIds = {
                R.id.summary_text_negative,
                R.id.summary_text_neutral,
                R.id.summary_text_positive
        };
        for (CommunityReview review : reviews) {
            switch (review.getRating()) {
                case NEGATIVE:
                    ratings[0]++;
                    break;
                case NEUTRAL:
                    ratings[1]++;
                    break;
                case POSITIVE:
                    ratings[2]++;
                    break;
            }
        }
        View summary = findViewById(R.id.reviews_summary);
        summary.setVisibility(View.VISIBLE);
        for (int i = 0; i < resIds.length; i++) {
            ((TextView) summary.findViewById(resIds[i])).setText(String.valueOf(ratings[i]));
        }
    }

}
