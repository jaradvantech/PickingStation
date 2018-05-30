package com.example.administrator.PickingStation;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements
        Line.OnFragmentInteractionListener,
        Algorithm.OnFragmentInteractionListener,
        Editor.OnFragmentInteractionListener,
        Manual.OnFragmentInteractionListener,
        Debug.OnFragmentInteractionListener,
        Alarms.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    int previous_id = R.id.holder_loading;
    TcpClient mTcpClient;
    Boolean FirstTimeRPRV =true;
    Line line = new Line();
    Algorithm algorithm = new Algorithm();
    Editor editor = new Editor();
    Logs logs = new Logs();
    Manual manual = new Manual();
    Debug debug = new Debug();
    Settings settings = new Settings();
    Loading loading = new Loading();
    Alarms alarms = new Alarms();


    /*****************************************************
     *                                  --Globals--
     *****************************************************/
    private final int TRANSITION_TIME = 400;
    private final int ALARM_CHECK_PERIOD = 2000;
    private final int RPRV_PERIOD = 300;
    private final Handler alarmLoopHandler = new Handler(Looper.getMainLooper());
    private final AlarmManager mAlarmManager = new AlarmManager(this);
    private ImageView appbar_connection;
    private Button appbarTransparentButton;
    private ImageView manipulatorAlarmIcon[] = new ImageView[5];
    private ImageView equipmentAlarmIcon;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile boolean DoLoops = false;
    private String CurrentLanguage = "en"; //default
    private NavigationView navigationView;
    private DrawerLayout drawer;

    /*****************************************************
     *
     *
     *                                  --ON CREATE--
     *
     *
     *****************************************************/
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        correctDisplayMetrics();
        setContentView(R.layout.activity_main);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        setGUILanguage();

        //Make transparent so when user press any icon view switches to alarms
        appbarTransparentButton = (Button) findViewById(R.id.appbar_button_transparentButton);
        appbarTransparentButton.setVisibility(View.VISIBLE);
        appbarTransparentButton.setBackgroundColor(Color.TRANSPARENT);
        appbar_connection = (ImageView) findViewById(R.id.appbar_imageView_connection);
        manipulatorAlarmIcon[0] = (ImageView) findViewById(R.id.appbar_imageView_arm1);
        manipulatorAlarmIcon[1] = (ImageView) findViewById(R.id.appbar_imageView_arm2);
        manipulatorAlarmIcon[2] = (ImageView) findViewById(R.id.appbar_imageView_arm3);
        manipulatorAlarmIcon[3] = (ImageView) findViewById(R.id.appbar_imageView_arm4);
        manipulatorAlarmIcon[4] = (ImageView) findViewById(R.id.appbar_imageView_arm5);
        equipmentAlarmIcon = (ImageView) findViewById(R.id.appbar_imageview_equipment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        getSupportActionBar().setTitle(getResources().getString(R.string.Loading));

        // FRAGMENT MANAGEMENT  https://www.youtube.com/watch?v=iksjcQuxtt4
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.holder_line, line, line.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_algorithm, algorithm, algorithm.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_editor, editor, editor.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_logs, logs, logs.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_manual, manual, manual.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_debug, debug, debug.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_settings, settings, settings.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_loading, loading, loading.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_alarms, alarms, alarms.getTag()).commit();

        //On any icon pressed in the Appbar, switch to alarm view.
        appbarTransparentButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switchToLayout(R.id.nav_alarms);
            }
        });

        startCheckingForAlarms();
    }

    /*
     * Release all resources before leaving app
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mTcpClient.stopClient();
    }

    /*
     * When back button is pressed:
     */
    @Override
    public void onBackPressed() {

        //If drawer was open, just close drawer
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

         //Otherwise, go to Line Status
        } else {
            startRPRV();
            switchToLayout(R.id.nav_lines);
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     *  Handle Menu presses
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switchToLayout(item.getItemId());
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //this is used when selecting a pallet in the line view.
    public void ChangeToEditor(int index){
        onSendCommand("RGMV_"+ String.format("%02d",index-1));
        switchToLayout(R.id.nav_editor);
    }


    /*
     *  This will be called when the connection with the machine has been
     *  stablished. Desired behavior is switching to Line status screen.
     */
    public void onLoadingFinished() {
        switchToLayout(R.id.nav_lines);
    }

    public void switchToLayout(int new_id) {
        navigationView.setCheckedItem(new_id);

        /*
         * Automatically enable/disable RPRV commands, so they stop
         * when the user leaves the "Line status" screen, and they start
         * again just before switching back to Line Status
         */
        if(previous_id == R.id.nav_lines) stopRPRV();
        if(new_id == R.id.nav_lines) startRPRV();

        /*
         * Save&Load settings when leaving the Settings screen
         */
        if(previous_id == R.id.nav_settings) settings.saveSettings();

        //Select layouts to change
        ConstraintLayout new_layout = getLayoutByID(new_id);
        ConstraintLayout previous_layout = getLayoutByID(previous_id);
        previous_id = new_id;

        //Update AppBar title to match fragment
        setCurrentTitle(new_id);

        //Switch panes only if they are different
        if(previous_layout.equals(new_layout) == false) {
            doFragmentSwitchAnimation(new_layout, previous_layout);
        }
    }

    public ConstraintLayout getLayoutByID(int id) {
        ConstraintLayout retLayout = (ConstraintLayout) this.findViewById(R.id.holder_loading); //Default

        switch (id) {
            case R.id.nav_lines:
                retLayout = (ConstraintLayout) this.findViewById(R.id.holder_line);
                break;
            case R.id.nav_algorithm:
                retLayout = (ConstraintLayout) this.findViewById(R.id.holder_algorithm);
                break;
            case R.id.nav_editor:
                retLayout = (ConstraintLayout) this.findViewById(R.id.holder_editor);
                break;
            case R.id.nav_logs:
                retLayout = (ConstraintLayout) this.findViewById(R.id.holder_logs);
                break;
            case R.id.nav_alarms:
                retLayout = (ConstraintLayout) this.findViewById(R.id.holder_alarms);
                break;
            case R.id.nav_manual:
                retLayout = (ConstraintLayout) this.findViewById(R.id.holder_manual);
                break;
            case R.id.nav_debug:
                retLayout = (ConstraintLayout) this.findViewById(R.id.holder_debug);
                break;
            case R.id.nav_settings:
                retLayout = (ConstraintLayout) this.findViewById(R.id.holder_settings);
                break;
        }
        return retLayout;
    }

    private void doFragmentSwitchAnimation( ConstraintLayout newLayout, ConstraintLayout oldLayout ) {

        newLayout.setVisibility(ConstraintLayout.VISIBLE);
        oldLayout.setVisibility(ConstraintLayout.VISIBLE);

        //Prepare animations
        ObjectAnimator oldAnimation_x = ObjectAnimator.ofFloat(oldLayout, "x", 0, -oldLayout.getWidth());
        ObjectAnimator oldAnimation_alpha = ObjectAnimator.ofFloat(oldLayout, "alpha", 0);
        ObjectAnimator newAnimation_x = ObjectAnimator.ofFloat(newLayout, "x", newLayout.getWidth() + 100, 0);
        ObjectAnimator newAnimation_alpha = ObjectAnimator.ofFloat(newLayout, "alpha", 1);

        //Run animations
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(oldAnimation_x, newAnimation_x, oldAnimation_alpha, newAnimation_alpha);
        animSetXY.setDuration(TRANSITION_TIME);
        animSetXY.start();
    }

    private void setCurrentTitle(int id) {
        switch (id) {
            case R.id.nav_lines:
                getSupportActionBar().setTitle(getResources().getString(R.string.LineState));
                break;
            case R.id.nav_algorithm:
                getSupportActionBar().setTitle(getResources().getString(R.string.AlgorithmConfiguration));
                break;
            case R.id.nav_editor:
                getSupportActionBar().setTitle(getResources().getString(R.string.PalletEditor));
                break;
            case R.id.nav_logs:
                getSupportActionBar().setTitle(getResources().getString(R.string.ProductionLogs));
                break;
            case R.id.nav_alarms:
                getSupportActionBar().setTitle(getResources().getString(R.string.Alarms));
                break;
            case R.id.nav_manual:
                getSupportActionBar().setTitle(getResources().getString(R.string.ManualControl));
                break;
            case R.id.nav_debug:
                getSupportActionBar().setTitle(getResources().getString(R.string.Debug));
                break;
            case R.id.nav_settings:
                getSupportActionBar().setTitle(getResources().getString(R.string.Settings));
                break;
        }
    }




    /*****************************************************
     *                          --TCP COMMUNICATIONS--
     *****************************************************/
    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground( String... message ) {

            //we create a TCPClient object
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress("cmdreceived", message);
                }

                public void connectionEstablished(){
                    publishProgress("connectionstatechange", "connectionestablished");
                }

                public void connectionLost(){
                    publishProgress("connectionstatechange", "connectionlost");
                }
            });
            mTcpClient.run(getApplicationContext());

            return null;
        }

        /*
         * RBS: Parse information received from publishProgress()
         */
        @Override
        protected void onProgressUpdate( String... values ) {
            super.onProgressUpdate(values);

            if(values[1].contains("Error") && values[0].contains("cmdreceived") ) {
                Log.d("Bad CMD received", values[1]);

            }else if(values[1].contains("RGMV") && values[0].contains("cmdreceived")) {
                editor.onTcpReply(values[1]);

            }else if(values[1].contains("RPRV") && values[0].contains("cmdreceived")){
                line.updateLineBrickInfo(values[1]);
                if(FirstTimeRPRV) {
                    //first RPRV is the trigger to move from the loading screen to the line
                    FirstTimeRPRV=false;
                    onLoadingFinished();
                }
            }else if(values[1].contains("PGSI") && values[0].contains("cmdreceived")){
                debug.updateDebugData(values[1]);

            }else if(values[1].contains("PWDA") && values[0].contains("cmdreceived")){
                manual.serverResponse(values[1], getApplicationContext());

            }else if(values[1].contains("CHAL") && values[0].contains("cmdreceived")){
                //Check for new alarms
                alarms.updateAlarms(mAlarmManager.parseAlarmCMD(values[1]));
                updateAppbarAlarms(mAlarmManager.getCurrentArmState());

            }else if(values[1].contains("PING") && values[0].contains("cmdreceived")){
                TcpClient.timeOuts=0;

            }else if(values[1].contains("connectionestablished") && values[0].contains("connectionstatechange")){
                appbar_connection.setImageResource(R.mipmap.linkup);
                appbar_connection.clearColorFilter();
                onSendCommand("RPRV_10\r\n");

            }else if(values[1].contains("connectionlost") && values[0].contains("connectionstatechange")){
                appbar_connection.setImageResource(R.mipmap.linkdown);
                appbar_connection.setColorFilter(Color.rgb(115, 0, 0));
            }
        }
    }

    public void onSendCommand( String command ) {
        if(mTcpClient!=null) {
            mTcpClient.sendMessage(command);
        }
    }





    /*****************************************************
     *                            --APP BAR ICONS--
     *****************************************************/
    public void updateAppbarAlarms(Boolean[] armState) {
        if(armState[0] == true) {
            equipmentAlarmIcon.setColorFilter(Color.rgb(255, 0, 0), android.graphics.PorterDuff.Mode.MULTIPLY);
        } else {
            equipmentAlarmIcon.clearColorFilter();
        }

        //Armstate indexes 1 to 5, manipulatorAlarmIcon 0 to 4
        for(int i=1; i<6; i++) {
            if(armState[i] == true) {
                manipulatorAlarmIcon[i-1].setColorFilter(Color.rgb(255, 0, 0), android.graphics.PorterDuff.Mode.MULTIPLY);
            } else {
                manipulatorAlarmIcon[i-1].clearColorFilter();
            }
        }
    }




    /*****************************************************
     *                            --PERIODIC TASKS--
     *****************************************************/
    //checks for alarm information to PLC every two secs.
    private void startCheckingForAlarms(){
        Runnable autoUpdater = new Runnable() {
            @Override
            public void run() {
                onSendCommand("CHAL_14\r\n");
                alarmLoopHandler.postDelayed(this, ALARM_CHECK_PERIOD);
            }
        };
        autoUpdater.run();
    }

    final Runnable timer_lines = new Runnable() {
        @Override
        public void run() {
            onSendCommand("RPRV_10\r\n"); //Ask for the UIDs
            if(DoLoops == false) handler.removeCallbacksAndMessages(null);
            else handler.postDelayed(this,RPRV_PERIOD);
        }
    };

    public void startRPRV() {
        handler.postDelayed(timer_lines, RPRV_PERIOD);
        DoLoops=true;
    }

    public void stopRPRV() {
        DoLoops = false;
    }





    /*****************************************************
     *                              --USER EXPERIENCE--
     *****************************************************/
    private void setGUILanguage() {
        SharedPreferences sharedPref = this.getSharedPreferences(this.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        switch(sharedPref.getInt("LANGUAGE", 0)) {
            case 0:
                CurrentLanguage = "en";
                break;
            case 1:
                CurrentLanguage = "es";
                break;
            case 2:
                CurrentLanguage = "zh";
                break;
        }

        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(CurrentLanguage));
        res.updateConfiguration(conf, res.getDisplayMetrics());

        /* RBS April 5th, 2018
         *
         * This ugly and hacky thing is required because of how android works.
         * Changing the locale and restarting will change in-app strings, but titles
         * and menu items won't change unless manually updated
         */
        Menu mainMenu = navigationView.getMenu();
        mainMenu.findItem(R.id.nav_alarms).setTitle(getString(R.string.Alarms));
        mainMenu.findItem(R.id.nav_algorithm).setTitle(getString(R.string.AlgorithmConfiguration));
        mainMenu.findItem(R.id.nav_debug).setTitle(getString(R.string.Debug));
        mainMenu.findItem(R.id.nav_lines).setTitle(getString(R.string.LineState));
        mainMenu.findItem(R.id.nav_settings).setTitle(getString(R.string.Settings));
        mainMenu.findItem(R.id.nav_editor).setTitle(getString(R.string.PalletEditor));
        mainMenu.findItem(R.id.nav_logs).setTitle(getString(R.string.ProductionLogs));
        mainMenu.findItem(R.id.nav_manual).setTitle(getString(R.string.ManualControl));
    }

    /*
     * We haven't redesigned this app yet, so we are running it with the
     * wrong DPI so it looks the same in the simulator
     */
    public void correctDisplayMetrics() {

        DisplayMetrics displayMetrics =  this.getResources().getDisplayMetrics();
        Configuration config = this.getResources().getConfiguration();

        if(displayMetrics.density < 1.3f) {
            displayMetrics.density = 1.33125f;
            config.densityDpi = DisplayMetrics.DENSITY_MEDIUM;
            this.getResources().updateConfiguration(config, displayMetrics);
        }
    }
}

