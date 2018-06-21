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
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import static com.example.administrator.PickingStation.Commands.CHAL;
import static com.example.administrator.PickingStation.Commands.RPRV;

public class MainActivity extends AppCompatActivity
        implements
        Line.OnFragmentInteractionListener,
        Algorithm.OnFragmentInteractionListener,
        Editor.OnFragmentInteractionListener,
        Manual.OnFragmentInteractionListener,
        Debug.OnFragmentInteractionListener,
        DebugAdvancedOptions.OnFragmentInteractionListener,
        Alarms.OnFragmentInteractionListener,
        MachineCalibration.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    private TcpClient mTcpClient;
    private Boolean FirstTimeRPRV = true;
    private Line line;
    private Algorithm algorithm;
    private Editor editor;
    private Logs logs;
    private Manual manual;
    private Debug debug;
    private DebugAdvancedOptions debug_advanced;
    private Settings settings;
    private Loading loading;
    private Alarms alarms;
    private MachineCalibration machineCalibration;
    private int previous_id = R.id.holder_loading;
    private AsyncTask<String, String, TcpClient> networkConnection;
    private final int TRANSITION_TIME = 400;
    private final String DEFAULT_IP = "127.0.0.1";
    private final String DEFAULT_PORT = "0";
    private final AlarmManager mAlarmManager = new AlarmManager(this);
    private Button appbarTransparentButton;
    private TextView title;
    private ImageView appbar_connection;
    private ImageView manipulatorAlarmIcon[] = new ImageView[5];
    private ImageView equipmentAlarmIcon;
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
        line = new Line();
        algorithm = new Algorithm();
        editor = new Editor();
        logs = new Logs();
        manual = new Manual();
        debug = new Debug();
        debug_advanced = new DebugAdvancedOptions();
        settings = new Settings();
        loading = new Loading();
        alarms = new Alarms();
        machineCalibration = new MachineCalibration();

        //Replace built-in title with custom title
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        title = (TextView) findViewById(R.id.appBar_TextView_Title);
        title.setText(getResources().getString(R.string.Loading));

        /*Configure Navigation Drawer*/
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        // FRAGMENT MANAGEMENT  https://www.youtube.com/watch?v=iksjcQuxtt4
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.holder_line, line, line.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_algorithm, algorithm, algorithm.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_editor, editor, editor.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_logs, logs, logs.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_manual, manual, manual.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_debug, debug, debug.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_debug_advanced, debug_advanced, debug_advanced.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_settings, settings, settings.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_loading, loading, loading.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_alarms, alarms, alarms.getTag()).commit();
        manager.beginTransaction().replace(R.id.holder_machine_calibration, machineCalibration, machineCalibration.getTag()).commit();

        //On any icon pressed in the Appbar, switch to alarm view.
        appbarTransparentButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switchToLayout(R.id.nav_alarms);
            }
        });

        startNetworking();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTcpClient.stopClient();
    }

    @Override
    public void onBackPressed() {

        //If drawer was open, just close drawer
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

         //Otherwise, go to Line Status
        } else {
            //If in sub-screen, return to main screen
            if(previous_id == R.id.opt_debug_advanced) switchToLayout(R.id.nav_debug);

            else {
                switchToLayout(R.id.nav_lines);
            }
        }
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
        try {
            JSONObject RGMVCommand = new JSONObject();
            RGMVCommand.put("command_ID", "RGMV");
            RGMVCommand.put("palletNumber", index);
            onSendCommand(RGMVCommand.toString());
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }

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
        if(previous_id == R.id.nav_lines) line.stopUpdating();
        if(new_id == R.id.nav_lines) line.startUpdating();

        /*
         * Enable/disable autoupdate of debug fragment
         */
        if(previous_id == R.id.opt_debug_advanced) debug_advanced.stopAutoUpdate();
        if(new_id == R.id.opt_debug_advanced) debug_advanced.startAutoUpdate();

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
            case R.id.opt_debug_advanced:
                retLayout = (ConstraintLayout) this.findViewById(R.id.holder_debug_advanced);
                break;
            case R.id.nav_settings:
                retLayout = (ConstraintLayout) this.findViewById(R.id.holder_settings);
                break;
            case R.id.opt_calibration:
                retLayout = (ConstraintLayout) this.findViewById(R.id.holder_machine_calibration);
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
                title.setText(getResources().getString(R.string.LineState));
                break;
            case R.id.nav_algorithm:
                title.setText(getResources().getString(R.string.AlgorithmConfiguration));
                break;
            case R.id.nav_editor:
                title.setText(getResources().getString(R.string.PalletEditor));
                break;
            case R.id.nav_logs:
                title.setText(getResources().getString(R.string.ProductionLogs));
                break;
            case R.id.nav_alarms:
                title.setText(getResources().getString(R.string.Alarms));
                break;
            case R.id.nav_manual:
                title.setText(getResources().getString(R.string.ManualControl));
                break;
            case R.id.nav_debug:
                title.setText(getResources().getString(R.string.Debug));
                break;
            case R.id.opt_debug_advanced:
                //title.setText(getResources().getString(R.string.DebugAdvanced));
                title.setText("Advanced Debug"); //TODO update strings
                break;
            case R.id.nav_settings:
                title.setText(getResources().getString(R.string.Settings));
                break;
            case R.id.opt_calibration:
                title.setText("Machine Calibration");
                break;
        }
    }




    /*****************************************************
     *                          --TCP COMMUNICATIONS--
     *****************************************************/
    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... params) {

            //we create a TCPClient object
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    Log.d("messageReceived", message);
                    publishProgress("cmdreceived", message);
                }

                public void connectionEstablished() {
                    publishProgress("connectionstatechange", "connectionestablished");
                }

                public void connectionLost() {
                    publishProgress("connectionstatechange", "connectionlost");
                }
            });
            mTcpClient.run(params[0], params[1]);

            return null;
        }


        /*
         * RBS: Parse information received from publishProgress()
         */
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            /*
             * We have two types of information here:
             *  values[0]: type of message received
             *  values[1]: the message, either a JSON command or information
             *                  about the state of the TCP connexion
             */
            if (values[0].equals("cmdreceived")) {
                processCommands(values[1]);
            } else if (values[0].equals("connectionstatechange")) {
                updateConnectionStatus(values[1]);
            }
        }
    }

    public void startNetworking() {
        String ip = DEFAULT_IP;
        String port = DEFAULT_PORT;
        //Read IP and address from settings.
        try {
            JSONObject JSONparser = new JSONObject( SettingManager.getSetting("Machine controller", getApplicationContext()));
            ip = JSONparser.getString("ip");
            port = JSONparser.getString("port");
        } catch (Exception jsonExc) {
            Log.e("JSON Exception", jsonExc.getMessage());
        }
        networkConnection =  new ConnectTask();
        networkConnection.execute(ip, port);
    }

    public void updateServerAddress() {
        String ip = DEFAULT_IP;
        String port = DEFAULT_PORT;
        try {
            JSONObject JSONparser = new JSONObject( SettingManager.getSetting("Machine controller", getApplicationContext()));
            ip = JSONparser.getString("ip");
            port = JSONparser.getString("port");
        } catch (Exception jsonExc) {
            Log.e("JSON Exception", jsonExc.getMessage());
        }
        mTcpClient.setAddress(ip, port);
    }

    public void processCommands(String receivedString) {
        String cmdID = "Error";
        try {
            JSONObject JSONparser = new JSONObject(receivedString);
            cmdID = JSONparser.getString("command_ID");

        } catch (JSONException exc) {
            Log.d("MainActivity", exc.getMessage());
        }

        if (cmdID.equals("Error")) {
            Log.d("Bad CMD received", receivedString);

        } else if (cmdID.equals("RGMV")) {
            editor.onTcpReply(receivedString);

        } else if (cmdID.equals("RPRV")) {
            line.updateLineBrickInfo(receivedString);
            if(FirstTimeRPRV) {
                //first RPRV is the trigger to move from the loading screen to the line
                FirstTimeRPRV=false;
                onLoadingFinished();
            }

        } else if (cmdID.equals("PGSI")) {
            debug.updateDebugData(receivedString);

        } else if (cmdID.equals("PWDA")) {
            manual.serverResponse(receivedString);

        } else if (cmdID.equals("CHAL")) {
            //Check for new alarms
            alarms.updateAlarms(mAlarmManager.parseAlarmCMD(receivedString));
            updateAppbarAlarms(mAlarmManager.getCurrentArmState());

        } else if (cmdID.equals("GDIS")) {
            debug_advanced.parseInternalStateDebugData(receivedString);

        } else if (cmdID.equals("PING")) {
            Log.d("PING", "ack");
            TcpClient.ack();
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

    public void updateConnectionStatus(String command) {
        if (command.equals("connectionestablished")) {
            appbar_connection.setImageResource(R.mipmap.linkup);
            appbar_connection.clearColorFilter();
            onSendCommand(RPRV);

        } else if (command.equals("connectionlost")) {
            appbar_connection.setImageResource(R.mipmap.linkdown);
            appbar_connection.setColorFilter(Color.rgb(115, 0, 0));
        }
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
        //RBS TODO Add calibration and advanced
    }

    /* RBS April 19th, 2018
     *
     * One more workaround (duh)
     * This crappy Android tablet has the wrong density value programmed
     * It should be aprox dens = 1 and DPI=160 since it is a 143dpi screen.
     * However someone decided to change this to 1.33125 at some point,
     * fucking everything up. In order to get this layout to match the one in the
     * AndroidStudio editor, this has to be reverted.
     */
    public void correctDisplayMetrics() {
        DisplayMetrics displayMetrics =  this.getResources().getDisplayMetrics();
        Configuration config = this.getResources().getConfiguration();

        if(displayMetrics.density != 1) {
            displayMetrics.density = 1;
            config.densityDpi = 160;
            this.getResources().updateConfiguration(config, displayMetrics);
        }
    }
}

