package de.andre_lehnert.masterthesis.invocations;

import android.app.usage.UsageStats;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.andre_lehnert.masterthesis.MainActivity;
import de.andre_lehnert.masterthesis.apps.App;

/**
 * Created by alehnert on 10.08.2016.
 */
public class TodayInvocationRequest extends AsyncTask<String, Void, Long> {

    private Context context;
    private TextView statsAppUsage;

    private ArrayList<App> whitelist;

    public TodayInvocationRequest(Context context, TextView statsAppUsage) {
        this.context = context;
        this.statsAppUsage = statsAppUsage;
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSZ");
    private static final SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
    final String TAG = MainActivity.TAG + "/INVOCATION";

    @Override
    protected Long doInBackground(String... params) {
        Log.d("ASYNC", "execute");
        String smartphoneId = params[0];

        List<UsageStats> usage = InvocationService.getToday(context);

        long sum = 0;

        for (UsageStats u: usage) {

            // Just today
            if (day.format(new Date(u.getFirstTimeStamp())).equals( day.format(new Date()) ) ) {

                long millis = u.getTotalTimeInForeground();

                sum = sum + millis;

                String time = String.format("%02d h %02d min %02d sec",
                        TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                        TimeUnit.MILLISECONDS.toSeconds(millis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                );


                Log.d(TAG, dateFormat.format(new Date(u.getFirstTimeStamp())) + " - " + time + "\t" + u.getPackageName());
            }
        }

        return sum;
    }

    @Override
    protected void onPostExecute(Long sum) {

        long millis = sum;
        String time = String.format("%02d h %02d min %02d sec",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );

        statsAppUsage.setText("Total App Usage (today): "+ time);
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}

}
