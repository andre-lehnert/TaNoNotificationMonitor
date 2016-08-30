package de.andre_lehnert.masterthesis.database;


import android.app.DownloadManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.andre_lehnert.masterthesis.MainActivity;
import de.andre_lehnert.masterthesis.R;
import de.andre_lehnert.masterthesis.activtations.Activation;
import de.andre_lehnert.masterthesis.apps.App;
import de.andre_lehnert.masterthesis.invocations.Invocation;
import de.andre_lehnert.masterthesis.notifications.Notification;

/**
 * Created by alehnert on 09.08.2016.
 */
public class HttpDatabase {

    // #############################################################################################
    //
    // Singelton
    //
    private static HttpDatabase instance;
    public static HttpDatabase getInstance () {
        return HttpDatabase.instance;
    }
    public static HttpDatabase getInstance (Context context, String baseUrl) {

        if (_baseUrl != baseUrl) {
            Log.d("DATABASE", "NEW BASE URL: "+baseUrl);
            _baseUrl = baseUrl;
            HttpDatabase.instance = new HttpDatabase (context, baseUrl);

        } else if (HttpDatabase.instance == null) {
            Log.d("DATABASE", "NEW DB: "+baseUrl);
            _baseUrl = baseUrl;
            HttpDatabase.instance = new HttpDatabase (context, baseUrl);
        }
        return HttpDatabase.instance;
    }
    public static boolean isInitialized() {
        return (HttpDatabase.instance != null);
    }
    // #############################################################################################

    public static final String REQUEST = "MyTag";
    final String TAG = MainActivity.TAG + "/DB";
    StringRequest stringRequest; // Assume this exists.
    JsonObjectRequest jsonRequest;
    RequestQueue mRequestQueue;  // Assume this exists.
    private Context context;
    private String baseUrl;
    private boolean isEnabled = true;

    public static String _baseUrl;

    private boolean isOnline = false;

    private final DefaultRetryPolicy policy = new DefaultRetryPolicy(
            10000, // timeout ms
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    public HttpDatabase(Context context, String baseUrl) {

        this.context = context;
        this.baseUrl = baseUrl;

        mRequestQueue = Volley.newRequestQueue(context);

        testConnection();

    }

    public boolean isOnline() {
        return isOnline;
    }

    public void getApps(String smartphoneId, final RequestAppsCallback callback) {

        if (isEnabled && isNetworkAvailable()) {

            final String url = baseUrl + "smartphones/" + smartphoneId + "/apps";
            Log.i(TAG, ">>> REQUEST " + "GET " + url);


            jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,

                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // the response is already constructed as a JSONObject!
                            try {

                                ArrayList<App> apps = new ArrayList<>();

                                JSONArray objs = response.getJSONArray("objects");

                                for (int i = 0; i < objs.length(); i++) {
                                    JSONObject obj = (JSONObject) objs.get(i);

                                    App app = new App();
                                    app.setId(obj.getString("id"));
                                    app.setName(obj.getString("name__c"));
                                    app.setAppPackage(obj.getString("package__c"));

                                    apps.add(app);
                                }

                                callback.onSuccess(apps);
                                Log.i(TAG, ">>> REQUEST [DONE] " + "GET " + url);

                            } catch (Exception e) {
                                e.printStackTrace();
                                callback.onFailure(e.toString());
                                Log.i(TAG, ">>> REQUEST [FAILED] " + "GET " + url);
                            }
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //error.printStackTrace();
                            callback.onFailure(error.toString());
                        }
                    });

            jsonRequest.setTag(REQUEST);
            jsonRequest.setRetryPolicy(policy);

