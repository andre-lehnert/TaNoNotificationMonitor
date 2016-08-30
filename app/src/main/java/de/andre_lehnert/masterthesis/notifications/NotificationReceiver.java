package de.andre_lehnert.masterthesis.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import de.andre_lehnert.masterthesis.MainActivity;
import de.andre_lehnert.masterthesis.activtations.Activation;
import de.andre_lehnert.masterthesis.apps.App;
import de.andre_lehnert.masterthesis.database.HttpDatabase;
import de.andre_lehnert.masterthesis.invocations.Invocation;

/**
 * Created by alehnert on 09.08.2016.
 */
public class NotificationReceiver extends BroadcastReceiver {

    final String TAG = MainActivity.TAG + "/NOTIFICATION";

    private TextView recentNotification;
    private TextView removedNotifications;
    private TextView statNotificationTotal;
    private TextView statInvocationTotal;

    private int totalNotifications;
    private ArrayList<Notification> notifications = new ArrayList<>();
    private ArrayList<Invocation> invocations = new ArrayList<>();

    private HttpDatabase db;
    private String smartphoneId;

    private ArrayList<App> whitelist = new ArrayList<>();

    public ArrayList<Invocation> getInvocations() {
        return invocations;
    }

    public void setWhitelist(ArrayList<App> whitelist) {
        this.whitelist = whitelist;
    }

    public void setSmartphoneId(String smartphoneId) {
        this.smartphoneId = smartphoneId;
    }

    public void setStatInvocationTotal(TextView statInvocationTotal) {
        this.statInvocationTotal = statInvocationTotal;
    }

    public void setStatNotificationTotal(TextView statNotificationTotal) {
        this.statNotificationTotal = statNotificationTotal;
    }

    public ArrayList<Notification> getNotifications() {
        return notifications;
    }

    public void setRecentNotificationTextView(TextView tv) {
        this.recentNotification = tv;
    }

    public void setRemovedNotificationsTextView(TextView tv) {
        this.removedNotifications = tv;
    }


    public NotificationReceiver() {
        if (HttpDatabase.isInitialized()) db = HttpDatabase.getInstance();

    }

    private void saveNotification(Notification notification) {

        Log.i(TAG, ">>> SAVE NOTIFICATION");
        if (HttpDatabase.isInitialized()) {
            db = HttpDatabase.getInstance();
        } else {
            Log.d(TAG, "NO DB INSTANCE");
        }

        if (db != null & db.isOnline() & smartphoneId != null) {
            db.addNotification(smartphoneId, notification);

            statNotificationTotal.setText("Notifications:\t\t"+notifications.size());
        } else {
            Log.d(TAG, "DB = "+(db != null)+"; db.isOnline() = "+db.isOnline()+"; smartphoneId != null ? "+(smartphoneId != null));
        }
    }

    private void saveInvocation(Invocation invocation) {

        Log.i(TAG, ">>> SAVE INVOCATION");
        if (HttpDatabase.isInitialized()) {
            db = HttpDatabase.getInstance();
        } else {
            Log.d(TAG, "NO DB INSTANCE");
        }

        if (db != null & db.isOnline() & smartphoneId != null) {
            db.addInvocation(smartphoneId, invocation);

            statInvocationTotal.setText("Removed Notifications:\t\t"+invocations.size());
        } else {
            Log.d(TAG, "DB = "+(db != null)+"; db.isOnline() = "+db.isOnline()+"; smartphoneId != null ? "+(smartphoneId != null));
        }
    }

    private String findAppName(String appPackage) {
        for (App a: whitelist) {
            if (a.getAppPackage().equals(appPackage)) return a.getName();
        }
        return null;
    }
    private String findAppId(String appPackage) {
        for (App a: whitelist) {
            if (a.getAppPackage().equals(appPackage)) return a.getId();
        }
        return null;
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        // Get new notifications
        //
        //
        String pack = intent.getStringExtra("notification_add_package");
        String title = intent.getStringExtra("notification_add_title");
        String text = intent.getStringExtra("notification_add_text");

        if (title != null && text != null)
            if (recentNotification != null) {

                Notification notification = new Notification();
                notification.setAppPackage(pack);
                notification.setAppId( findAppId(pack) );
                notification.setAppName( findAppName(pack) );
                notification.setTitle(title);
                notification.setText(text);
                notification.setDate(new Date());

                // Special: NotificationGenerator -> Simulate App Notification
                if (text.contains("☞ Simulate: ")) {

                    String simulatedPackage = text.split(": ")[1];

                    Log.d(TAG, "SIMULATION DETECTED ☞ "+simulatedPackage);

                    for (App simulatedApp : whitelist) {
                        if (simulatedPackage.equals(simulatedApp.getAppPackage())) {

                            // Overwrite App Infos
                            notification.setAppPackage( simulatedApp.getAppPackage() );
                            notification.setAppId( simulatedApp.getId() );
                            notification.setAppName( simulatedApp.getName() );
                            notification.setText("Simulated Notification ["+simulatedPackage+"]");

                        }
                    }
                }

                notifications.add(notification);

                saveNotification(notification);

                recentNotification.setText("[" + pack + "]\n\nTitle:\n" + title + "\n\nText:\n" + text);



            }

        // Get removed notifications
        //
        //
        pack = intent.getStringExtra("notification_remove_package");
        title = intent.getStringExtra("notification_remove_title");
        text = intent.getStringExtra("notification_remove_text");

        if (title != null && text != null)
            if (removedNotifications != null) {

                Date start = new Date();
                Date end = new Date();
                end.setTime(start.getTime() + 1000);

                Invocation invocation = new Invocation();
                invocation.setStartDate(start);
                invocation.setEndDate(end);
                invocation.setAppPackage(pack);
                invocation.setAppId( findAppId(pack) );
                invocation.setAppName( findAppName(pack) );

                // Special: NotificationGenerator -> Simulate App Notification
                if (text.contains("☞ Simulate: ")) {

                    String simulatedPackage = text.split(": ")[1];

                    Log.d(TAG, "SIMULATION DETECTED ☞ "+simulatedPackage);

                    for (App simulatedApp : whitelist) {
                        if (simulatedPackage.equals(simulatedApp.getAppPackage())) {

                            // Overwrite App Infos
                            invocation.setAppPackage( simulatedApp.getAppPackage() );
                            invocation.setAppId( simulatedApp.getId() );
                            invocation.setAppName( simulatedApp.getName() );

                        }
                    }
                }

                invocations.add(invocation);

                saveInvocation(invocation);

                removedNotifications.setText("[" + pack + "]\n\nTitle:\n" + title + "\n\nText:\n" + text);
            }
    }


}