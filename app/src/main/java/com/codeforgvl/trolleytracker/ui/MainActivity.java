package com.codeforgvl.trolleytracker.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.codeforgvl.trolleytracker.BackgroundLocationService;
import com.codeforgvl.trolleytracker.R;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {
    // Global constants
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Global variables
    /*
     */
    PagerAdapter mAdapter;
    ViewPager mPager;

    /*@Override
    public void onTestStart(int testId) {
        Intent intent=new Intent(this, BackgroundLocationService.class);
        intent.putExtra(BackgroundLocationService.KEY_ONE_SHOT, true);
        startService(intent);
    }*/

    Messenger mService = null;
    boolean mIsBound;
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            ServiceFragment diagFrag = (ServiceFragment)mAdapter.getItem(1);
            switch (msg.what) {
                case BackgroundLocationService.MSG_DEBUG_TEXT:
                    Bundle b = msg.getData();
                    //diagFrag.appendLine(b.getString(BackgroundLocationService.KEY_DEBUG_TIMESTAMP), b.getString(BackgroundLocationService.KEY_DEBUG_STRING));
                    break;
                case BackgroundLocationService.MSG_TEST_OVER:
                    //diagFrag.resetButton();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        BackgroundLocationService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(this,
                BackgroundLocationService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            BackgroundLocationService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //enable tabs
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //create fragments, pass to viewpager
        ArrayList<Fragment> fragments = getFragments();
        mAdapter = new PagerAdapter(getSupportFragmentManager(), fragments);
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        //update page when tab is clicked
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };

        //update tab when page is swiped
        mPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

        //init tabs
        //actionBar.addTab(actionBar.newTab().setText("History").setTabListener(tabListener));
        //actionBar.addTab(actionBar.newTab().setText("Diagnostics").setTabListener(tabListener), true);
        actionBar.addTab(actionBar.newTab().setText("Service").setTabListener(tabListener), true);

        //default page
        mPager.setCurrentItem(1);

        doBindService();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!mIsBound){
            doBindService();
        }
        int resCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resCode == ConnectionResult.SERVICE_MISSING ||
           resCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
           resCode == ConnectionResult.SERVICE_DISABLED) {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resCode, this, 1);
            errorDialog.show();
        }
    }

    @Override
    public void onDestroy(){
        doUnbindService();
        super.onDestroy();
    }

    public static class PagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;

        public PagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }
    }

    private ArrayList<Fragment> getFragments(){
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        //fragments.add(new DetailsFragment());
        //fragments.add(new DiagnosticsFragment());
        fragments.add(new ServiceFragment());
        return fragments;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_settings was selected
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                break;
        }

        return true;
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                        break;
                }
        }
    }

}
