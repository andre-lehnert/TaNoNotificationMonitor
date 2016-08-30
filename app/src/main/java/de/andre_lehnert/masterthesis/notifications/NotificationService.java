package de.andre_lehnert.masterthesis.notifications;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;

import de.andre_lehnert.masterthesis.MainActivity;

/**
 * Created by alehnert on 08.08.2016.
 */
public class NotificationService extends NotificationListenerService {

    final String TAG = MainActivity.TAG + "/NOTIFICATION";
    Context context;
    private NotificationServiceReceiver cmdReceiver;

    private ArrayList<String> whitelist = new ArrayList<>();

    private boolean isEnabled = true;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        cmdReceiver = new NotificationServiceReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction("de.andre_lehnert.masterthesis.notifications.NOTIFICATION_CMD");
        registerReceiver(cmdReceiver, filter);

        Log.d(TAG, "*****\nStarted\n******");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(cmdReceiver);
    }


    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {

        if (isEnabled) {

            Log.d(TAG, "--- Notification added ---");

            if (statusBarNotification.getNotification() != null) {

                boolean hasTitle = false,
                        hasText = false;

                Intent i = new Intent("de.andre_lehnert.masterthesis.notifications.NOTIFICATION_LISTENER");

                if (statusBarNotification.getPackageName() != null) {
                    String pack = statusBarNotification.getPackageName();

                    if ( ! isAllowed(pack) ) {
                        Log.d(TAG, "Package:\t" + pack + " NOT ALLOWED");
                        return;
                    }

                    Log.d(TAG, "Package:\t" + pack);
                    i.putExtra("notification_add_package", pack);
                }

                if (statusBarNotification.getNotification().extras != null) {
                    Bundle extras = statusBarNotification.getNotification().extras;

                    if (extras.getCharSequence("android.title") != null) {
                        String title = extras.getString("android.title");
                        Log.d(TAG, "Title:\t\t" + title);

                        i.putExtra("notification_add_title", title);

                        hasTitle = true;
                    }

                    if (extras.getCharSequence("android.text") != null) {
                        String text = extras.getCharSequence("android.text").toString();
                        Log.d(TAG, "Text:\t\t" + text);

                        i.putExtra("notification_add_text", text);

                        hasText = true;
                    }
                }

                if (hasTitle || hasText) {
                    sendBroadcast(i);
                }
            }

        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {

        if (isEnabled) {


            Log.d(TAG, "--- Notification removed ---");

            if (statusBarNotification.getNotification() != null) {

                boolean hasTitle = false,
                        hasText = false;

                Intent i = new Intent("de.andre_lehnert.masterthesis.notifications.NOTIFICATION_LISTENER");

                if (statusBarNotification.getPackageName() != null) {
                    String pack = statusBarNotification.getPackageName();

                    if ( ! isAllowed(pack) ) {
                        Log.d(TAG, "Package:\t" + pack + " NOT ALLOWED");
                        return;
                    }

                    Log.d(TAG, "Package:\t" + pack);
                    i.putExtra("notification_remove_package", pack);

                }

                if (statusBarNotification.getNotification().extras != null) {
                    Bundle extras = statusBarNotification.getNotification().extras;

                    if (extras.getCharSequence("android.title") != null) {
                        String title = extras.getString("android.title");
                        Log.d(TAG, "Title:\t\t" + title);


                        i.putExtra("notification_remove_title", title);

                        hasTitle = true;
                    }

                    if (extras.getCharSequence("android.text") != null) {
                        String text = extras.getCharSequence("android.text").toString();
                        Log.d(TAG, "Text:\t\t" + text);

                        i.putExtra("notification_remove_text", text);

                        hasText = true;
                    }
                }

                if (hasTitle || hasText) {
                    sendBroadcast(i);
                }
            }

        }
    }

    private boolean isAllowed(String appPackage) {
        for (String entry: whitelist) {
            if (entry.equals(appPackage)) return true;
        }
        return false;
    }

    public boolean hasPermission() {

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE);

        return (permissionCheck == PackageManager.PERMISSION_GRANTED);

    }


    class NotificationServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null & intent.getStringExtra("cmd") != null) {

                String cmd = intent.getStringExtra("cmd");
                Log.d(TAG, "CMD RECEIVED: "+cmd);

                if (cmd.equals("disable")) {

                    isEnabled = false;

                    Intent i = new Intent("de.andre_lehnert.masterthesis.notifications.NOTIFICATION_CMD");
                    i.putExtra("response_cmd", "OK");
                    sendBroadcast(i);

                    Log.i(TAG, "--- Command Event ---\n Notfication monitoring stopped");

                } else if (cmd.equals("enable")) {

                    isEnabled = true;

                    Intent i = new Intent("de.andre_lehnert.masterthesis.notifications.NOTIFICATION_CMD");
                    i.putExtra("response_cmd", "OK");
                    sendBroadcast(i);

                    Log.i(TAG, "--- Command Event ---\n Notfication monitoring started");
                }

            } else if (intent != null & intent.getStringExtra("whitelist") != null) {

                String entry = intent.getStringExtra("whitelist");

                whitelist.add(entry);

                Intent i = new Intent("de.andre_lehnert.masterthesis.notifications.NOTIFICATION_CMD");
                i.putExtra("response_whitelist", "OK");
                sendBroadcast(i);

                Log.i(TAG, ">>> New Whitelist Entry: \t"+ entry);

            }

        }
    }

}