package com.happyday.android.happyday;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.SeekBar;
import android.widget.TextView;

public class VolumeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView volumeText;
    private SeekBar volumeControl;
    private AudioManager audioManager;

    int maxVolume = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volume);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        initializeControls();
    }

    private void initializeControls(){
        volumeControl = findViewById(R.id.volume_control);
        volumeText = findViewById(R.id.volume);

        try{
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            volumeControl.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeControl.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

            volumeControl.setKeyProgressIncrement(1);
            maxVolume = volumeControl.getMax();
            volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    float perc = (progress /(float)maxVolume)*100;
                    volumeText.setText("Volume : " + (int)perc + " %");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        } catch(Exception e){

        }
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

        if (id == R.id.nav_home) {
            // Handle the camera action
            Intent about = new Intent(VolumeActivity.this, MainActivity.class);
            startActivity(about);
        } else if (id == R.id.nav_reminder) {
            Intent about = new Intent(VolumeActivity.this, ReminderActivity.class);
            setTitle(R.string.title_activity_reminder);
            startActivity(about);
        } else if (id == R.id.nav_notification) {
            Intent about = new Intent(VolumeActivity.this, NotificationActivity.class);
            setTitle(R.string.title_activity_notification);
            startActivity(about);
        } else if (id == R.id.nav_language) {
            Intent about = new Intent(VolumeActivity.this, LanguageActivity.class);
            setTitle(R.string.title_activity_language);
            startActivity(about);
        } else if (id == R.id.nav_volume) {
            Intent about = new Intent(VolumeActivity.this, VolumeActivity.class);
            setTitle(R.string.title_activity_volume);
            startActivity(about);
        } else if (id == R.id.nav_about) {
            Intent about = new Intent(VolumeActivity.this, AboutActivity.class);
            setTitle(R.string.title_activity_about);
            startActivity(about);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
