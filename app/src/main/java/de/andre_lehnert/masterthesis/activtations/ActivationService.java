package de.andre_lehnert.masterthesis.activtations;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.andre_lehnert.masterthesis.MainActivity;
import de.andre_lehnert.masterthesis.database.HttpDatabase;
import de.andre_lehnert.masterthesis.invocations.Invocation;


public class ActivationService extends BroadcastReceiver {

    final String TAG = MainActivity.TAG + "/ACTIVATION";

    private ArrayList<Activation> activations = new ArrayList<>();
    private Activation currentSession;

    private TextView recentActivation;
    private TextView statActivationsTotal;
    private String smartphoneId;

    private HttpDatabase db;

    private boolean isEnabled = true;

    public void setRecentActivationTextView(TextView tv) {
        this.recentActivation = tv;
    }

    public String getSmartphoneId() {
        return smartphoneId;
    }

    public void setSmartphoneId(String smartphoneId) {
        this.smartphoneId = smartphoneId;
    }

    public void setStatActivationsTotal(TextView statActivationsTotal) {
        this.statActivationsTotal = statActivationsTotal;
    }

    public ArrayList<Activation> getActivations() {
        return activations;
    }

    public void disable() {
        isEnabled = false;
    }

    public void enable() {
        isEnabled = true;
    }

    public ActivationService() {
        if (HttpDatabase.isInitialized()) db = HttpDatabase.getInstance();
    }



    private void saveActivation(Activation activation) {

        Log.i(TAG, ">>> SAVE ACTIVATION/ USER SESSION");
        if (HttpDatabase.isInitialized()) {
            db = HttpDatabase.getInstance();
        } else {
            Log.d(TAG, "NO DB INSTANCE");
        }

        if (db != null & db.isOnline() & smartphoneId != null) {
            db.addActivation(smartphoneId, activation);

            statActivationsTotal.setText("Activations:\t\t"+ activations.size()  );
        } else {
            Log.d(TAG, "DB = "+(db != null)+"; db.isOnline() = "+db.isOnline()+"; smartphoneId != null ? "+(smartphoneId != null));
        }
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {

        if (isEnabled) {

            String formattedDate = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date());

            if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                Log.i(TAG, "--- Activation Event ---\n>>> ACTION_USER_PRESENT");

                currentSession = new Activation();
                currentSession.setStartDate(new Date());
                currentSession.setSmartphoneId(smartphoneId);

                recentActivation.setText("ACTION_USER_PRESENT: " + formattedDate);
            }

            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.i(TAG, "--- Activation Event ---\n>>> ACTION_SCREEN_ON");
                //recentActivation.setText("ACTION_SCREEN_ON: "+formattedDate);
            }

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i(TAG, "--- Activation Event ---\n>>> ACTION_SCREEN_OFF");

                if (currentSession != null) {

                    currentSession.setEndDate(new Date());

                    activations.add(currentSession);

                    saveActivation(currentSession);

                    currentSession = null;
                }

                recentActivation.setText("ACTION_SCREEN_OFF: " + formattedDate);
            }
        }
    }
}
