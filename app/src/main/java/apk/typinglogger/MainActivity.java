package apk.typinglogger;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private Spinner dropDown;
    private String currentFile = "";
    private String filesPath;
    private final List<String> fileList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        filesPath = getFilesDir().getAbsolutePath();

        dropDown = findViewById(R.id.dropdown);
        textView = findViewById(R.id.textview);
        TextView tvadv1 = findViewById(R.id.adv1);
        TextView tvadv2 = findViewById(R.id.adv2);
        LinearLayout advLayout = findViewById(R.id.belolayout);

        dropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFile = fileList.get(position);
                loadText(currentFile);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentFile = "";
                textView.setText("");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFiles();
        checkAccessibilityState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.btn, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.btn, menu);
        menu.findItem(R.id.action_delete).setVisible(!currentFile.isEmpty());
        menu.findItem(R.id.action_copy).setVisible(!currentFile.isEmpty());
        menu.findItem(R.id.action_share).setVisible(!currentFile.isEmpty());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int ItemID = item.getItemId();
        if (ItemID == R.id.action_delete) {
            final AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setMessage(getString(R.string.ask_delete));
            dlg.setCancelable(true);
            dlg.setPositiveButton(getString(R.string.ok), (dialog, id) -> {
                if (!currentFile.isEmpty()) {
                    final File file = new File(filesPath, currentFile);
                    if (!file.delete()) {
                        Toast.makeText(this, getString(R.string.not_deleted), Toast.LENGTH_LONG).show();
                    } else {
                        if (SvcAcc.instance != null) {
                            if (SvcAcc.instance.fileName.equals(currentFile)) {
                                SvcAcc.instance.clearText();
                            }
                        }
                        loadFiles();
                    }
                }
            });
            dlg.setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.dismiss());
            dlg.show();
        } else if (ItemID == R.id.action_copy) {
            ClipboardManager clipboard = (ClipboardManager) this.getApplicationContext().getSystemService(AppCompatActivity.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText(getString(R.string.content_copied), textView.getText());
                clipboard.setPrimaryClip(clip);
            }
        } else if (ItemID == R.id.action_share) {
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT,getString(R.string.content_shared));
            intent.putExtra(Intent.EXTRA_TEXT, textView.getText());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.share)));
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAccessibilityState() {
        if (!SvcAcc.isEnabled) {
            final AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
            dlg.setTitle(getString(R.string.disclosure));
            dlg.setCancelable(false);
            dlg.setMessage(getString(R.string.using_accessibility) + "\n\n" + getString(R.string.purpose));
            dlg.setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.dismiss());
            dlg.setPositiveButton(getString(R.string.accept), (dialog, id) -> {
                Intent intent = new Intent(MainActivity.this, AccessibilityActivity.class);
                this.startActivity(intent);
            });
            dlg.show();
        }
    }

    private Date getDate(String s) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        try {
            return format.parse(s);
        } catch (Exception e) {
            return new Date(0);
        }
    }

    private void loadFiles() {
        String selectedFile;
        if (dropDown.getSelectedItem() != null) {
            selectedFile = fileList.get(dropDown.getSelectedItemPosition());
        } else {
            selectedFile = "";
        }

        fileList.clear();
        List<String> nameList = new ArrayList<>();

        File directory = new File(filesPath);
        File[] files = directory.listFiles();

        if (files != null) {
            Arrays.sort(files, Comparator.reverseOrder());
            for (File file : files) {
                String s = file.getName();
                Date date = getDate(s);
                if (date.getTime() > 0) {
                    fileList.add(s);
                    nameList.add(DateFormat.getDateInstance(DateFormat.MEDIUM).format(date));
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nameList);
        dropDown.setAdapter(adapter);

        if (fileList.size() > 0) {
            int i = fileList.indexOf(selectedFile);
            if (i < 1) {
                i = 0;
                currentFile = fileList.get(i);
            }
            dropDown.setSelection(i,false);
        } else {
            currentFile = "";
            textView.setText("");
        }

        invalidateOptionsMenu();
    }

    private void loadText(String file) {
        File textFile = new File(filesPath, file);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(textFile));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }
            br.close();

            if (SvcAcc.instance != null) {
                if (SvcAcc.instance.fileName.equals(file)) {
                    text.append(SvcAcc.instance.getCurrentText());
                }
            }
            textView.setText(text);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
