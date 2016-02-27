package com.hackathon.harmalarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bottlerocketstudios.groundcontrol.convenience.GroundControl;
import com.bottlerocketstudios.groundcontrol.listener.AgentListener;
import com.hackathon.harmalarm.service.DataAgent;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView mTemperatureTextView;
    ImageView mStatusIcon;
    Button mMedicineButton;
    Vibrator mVibrator;

    private int mCurrentNotificationId = 0;

    public final static String EXTRA_TEMPERATURE_ENTRY_LIST = "com.hackathon.harmalarm.temperature_entry_list";

    final static int CAUTION_TEMPERATURE = 99;
    final static int DANGER_TEMPERATURE = 103;
    final static int PING_TIME_SECONDS = 30;

    final static int MEDICINE_REQUEST_CODE = 100;

    public static boolean SHOW_MEDICINE_MESSAGE = false;

    public static List<TemperatureEntry> sTemperatureEntries;

    private int mPreviousTemperature = 0;
    private static final String TAG = "[MainActivity] ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });

        mTemperatureTextView = (TextView) findViewById(R.id.temperature_text);
        mStatusIcon = (ImageView) findViewById(R.id.status_icon);
        mMedicineButton = (Button) findViewById(R.id.medicine_button);
        mMedicineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MedicineDialog dialogFragment = new MedicineDialog();
                dialogFragment.show(getSupportFragmentManager(), "medicine");
            }
        });
        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        startDataAgent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SHOW_MEDICINE_MESSAGE == true) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Medicine Time!", Toast.LENGTH_SHORT).show();
                    mVibrator.vibrate(200);
                }
            }, 3000);
            SHOW_MEDICINE_MESSAGE = false;
        }
    }

    private void startDataAgent() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Running scheduled job.");
                callDataAgent();
            }
        }, 0, PING_TIME_SECONDS, TimeUnit.SECONDS);
    }

    private void callDataAgent() {
        GroundControl.uiAgent(this, new DataAgent(this))
                .uiCallback(new AgentListener<List<TemperatureEntry>, Integer>() {
                    @Override
                    public void onCompletion(String agentIdentifier, List<TemperatureEntry> result) {
                        sTemperatureEntries = result;
                        int latestTemperature = result.get(0).getTemperature();
                        Log.i(TAG, "Updating temperature text view.");
                        mTemperatureTextView.setText(latestTemperature + "\u00B0");
                        adjustTemperatureColor(latestTemperature);
                    }

                    @Override
                    public void onProgress(String agentIdentifier, Integer progress) {
                        // Do nothing
                    }
                }).bypassCache(true)
        .execute();
    }

    private void adjustTemperatureColor(int temperature) {
        int temperatureColor;
        if (temperature < CAUTION_TEMPERATURE) {
            temperatureColor = R.color.colorOkay;
            mStatusIcon.setImageDrawable(getResources().getDrawable(R.drawable.heart_icon));
            mPreviousTemperature = temperature;
        } else if (temperature < DANGER_TEMPERATURE) {
            temperatureColor = R.color.colorCaution;
            mStatusIcon.setImageDrawable(getResources().getDrawable(R.drawable.caution_icon));
            if (mPreviousTemperature < CAUTION_TEMPERATURE) {
                mVibrator.vibrate(250);
                sendNotification("Caution: Temperature Warning", "Jonathan's temperature is higher than the comfortable level.");
            }
            mPreviousTemperature = temperature;
        } else {
            temperatureColor = R.color.colorDanger;
            mStatusIcon.setImageDrawable(getResources().getDrawable(R.drawable.danger_icon));
            if (mPreviousTemperature < DANGER_TEMPERATURE) {
                mVibrator.vibrate(500);
                sendNotification("Danger! Temperature Warning", "Jonathan's temperature is dangerously high.");
            }
            mPreviousTemperature = temperature;
        }
        mTemperatureTextView.setTextColor(getResources().getColor(temperatureColor));
    }

    private void sendNotification(String title, String body) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).
                setSmallIcon(R.drawable.heart_icon).
                setContentTitle(title).
                setContentText(body);
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(++mCurrentNotificationId, builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GroundControl.onDestroy(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_graph) {
            Intent intent = new Intent(this, InfoActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