            // Add the request to the RequestQueue.
            mRequestQueue.add(jsonRequest);



        } else {
            Log.d(TAG, "DATABASE DISABLED -> REQUEST BLOCKED");
        }

    }


    public void addNotification(String smartphoneId, Notification notification) {

        if (isEnabled && isNetworkAvailable()) {

            if (notification.getAppId() == null) {
                Log.i(TAG, ">>> NOTIFICATION -> MISSING APP ID: "+notification.getAppPackage()+": "+notification.getTitle());
            } else {

                final String url = baseUrl + "smartphones/" + smartphoneId + "/apps/" + notification.getAppId() + "/notifications";
                Log.i(TAG, ">>> REQUEST " + "POST " + url);

            /*
             {
              "createddate": "2016-08-03T14:21:10.000+0000",
              "name": "notification-10",
              "text__c": null,
              "datetime__c": "2016-07-31T14:21:00.000+0000",
              "title__c": null,
              "priority__c": 4,
              "contact__c": null,
              "app__c": "a0B58000004L5xsEAC",
              "id": "a0F58000001k2P7EAI"
            }
             */

                try {

                    JSONObject obj = new JSONObject();

                    obj.put("title__c", notification.getTitle());
                    obj.put("text__c", notification.getText());
                    obj.put("datetime__c", notification.getDateFormated());
                    obj.put("app__c", notification.getAppId());

                    Log.d(TAG, ">>> JSON\t" + obj.toString());

                    jsonRequest = new JsonObjectRequest(Request.Method.POST, url, obj,

                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i(TAG, ">>> REQUEST [DONE] " + "POST " + url);
                                }
                            },

                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //error.printStackTrace();
                                    Log.i(TAG, ">>> REQUEST [FAILED] " + "POST " + url);
                                }
                            });

                    jsonRequest.setTag(REQUEST);
                    jsonRequest.setRetryPolicy(policy);

                    // Add the request to the RequestQueue.
                    mRequestQueue.add(jsonRequest);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        } else {
            Log.d(TAG, "DATABASE DISABLED -> REQUEST BLOCKED");
        }
    }




    public void addInvocation(String smartphoneId, Invocation invocation) {

        if (isEnabled && isNetworkAvailable()) {

            if (invocation.getAppId() == null) {
                Log.i(TAG, ">>> INVOCATION -> MISSING APP ID: "+invocation.getAppPackage());

            } else {

                final String url = baseUrl + "smartphones/" + smartphoneId + "/apps/" + invocation.getAppId() + "/invocations";
                Log.i(TAG, ">>> REQUEST " + "POST " + url);

                /*
                 {
                  "createddate": "2016-08-03T13:49:48.000+0000",
                  "lastmodifieddate": "2016-08-03T13:49:48.000+0000",
                  "start__c": "2016-07-25T13:49:00.000+0000",
                  "app__c": "a0B58000004L5xdEAC",
                  "total_time__c": 60,
                  "end__c": "2016-07-25T13:50:00.000+0000",
                  "id": "a0I58000001YmFOEA0"
                }
                 */

                try {

                    JSONObject obj = new JSONObject();

                    obj.put("start__c", invocation.getStartDateFormated());
                    obj.put("end__c", invocation.getEndDateFormated());
                    obj.put("app__c", invocation.getAppId());

                    Log.d(TAG, ">>> JSON\t" + obj.toString());

                    jsonRequest = new JsonObjectRequest(Request.Method.POST, url, obj,

                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.i(TAG, ">>> REQUEST [DONE] " + "POST " + url);
                                }
                            },

                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //error.printStackTrace();
                                    Log.i(TAG, ">>> REQUEST [FAILED] " + "POST " + url);
                                }
                            });

                    jsonRequest.setTag(REQUEST);
                    jsonRequest.setRetryPolicy(policy);

                    // Add the request to the RequestQueue.
                    mRequestQueue.add(jsonRequest);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        } else {
            Log.d(TAG, "DATABASE DISABLED -> REQUEST BLOCKED");
        }
    }


    public void addActivation(String smartphoneId, Activation activation) {

        if (isEnabled && isNetworkAvailable()) {

            final String url = baseUrl + "smartphones/" + smartphoneId + "/activations";
            Log.i(TAG, ">>> REQUEST " + "POST " + url);

            /*
             {
              "createddate": "2016-08-03T15:37:22.000+0000",
              "lastmodifieddate": "2016-08-03T15:37:22.000+0000",
              "start__c": "2016-08-03T15:37:00.000+0000",
              "smartphone__c": "a0958000001auqVAAQ",
              "total_time__c": 600,
              "end__c": "2016-08-03T15:47:00.000+0000",
              "id": "a0L5800000136gLEAQ"
            }
             */

            try {

                JSONObject obj = new JSONObject();

                obj.put("start__c", activation.getStartDateFormated());
                obj.put("end__c", activation.getEndDateFormated());
                obj.put("smartphone__c", activation.getSmartphoneId());

                Log.d(TAG, ">>> JSON\t" + obj.toString());

                jsonRequest = new JsonObjectRequest(Request.Method.POST, url, obj,

                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.i(TAG, ">>> REQUEST [DONE] " + "POST " + url);
                            }
                        },

                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //error.printStackTrace();
                                Log.i(TAG, ">>> REQUEST [FAILED] " + "POST " + url);
                            }
                        });

                jsonRequest.setTag(REQUEST);
                jsonRequest.setRetryPolicy(policy);

                // Add the request to the RequestQueue.
                mRequestQueue.add(jsonRequest);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Log.d(TAG, "DATABASE DISABLED -> REQUEST BLOCKED");
        }
    }

    public void enable() {
        isEnabled = true;
    }

    public void disable() {
        isEnabled = true;
    }

    // =============================================================================================
    //
    // =============================================================================================


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void testConnection() {

        if (isEnabled) {


            String url = baseUrl;

            // Request a string response from the provided URL.
            stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            isOnline = true;
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            isOnline = false;
                            Log.d(TAG, "DATABASE OFFLINE");
                        }
                    });
            stringRequest.setTag(REQUEST);

            // Add the request to the RequestQueue.
            mRequestQueue.add(stringRequest);

        } else {

            Log.d(TAG, "DATABASE DISABLED -> REQUEST BLOCKED");
            isOnline = false;
        }

    }
    private int msgCount = 0;
    public void isHostAvaiable(final ImageView iv, final String url) {

        if (isEnabled) {
            // Request a string response from the provided URL.
            stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            iv.setImageResource(R.drawable.ic_done);
                            iv.setColorFilter(context.getResources().getColor(R.color.colorOK, context.getTheme()));
                            msgCount = 0;
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            iv.setImageResource(R.drawable.ic_report);
                            iv.setColorFilter(context.getResources().getColor(R.color.colorAccent, context.getTheme()));

                            if (msgCount < 3) {
                                Toast toast = Toast.makeText(context, "Host " + url + " not reachable!", Toast.LENGTH_SHORT);
                                toast.show();
                                msgCount++;
                            }
                        }
                    });
            stringRequest.setTag(REQUEST);

            // Add the request to the RequestQueue.
            mRequestQueue.add(stringRequest);
        }
    }


    public interface RequestAppsCallback {
        void onSuccess(ArrayList<App> apps);
        void onFailure(String error);
    }


    public void destroy() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(REQUEST);
        }
    }
}
