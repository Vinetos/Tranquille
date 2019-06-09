package dummydomain.yetanothercallblocker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SwitchCompat notificationsSwitch = findViewById(R.id.notificationsEnabledSwitch);
        notificationsSwitch.setChecked(CallReceiver.isEnabled(this));
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked)
                -> CallReceiver.setEnabled(MainActivity.this, isChecked));

        SwitchCompat autoUpdateSwitch = findViewById(R.id.autoUpdateEnabledSwitch);
        autoUpdateSwitch.setChecked(Updater.isAutoUpdateScheduled());
        autoUpdateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) Updater.scheduleAutoUpdateWorker();
            else Updater.cancelAutoUpdateWorker();
        });

        PermissionHelper.checkPermissions(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // TODO: handle
    }

    public void onOpenDebugActivity(MenuItem item) {
        startActivity(new Intent(this, DebugActivity.class));
    }

}
