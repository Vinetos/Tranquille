package fr.vinetos.tranquille;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import fr.vinetos.tranquille.data.NumberInfo;
import fr.vinetos.tranquille.data.YacbHolder;

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

        NumberInfo numberInfo = YacbHolder.getNumberInfo(getIntent().getStringExtra(PARAM_NUMBER),
                App.getSettings().getCachedAutoDetectedCountryCode());

        InfoDialogHelper.showDialog(this, numberInfo, (d) -> finish());
    }

}
