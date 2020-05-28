package dummydomain.yetanothercallblocker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import dummydomain.yetanothercallblocker.data.DatabaseSingleton;
import dummydomain.yetanothercallblocker.data.NumberInfo;

public class InfoDialogActivity extends AppCompatActivity {

    public static final String PARAM_NUMBER = "number";

    public static Intent getIntent(Context context, String number) {
        Intent intent = new Intent(context, InfoDialogActivity.class);
        intent.putExtra(PARAM_NUMBER, number);
        intent.setData(IntentHelper.getUriForPhoneNumber(number));
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NumberInfo numberInfo = DatabaseSingleton.getNumberInfo(
                getIntent().getStringExtra(PARAM_NUMBER));

        InfoDialogHelper.showDialog(this, numberInfo, (d) -> finish());
    }

}
