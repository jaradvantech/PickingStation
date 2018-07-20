package com.example.administrator.PickingStation;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.Locale;

import static com.example.administrator.PickingStation.AppBarManager.CONNECTED;
import static com.example.administrator.PickingStation.AppBarManager.DISCONNECTED;


public class MainActivity extends AppCompatActivity
        implements
        Line.OnFragmentInteractionListener,
        Algorithm.OnFragmentInteractionListener,
        Editor.OnFragmentInteractionListener,
        Manual.OnFragmentInteractionListener,
        Debug.OnFragmentInteractionListener,
        DebugAdvancedOptions.OnFragmentInteractionListener,
        Alarms.OnFragmentInteractionListener,
        Settings.OnFragmentInteractionListener,
        MachineCalibration.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    private TcpClient mTcpClient;
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
    private int previous_id = R.id.opt_loading;
    private AsyncTask<String, String, TcpClient> networkConnection;
    private final int TRANSITION_TIME = 400;
    private TextView title;
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

        //Start-up configurations
        correctDisplayMetrics();
        setContentView(R.layout.activity_main);
        SettingManager.initSettingManager(this);
        BrickManager.initBrickManager(this);
        AppBarManager.initAppBarManager(this);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        setGUILanguage();

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
         * Notify fragments
         */
        switch (previous_id) {
            case R.id.nav_lines: line.whenLeavingFragment(); break;
            case R.id.nav_algorithm: algorithm.whenLeavingFragment(); break;
            case R.id.nav_editor: editor.whenLeavingFragment();  break;
            case R.id.nav_logs:  logs.whenLeavingFragment(); break;
            case R.id.nav_alarms: alarms.whenLeavingFragment(); break;
            case R.id.nav_manual: manual.whenLeavingFragment(); break;
            case R.id.nav_debug: debug.whenLeavingFragment(); break;
            case R.id.opt_debug_advanced: debug_advanced.whenLeavingFragment(); break;
            case R.id.nav_settings: settings.whenLeavingFragment(); break;
            case R.id.opt_calibration: machineCalibration.whenLeavingFragment(); break;
        }
        switch (new_id) {
            case R.id.nav_lines:  line.whenEnteringFragment(); break;
            case R.id.nav_algorithm: algorithm.whenEnteringFragment(); break;
            case R.id.nav_editor: editor.whenEnteringFragment(); break;
            case R.id.nav_logs: logs.whenEnteringFragment(); break;
            case R.id.nav_alarms: alarms.whenEnteringFragment(); break;
            case R.id.nav_manual: manual.whenEnteringFragment(); break;
            case R.id.nav_debug: debug.whenEnteringFragment(); break;
            case R.id.opt_debug_advanced: debug_advanced.whenEnteringFragment(); break;
            case R.id.nav_settings: settings.whenEnteringFragment(); break;
            case R.id.opt_calibration: machineCalibration.whenEnteringFragment(); break;
        }

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
                    //Log.d("messageReceived", message);
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
                onUpdateConnectionStatus(values[1]);
            }
        }
    }

    public void startNetworking() {
        String ip = SettingManager.getMachineControllerAddress().address;
        String port = SettingManager.getMachineControllerAddress().port;
        networkConnection =  new ConnectTask();
        networkConnection.execute(ip, port);
    }

    public void updateServerAddress() {
        String ip = SettingManager.getMachineControllerAddress().address;
        String port = SettingManager.getMachineControllerAddress().port;
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
            editor.updateBrickInfo(receivedString);

        } else if (cmdID.equals("PGSI")) {
            debug.updateDebugData(receivedString);

        } else if (cmdID.equals("PWDA")) {
            manual.serverResponse(receivedString);
            machineCalibration.onServerResponse(receivedString);

        } else if (cmdID.equals("CHAL")) {
            alarms.updateAlarmLog(alarms.parseAlarmCMD(receivedString));

        } else if (cmdID.equals("GDIS")) {
            debug_advanced.parseInternalStateDebugData(receivedString);

        } else if (cmdID.equals("GCFG")) {
            settings.onSettingsRetrieved(receivedString);

        } else if (cmdID.equals("ALGC")) {
            algorithm.onAlgorithmConfigurationRetrieved(receivedString);

        } else if (cmdID.equals("PING")) {
            //Log.d("PING", "ack");
            TcpClient.ack();
        }
    }

    public void onSendCommand( String command ) {
        if(mTcpClient!=null) {
            mTcpClient.sendMessage(command);
        }
    }



    public void onUpdateConnectionStatus(String command) {
        if (command.equals("connectionestablished")) {
            AppBarManager.updateConnectionIcon(CONNECTED);
            if(previous_id == R.id.opt_loading) {

                onLoadingFinished();
            }
            settings.onEstablishedConnection();
            algorithm.onEstablishedConnection();

        } else if (command.equals("connectionlost")) {
            AppBarManager.updateConnectionIcon(DISCONNECTED);
            settings.onLostConnection();
            algorithm.onLostConnection();
        }
    }


    /*****************************************************
     *                              --USER EXPERIENCE--
     *****************************************************/
    private void setGUILanguage() {
        switch(SettingManager.getLanguage()) {
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

