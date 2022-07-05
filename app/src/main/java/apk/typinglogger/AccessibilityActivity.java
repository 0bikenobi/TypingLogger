package apk.typinglogger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AccessibilityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SvcAcc.isEnabled) {
            SvcAcc.isActivating = false;
            Toast.makeText(this, getString(R.string.type_something), Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }

        setContentView(R.layout.activity_accessibility);

        findViewById(R.id.btn501925).setOnClickListener(v -> {
            try {
                SvcAcc.isActivating = true;
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.btn751915).setOnClickListener(v -> this.finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SvcAcc.isEnabled) {
            SvcAcc.isActivating = false;
            Toast.makeText(this, getString(R.string.type_something), Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SvcAcc.isActivating = false;
    }
}