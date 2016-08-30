package de.andre_lehnert.masterthesis.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by alehnert on 09.08.2016.
 */
public class NotificationServiceCommunicator extends BroadcastReceiver{

    private boolean hasCmdReceived = false;
    private Context context;

    public NotificationServiceCommunicator(Context context) {

        this.context = context;

        IntentFilter notificationCmdFilter = new IntentFilter();
        notificationCmdFilter.addAction("de.andre_lehnert.masterthesis.notifications.NOTIFICATION_CMD");

        context.registerReceiver(this, notificationCmdFilter);
    }

    public void destroy() {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String responseStatus = intent.getStringExtra("response_cmd");
        hasCmdReceived = (responseStatus == "OK");
    }


    public void addWhitelistElement(String appPackage) {
        Intent i = new Intent("de.andre_lehnert.masterthesis.notifications.NOTIFICATION_CMD");
        i.putExtra("whitelist", appPackage);
        context.sendBroadcast(i);
    }

    public void enableMonitoring() {
        Intent i = new Intent("de.andre_lehnert.masterthesis.notifications.NOTIFICATION_CMD");
        i.putExtra("cmd", "enable");
        context.sendBroadcast(i);
    }

    public void disableMonitoring() {
        Intent i = new Intent("de.andre_lehnert.masterthesis.notifications.NOTIFICATION_CMD");
        i.putExtra("cmd", "disable");
        context.sendBroadcast(i);
    }






}
