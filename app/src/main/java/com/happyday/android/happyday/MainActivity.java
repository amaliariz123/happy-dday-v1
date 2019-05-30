package com.happyday.android.happyday;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.hardware.SensorEventListener;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener {

    Sensor mSensor;
    SensorEventListener sensorEventListener;
    SensorManager mSensorManager;
    int xAxis,yAxis,zAxis;
    ImageView ivCandleSkin,ivCandleFlame,ivCandleSmoke,ivCandleSkinSMoke;
    int x;
    Runnable runnable;
    Handler handler;
    Bitmap[] bitmaps;
    private int countNumLeft = 2;
    private int countNumRight = 2;
    ImageView leftCandle, rightCandle;

    private android.widget.Button mBtnNext, mBtnPrevious;


    SharedPreferences prefs;

    int[] CANDLES_A = new int[] { R.drawable.candle_1a, R.drawable.candle_2a,
            R.drawable.candle_3a, R.drawable.candle_4a, R.drawable.candle_5a,
            R.drawable.candle_6a, R.drawable.candle_7a, R.drawable.candle_8a,
            R.drawable.candle_9a, R.drawable.candle_10a};

    int[] CANDLES_B = new int[] { R.drawable.candle_1b, R.drawable.candle_2b,
            R.drawable.candle_3b, R.drawable.candle_4b, R.drawable.candle_5b,
            R.drawable.candle_6b, R.drawable.candle_7b, R.drawable.candle_8b,
            R.drawable.candle_9b, R.drawable.candle_10b, };

    int[] candle = new int[] {
            R.drawable.nol, R.drawable.satu, R.drawable.dua, R.drawable.tiga, R.drawable.empat,
            R.drawable.lima, R.drawable.enam, R.drawable.tujuh, R.drawable.delapan, R.drawable.sembilan
    };

    /* constants */
    private static final int POLL_INTERVAL = 1000;

    /** running state **/
    private boolean mRunning = false;

    /** config state **/
    private int mThreshold;

    private PowerManager.WakeLock mWakeLock;

    private Handler mHandler = new Handler();

    /* References to view elements */
//    private SoundLevelView mDisplay;

    /* data source */
    private SoundMeter mSensorMeter;

    /****************** Define runnable thread again and again detect noise *********/

    private Runnable mSleepTask = new Runnable() {
        public void run() {
            //Log.i("Noise", "runnable mSleepTask");
            start();
        }
    };

    // Create runnable thread to Monitor Voice
    private Runnable mPollTask = new Runnable() {
        public void run() {

            double amp = mSensorMeter.getAmplitude();
            //Log.i("Noise", "runnable mPollTask");
            updateDisplay(amp);

            if ((amp > mThreshold)) {
                Toast.makeText(getApplicationContext(), "Noise Thersold Crossed, do here your stuff.", Toast.LENGTH_LONG).show();
                Log.i("Noise", "==== onCreate ===");
            }

            // Runnable(mPollTask) will again execute after POLL_INTERVAL
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mBtnNext = findViewById(R.id.nextBtn);
        mBtnPrevious = findViewById(R.id.previousBtn);
        leftCandle = findViewById(R.id.left_candle);
        rightCandle = findViewById(R.id.right_candle);

        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countRight(view);
            }
        });

        mBtnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countLeft(view);
            }
        });

        init();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_reminder) {
            Intent about = new Intent(MainActivity.this, ReminderActivity.class);
            setTitle(R.string.title_activity_reminder);
            startActivity(about);
        } else if (id == R.id.nav_notification) {
            Intent about = new Intent(MainActivity.this, NotificationActivity.class);
            setTitle(R.string.title_activity_notification);
            startActivity(about);
        } else if (id == R.id.nav_language) {
            Intent about = new Intent(MainActivity.this, LanguageActivity.class);
            setTitle(R.string.title_activity_language);
            startActivity(about);
        } else if (id == R.id.nav_volume) {
            Intent about = new Intent(MainActivity.this, VolumeActivity.class);
            setTitle(R.string.title_activity_volume);
            startActivity(about);
        } else if (id == R.id.nav_about) {
            Intent about = new Intent(MainActivity.this, AboutActivity.class);
            setTitle(R.string.title_activity_about);
            startActivity(about);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSensorMeter.start();
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }

        //Noise monitoring start
        // Runnable(mPollTask) will execute after POLL_INTERVAL
        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stop();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mSensorManager.unregisterListener(this);
        Log.i("onPAUSE", "Pausing");
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);

        initializeApplicationConstants();

        if (!mRunning) {
            mRunning = true;
        }
        Log.i("onRESUME", "Resuming");
    }

    private void init() {
        // TODO Auto-generated method stub

        //sensor
        // Used to record voice
        mSensorMeter = new SoundMeter();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "NoiseAlert");

        handler = new Handler();

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        PackageManager mPackageManager = getPackageManager();

        if(mPackageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)){
            Log.i("SensorManager", "Present");
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        } /*else if (mPackageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR)){

        } */else {
            Log.i("SensorManager", "Not-Found");
        }

        ivCandleFlame = (ImageView)findViewById(R.id.imageView_candle_flame);

        ivCandleSmoke = (ImageView)findViewById(R.id.imageView_candle_smoke);

        ivCandleSmoke.setVisibility(ImageView.INVISIBLE);

        ivCandleFlame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                showSmoke();

                if(ivCandleFlame.isShown()){
                    ivCandleFlame.setVisibility(ImageView.INVISIBLE);
                    ivCandleSmoke.setVisibility(ImageView.VISIBLE);
                }

            }
        });

        ivCandleSmoke.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if(ivCandleSmoke.isShown()){
                    ivCandleFlame.setVisibility(ImageView.VISIBLE);
                    ivCandleSmoke.setVisibility(ImageView.INVISIBLE);
                }

            }
        });

        getSmokes();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub

        xAxis = (int) event.values[0];
        yAxis = (int) event.values[1];
        zAxis = (int) event.values[2];

        if (xAxis == 0) {
            ivCandleFlame.setImageDrawable(getCandle(R.drawable.candle_11));
        }
        if (xAxis ==1) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_B[9]));
        }
        if (xAxis ==2) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_B[8]));
        }
        if (xAxis ==3) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_B[7]));
        }
        if (xAxis ==4) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_B[6]));
        }
        if (xAxis ==5) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_B[5]));
        }
        if (xAxis ==6) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_B[4]));
        }
        if (xAxis ==7) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_B[3]));
        }
        if (xAxis ==8) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_B[2]));
        }
        if (xAxis ==9) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_B[1]));
        }
        if (xAxis ==10) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_B[0]));
        }

        if (xAxis ==-1) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_A[9]));

        }
        if (xAxis ==-2) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_A[8]));

        }
        if (xAxis ==-3) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_A[7]));

        }
        if (xAxis ==-4) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_A[6]));

        }
        if (xAxis ==-5) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_A[5]));

        }
        if (xAxis ==-6) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_A[4]));

        }
        if (xAxis ==-7) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_A[3]));
        }
        if (xAxis ==-8) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_A[2]));
        }
        if (xAxis ==-9) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_A[1]));
        }
        if (xAxis ==-10) {
            ivCandleFlame.setImageDrawable(getCandle(CANDLES_A[0]));
        }

        Log.i("onSensorChanged", "X-AXIS" +xAxis +"\nY-AXIS" +yAxis +"\nZ-AXIS" +zAxis);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    private Drawable getCandle(int candleID){
        Drawable drawable = getResources().getDrawable(candleID);
        return drawable;
    }

    private void getSmokes(){
        bitmaps = new Bitmap[21];
        bitmaps[0]= BitmapFactory.decodeResource(getResources(), R.drawable.smoke_1);
        bitmaps[1]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_2);
        bitmaps[2]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_3);
        bitmaps[3]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_4);
        bitmaps[4]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_5);
        bitmaps[5]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_6);
        bitmaps[6]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_7);
        bitmaps[7]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_8);
        bitmaps[8]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_9);
        bitmaps[9]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_10);
        bitmaps[10]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_1);
        bitmaps[11]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_2);
        bitmaps[12]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_3);
        bitmaps[13]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_4);
        bitmaps[14]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_5);
        bitmaps[15]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_6);
        bitmaps[16]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_7);
        bitmaps[17]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_8);
        bitmaps[18]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_9);
        bitmaps[19]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_10);
        bitmaps[20]=BitmapFactory.decodeResource(getResources(), R.drawable.smoke_1);
    }
    private void showSmoke(){
        runnable = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                for(int i=0;i<21;i++){

                    x = i;
                    try {
                        Thread.sleep(100);
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                if(x==20){
                                    if(ivCandleSmoke.isShown()){
                                        ivCandleFlame.setVisibility(ImageView.INVISIBLE);
                                        ivCandleSmoke.setVisibility(ImageView.VISIBLE);
                                    }
                                    ivCandleSmoke.setImageBitmap(bitmaps[x]);
                                }else{
                                    ivCandleSmoke.setImageBitmap(bitmaps[x]);
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    //Sensor
    private void start() {
        Log.i("Noise", "==== start ===");

        mSensorMeter.start();
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }

        //Noise monitoring start
        // Runnable(mPollTask) will execute after POLL_INTERVAL
        mHandler.postDelayed(mPollTask, POLL_INTERVAL);
    }

    private void stop() {
        Log.i("Noise", "==== Stop Noise Monitoring===");
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mHandler.removeCallbacks(mSleepTask);
        mHandler.removeCallbacks(mPollTask);
        mSensorMeter.stop();
//        mDisplay.setLevel(0,0);
        updateDisplay(0.0);
        mRunning = false;

    }

    private void initializeApplicationConstants() {
        // Set Noise Threshold
        mThreshold = 8;
    }

    private void updateDisplay(double signalEMA) {
        //matikan lilin
        if (signalEMA > 1) {
                ivCandleFlame.setVisibility(ImageView.INVISIBLE);
                ivCandleSmoke.setVisibility(ImageView.VISIBLE);
            showSmoke();
        }
    }

    public void countLeft(View view) {
        countNumLeft++;
        if (leftCandle != null){
            if (countNumLeft == 0){
                leftCandle.setImageResource(candle[0]);
            }else if (countNumLeft == 1){
                leftCandle.setImageResource(candle[1]);
            }else if (countNumLeft == 2){
                leftCandle.setImageResource(candle[2]);
            }else if (countNumLeft == 3){
                leftCandle.setImageResource(candle[3]);
            }else if (countNumLeft == 4){
                leftCandle.setImageResource(candle[4]);
            }else if (countNumLeft == 5){
                leftCandle.setImageResource(candle[5]);
            }else if (countNumLeft == 6){
                leftCandle.setImageResource(candle[6]);
            }else if (countNumLeft == 7){
                leftCandle.setImageResource(candle[7]);
            }else if (countNumLeft == 8){
                leftCandle.setImageResource(candle[8]);
            }else if (countNumLeft == 9){
                leftCandle.setImageResource(candle[9]);
            }else if (countNumLeft > 9){
                countNumLeft = 0;
                leftCandle.setImageResource(candle[0]);
            }
        }
    }

    public void countRight(View view) {
        countNumRight++;
        if (rightCandle != null){
            if (countNumRight == 0){
                rightCandle.setImageResource(candle[0]);
            }else if (countNumRight == 1){
                rightCandle.setImageResource(candle[1]);
            }else if (countNumRight == 2){
                rightCandle.setImageResource(candle[2]);
            }else if (countNumRight == 3){
                rightCandle.setImageResource(candle[3]);
            }else if (countNumRight == 4){
                rightCandle.setImageResource(candle[4]);
            }else if (countNumRight == 5){
                rightCandle.setImageResource(candle[5]);
            }else if (countNumRight == 6){
                rightCandle.setImageResource(candle[6]);
            }else if (countNumRight == 7){
                rightCandle.setImageResource(candle[7]);
            }else if (countNumRight == 8){
                rightCandle.setImageResource(candle[8]);
            }else if (countNumRight == 9){
                rightCandle.setImageResource(candle[9]);
            }else if (countNumRight > 9){
                countNumRight = 0;
                rightCandle.setImageResource(candle[0]);
            }
        }
    }
}
