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
    public static SvcAcc instance = null;
    public static boolean isEnabled = false;
    public static boolean isActivating = false;
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
        // comming soon
    }

    private void setNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel serviceChannel = new NotificationChannel(BuildConfig.APPLICATION_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
        serviceChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        serviceChannel.setShowBadge(false);
        serviceChannel.setVibrationPattern(new long[]{ 0 });
        serviceChannel.enableVibration(true);
        serviceChannel.enableLights(false);
        serviceChannel.setSound(null, null);
        notificationManager.createNotificationChannel(serviceChannel);
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