package dummydomain.yetanothercallblocker;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.util.List;

import dummydomain.yetanothercallblocker.sia.model.CommunityReview;
import dummydomain.yetanothercallblocker.sia.model.CommunityReviewsLoader;

public class ReviewsActivity extends AppCompatActivity {

    private static final String PARAM_NUMBER = "param_number";

    private CustomListViewAdapter listViewAdapter;
    private RecyclerView reviewsList;

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

        setText(getString(R.string.reviews_loading, paramNumber));

        listViewAdapter = new CustomListViewAdapter();
        reviewsList = findViewById(R.id.reviews_list);
        reviewsList.setLayoutManager(new LinearLayoutManager(this));
        reviewsList.setAdapter(listViewAdapter);
        reviewsList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        new AsyncTask<Void, Void, List<CommunityReview>>() {
            @Override
            protected List<CommunityReview> doInBackground(Void... voids) {
                return CommunityReviewsLoader.loadReviews(paramNumber);
            }

            @Override
            protected void onPostExecute(List<CommunityReview> reviews) {
                setText(paramNumber);
                handleReviews(reviews);
            }
        }.execute();
    }

    private void setText(String text) {
        this.<TextView>findViewById(R.id.text_view).setText(text);
    }

    private void handleReviews(List<CommunityReview> reviews) {
        listViewAdapter.setItems(reviews);
        listViewAdapter.notifyDataSetChanged();
    }

}
