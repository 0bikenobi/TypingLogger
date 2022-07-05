package apk.typinglogger;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SvcAcc extends AccessibilityService {
    static SvcAcc instance = null;
    static boolean isEnabled = false;
    static boolean isActivating = false;
    public String fileName = "";
    private String recordedText = "";
    private String recordedTime = "";
    private String recordedPackage = "";
    private String filesPath;

    @Override
    public void onCreate() {
        super.onCreate();
        filesPath = getFilesDir().getAbsolutePath();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        isEnabled = true;
        instance = this;
        setNotification();
        if (isActivating) {
            isActivating = false;
            Intent intent = new Intent(this, AccessibilityActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public boolean onUnbind(@NonNull Intent intent) {
        isEnabled = false;
        instance = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(@NonNull Intent intent) {
        super.onRebind(intent);
        isEnabled = true;
        instance = this;
    }

    @Override
    public void onInterrupt() {
        //
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isEnabled = false;
        instance = null;
        removeNotification();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        try {
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                String date = getDate();
                if ((!date.equals(fileName)) && (!fileName.isEmpty())) {
                    writeFile();
                    fileName = "";
                    recordedTime = "";
                    recordedText = "";
                }
            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                // comming soon
            }
        } catch (Exception e) {
            Log.e(BuildConfig.APPLICATION_ID, e.getMessage());
        }
    }

    private void setNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel Channel = new NotificationChannel(BuildConfig.APPLICATION_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
        Channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        notificationManager.createNotificationChannel(Channel);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), BuildConfig.APPLICATION_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(getString(R.string.app_name))
                .setCategory(Notification.CATEGORY_SERVICE)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        Random random = new Random();
        int serviceID = random.nextInt(99999 - 10000) + 1000;
        notificationManager.cancelAll();
        notificationManager.notify(serviceID, notification);
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public void clearText() {
        fileName = "";
        recordedTime = "";
        recordedText = "";
    }

    public String getCurrentText() {
        if (!recordedText.isEmpty()) {
            String title = recordedTime + " " + recordedPackage;
            String line = String.format("%" + title.length() + "s", "").replace(' ', '-');
            return line + "\n" + title + "\n" + line + "\n" + recordedText + "\n\n";
        } else {
            return "";
        }
    }

    private void createFile() {
        File file = new File(filesPath, fileName);
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new Exception();
                }
            }
        } catch (Exception e) {
            //
        }
    }

    private void writeFile() {
        try {
            File file = new File(filesPath, fileName);
            FileOutputStream fOut = new FileOutputStream(file,true);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            String title = recordedTime + " " + recordedPackage;
            String line = String.format("%"+title.length()+"s", "").replace(' ', '-');
            osw.write(line);
            osw.write("\n");
            osw.write(title);
            osw.write("\n");
            osw.write(line);
            osw.write("\n");
            osw.write(recordedText);
            osw.write("\n\n");
            osw.flush();
            osw.close();
        } catch (Exception e) {
            Log.e(BuildConfig.APPLICATION_ID, e.getMessage());
        }
    }

    private String getDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        try {
            return format.format(java.util.Calendar.getInstance().getTime());
        } catch (Exception e) {
            return "";
        }
    }
}