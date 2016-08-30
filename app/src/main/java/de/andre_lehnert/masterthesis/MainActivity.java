package de.andre_lehnert.masterthesis;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import de.andre_lehnert.masterthesis.activtations.ActivationService;
import de.andre_lehnert.masterthesis.apps.App;
import de.andre_lehnert.masterthesis.database.HttpDatabase;
import de.andre_lehnert.masterthesis.invocations.InvocationService;
import de.andre_lehnert.masterthesis.invocations.TodayInvocationRequest;
import de.andre_lehnert.masterthesis.notifications.NotificationReceiver;
import de.andre_lehnert.masterthesis.notifications.NotificationService;
import de.andre_lehnert.masterthesis.notifications.NotificationServiceCommunicator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public final static String TAG = "Main";

    // Services
    //
    //
    private NotificationService notifications;
    private NotificationReceiver notificationReceiver;
    private NotificationServiceCommunicator notificationCommunicator;


    private ActivationService activations;
    private InvocationService invocations;

    // Local Storage/ shared preferences
    //
    //
    private SharedPreferences storage;
    private final String storageName = "hciLocalStorage";
    private boolean isFirstStart = false; // request notification access

    // Settings
    //
    //
    private Context context;
    private int toastDuration = Toast.LENGTH_LONG;
    private boolean isEnabled = true;

    private String postfix = "/api/v1/";
    private String host = "192.168.0.100:8080";
    private String baseUrl = "http://" + host + postfix;

    private ArrayList<App> apps;

    private ArrayList<String> notificationList;
    private TextView recentNotification;
    private TextView removedNotifications;
    private TextView recentActivation;
    private EditText inputHost;
    private ImageView status;

    private TextView statNotifications;
    private TextView statActivations;
    private TextView statRemovedNotifications;
    private TextView statsAppUsage;
    private TextView whitelist;
    private Switch enableSwitch;

    private HttpDatabase db;

    private int totalNotifications;
    private int totalInvocations;
    private int totalActivations;

    private boolean isWhitelistInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();


        // Layout Components
        //
        //
        recentNotification = (TextView) findViewById(R.id.notifications);
        removedNotifications = (TextView) findViewById(R.id.removedNotifications);
        recentActivation = (TextView) findViewById(R.id.activation);

        statNotifications = (TextView) findViewById(R.id.stats_notification_total);
        statActivations = (TextView) findViewById(R.id.stats_activation_total);
        statRemovedNotifications = (TextView) findViewById(R.id.stats_removed_notification_total);
        statsAppUsage = (TextView) findViewById(R.id.stats_app_usage);
        whitelist = (TextView) findViewById(R.id.whitelist);

        status = (ImageView) findViewById(R.id.imageView);

        enableSwitch = (Switch) findViewById(R.id.switch2);

        inputHost = (EditText) findViewById(R.id.host);

        // Focus fix
        ((LinearLayout) findViewById(R.id.dummy)).requestFocus();

        // Toolbar
        //
        //
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Navigation drawer
        //
        //
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Starting monitoring services
        //
        //
        initializeNotificationService();
        initializeInvocationService();
        initializeActivationService();


        enableSwitch.setChecked(isEnabled);
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isEnabled = isChecked;

                checkOptions();
            }
        });



        inputHost.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {

                host = s.toString();

                if (host != null && host.length() > 0) {
                    baseUrl = "http://" + host + postfix;
                    Log.d(TAG, "BASE URL: " + baseUrl);
                    initializeDatabase();
                }
            }

        });

        inputHost.setText(host, TextView.BufferType.SPANNABLE);





        TodayInvocationRequest async = new TodayInvocationRequest(this, statsAppUsage);
        async.execute(getResources().getString(R.string.smartphone_id));



        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {

                        try {
                            Log.d(TAG, ">>> CHECK CONNECTIVITY");
                            (new AsyncTask<String, Void, Boolean>() {
                                @Override
                                protected Boolean doInBackground(String... params) {

                                    if (db != null && isEnabled) {
                                        db.isHostAvaiable(status, params[0]);
                                        return true;
                                    }
                                    return false;
                                }

                            }).execute(baseUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 1000, 10000); //execute in every 50000 ms


    }

    public void setUsage(String s) {
        statsAppUsage.setText(s);
    }

    private void initializeDatabase() {

        db = HttpDatabase.getInstance(this, baseUrl);

        if (! db.isNetworkAvailable()) {
           Toast toast = Toast.makeText(context, "No Network Connection!", toastDuration);
           toast.show();
        } else {
            if (! isWhitelistInitialized) {
                initializeApps();
                isWhitelistInitialized = true;
            }
        }

    }



    private void initializeApps() {

        apps = new ArrayList<>();
        Log.d(TAG, "ENABLE: "+isEnabled+", db.isNetworkAvailable() "+db.isNetworkAvailable());
        if (isEnabled && db.isNetworkAvailable())
            db.getApps(getResources().getString(R.string.smartphone_id), new HttpDatabase.RequestAppsCallback() {

                @Override
                public void onSuccess(ArrayList<App> appList) {
                    apps = appList;
                    whitelist.setText("");
                    int i = 1;
                    // Whitelisting
                    //
                    //
                    for (App a : apps) {
                        if (notificationCommunicator != null) {
                            notificationCommunicator.addWhitelistElement(a.getAppPackage());
                            String newEntry = whitelist.getText() + "\n["+i+"]\t"+a.getAppPackage();
                            whitelist.setText(newEntry);
                            i++;
                        }
                    }

                    if (notificationReceiver != null) notificationReceiver.setWhitelist(apps);

                    Log.d("SUCCESS", "Apps "+apps.size());
                }

                @Override
                public void onFailure(String error) {
                    Log.d("Error", error);
                    whitelist.setText("Whitelist request failed...");
                }
            });
    }

    private void checkOptions() {

        inputHost.setText(host, TextView.BufferType.SPANNABLE);

        if (isEnabled) {

            if (notificationCommunicator != null) notificationCommunicator.enableMonitoring();
            if (activations != null) activations.enable();

            initializeDatabase();

            if (db != null) db.enable();

        } else {

            if (notificationCommunicator != null) notificationCommunicator.disableMonitoring();
            if (activations != null) activations.disable();
            if (db != null) db.disable();
        }
    }


    /**
     *
     */
    private void initializeStorage() {

        // Restore preferences
        storage = getSharedPreferences(storageName, 0);

        // Get data on startup
        //
        //
        isFirstStart = storage.getBoolean("isFirstStart", true);
        isEnabled = storage.getBoolean("isEnabled", true);

        totalNotifications = storage.getInt("totalNotifications", 0);
        totalInvocations = storage.getInt("totalInvocations", 0);
        totalActivations = storage.getInt("totalActivations", 0);
        host = storage.getString("host", "hci.local");

        Log.d("Storage", "---------------------------------------------------------------------------");
        Log.d("Storage", "isFirstStart = " + isFirstStart);
        Log.d("Storage", "isEnabled = " + isEnabled);
        Log.d("Storage", "notification = " + totalNotifications);
        Log.d("Storage", "invocations = " + totalInvocations);
        Log.d("Storage", "activations = " + totalActivations);
        Log.d("Storage", "host = " + host);
        Log.d("Storage", "---------------------------------------------------------------------------");


        //statNotifications.setText("Total Notifications:\t\t"+totalNotifications);
        //statRemovedNotifications.setText("Removed Notifications:\t\t"+totalInvocations);
        //statActivations.setText("Total Activations:\t\t"+totalActivations);

        checkOptions();
    }

    /**
     *
     */
    private void initializeNotificationService() {

        notifications = new NotificationService();

        if (isFirstStart) {

            Log.i("Init", "*****\nNotifications Service initialization\n*****");
            Log.d("Init", "Request permissions");

            Toast toast = Toast.makeText(context, "First start detected! Please allow notification access", toastDuration);
            toast.show();

            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);

            isFirstStart = false;

        }

        // Here, thisActivity is the current activity
        if (isServiceRunning(NotificationService.class)) {

            if (notificationReceiver == null) {

                notificationReceiver = new NotificationReceiver();
                notificationReceiver.setRecentNotificationTextView(recentNotification);
                notificationReceiver.setRemovedNotificationsTextView(removedNotifications);
                notificationReceiver.setSmartphoneId(getResources().getString(R.string.smartphone_id));
                notificationReceiver.setStatNotificationTotal(statNotifications);
                notificationReceiver.setStatInvocationTotal(statRemovedNotifications);

                IntentFilter notificationFilter = new IntentFilter();
                notificationFilter.addAction("de.andre_lehnert.masterthesis.notifications.NOTIFICATION_LISTENER");

                registerReceiver(notificationReceiver, notificationFilter);

            }

            if (notificationCommunicator == null) {
                notificationCommunicator = new NotificationServiceCommunicator(context);
            }

            Log.i("Init", ">>>\t\tNotification Service started");

        } else {

            Log.d("Init", "Notifications Service doesn´t run");

        }
    }

    /**
     *
     */
    private void initializeActivationService() {

        if (activations == null) {

            activations = new ActivationService();
            activations.setRecentActivationTextView(recentActivation);
            activations.setSmartphoneId(getResources().getString(R.string.smartphone_id));
            activations.setStatActivationsTotal(statActivations);

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_USER_PRESENT);

            registerReceiver(activations, filter);

            Log.i("Init", ">>>\t\tActivation Service started");
        }
    }

    /**
     *
     */
    private void initializeInvocationService() {


        if (invocations == null) {

            invocations = new InvocationService();

            Log.d("Init", "HAS USAGE DATA ACCESS PERMISSION?\t" + invocations.hasPermission(context));

            if (invocations.hasPermission(context)) {

            } else {

                Toast toast = Toast.makeText(context, "Please allow usage data access", toastDuration);
                toast.show();

                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);

            }

            Log.i("Init", ">>>\t\tInvocation Service started");

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //InvocationService.printCurrentUsageStatus(MainActivity.this);

            //TodayInvocationRequest async = new TodayInvocationRequest(this, statsAppUsage);
            //async.execute(getResources().getString(R.string.smartphone_id));



            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void saveData() {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        storage = getSharedPreferences(storageName, 0);
        SharedPreferences.Editor editor = storage.edit();

        // Save data before closing
        //
        //
        editor.putBoolean("isEnabled", isEnabled);
        editor.putBoolean("isFirstStart", isFirstStart);
        editor.putString("host", host);

        // Commit the edits!
        editor.commit();
    }

// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    @Override
    protected void onStop() {
        super.onStop();

        saveData();
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Focus fix
        ((LinearLayout) findViewById(R.id.dummy)).requestFocus();


        // Start shared preferences storage
        //
        //
        initializeStorage();


        initializeDatabase();


        // Starting monitoring services
        //
        //
        initializeNotificationService();
        initializeInvocationService();
        initializeActivationService();

        checkOptions();
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (activations != null) unregisterReceiver(activations);
        if (notificationReceiver != null) unregisterReceiver(notificationReceiver);
        if (notificationCommunicator != null) notificationCommunicator.destroy();

        saveData();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

        saveData();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


        if (id == R.id.showWebsite) {
            String url = "http://" + host;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }


        return true;
    }


    // =============================================================================================
    // Utils
    // =============================================================================================

    /**
     * Check, if a application service is running
     *
     * @param serviceClass
     * @return
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}