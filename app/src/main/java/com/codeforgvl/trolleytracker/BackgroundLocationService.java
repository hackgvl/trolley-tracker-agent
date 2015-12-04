package com.codeforgvl.trolleytracker;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

/**
 * BackgroundLocationService used for tracking user locationInfo in the background.
 *
 * @author cblack
 */
public class BackgroundLocationService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();

    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    public static final int MSG_REGISTER_CLIENT = 1;

    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    public static final int MSG_UNREGISTER_CLIENT = 2;

    public static final int MSG_DEBUG_TEXT = 11;
    public static final int MSG_DEBUG_STATUS = 12;


    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
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

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    private BackgroundLocationService btsThis;

    public void sendToClients(String message){
        Time stamp = new Time(Time.getCurrentTimezone());
        stamp.setToNow();

        Bundle b = new Bundle();
        b.putString(KEY_DEBUG_STRING, message);
        b.putString(KEY_DEBUG_TIMESTAMP, stamp.format("%I:%M:%S%P"));

        Message mMessage = Message.obtain(null, MSG_DEBUG_TEXT, 0, 0);
        mMessage.setData(b);

        for (int i=mClients.size() - 1; i>=0; i--){
            try{
                mClients.get(i).send(mMessage);
            } catch (RemoteException e){
                mClients.remove(i);
            }
        }
    }

    public void updateStatus(String message){
        Time stamp = new Time(Time.getCurrentTimezone());
        stamp.setToNow();

        Bundle b = new Bundle();
        b.putString(KEY_DEBUG_STRING, message);
        b.putString(KEY_DEBUG_TIMESTAMP, stamp.format("%I:%M:%S%P"));

        Message mMessage = Message.obtain(null, MSG_DEBUG_STATUS, 0, 0);
        mMessage.setData(b);

        for (int i=mClients.size() - 1; i>=0; i--){
            try{
                mClients.get(i).send(mMessage);
            } catch (RemoteException e){
                mClients.remove(i);
            }
        }
    }

    public static final String KEY_STOP = "com.codeforgvl.trolleytracker.KEY_STOP";
    public static final String KEY_DEBUG_STRING = "com.codeforgvl.trolleytracker.KEY_DEBUG_STRING";
    public static final String KEY_DEBUG_TIMESTAMP = "com.codeforgvl.trolleytracker.KEY_DEBUG_TIMESTAMP";

    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;

    private Boolean servicesAvailable = false;

    private Location location;

    //PendingIntent receiver stuff for location
    private PendingIntent mLocationPendingIntent;



    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        btsThis = this;

        Intent intent = new Intent(this, BackgroundLocationService.class);
        mLocationPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mInProgress = false;

        servicesAvailable = servicesConnected();
        
        /*
         * Create a new locationInfo client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
    }

    private boolean servicesConnected() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;
    }

    public int onStartCommand (Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        Log.d(Constants.LOG_TAG, "Received message...");
        if(!servicesAvailable) {
            Log.e(Constants.LOG_TAG, "Google Play Location Services not available.");
            return START_NOT_STICKY;
        }
        if(mLocationClient == null)
            mLocationClient = new LocationClient(this, this, this);

        Bundle b = intent.getExtras();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();

        //setup location request if this is a true start command
        // Sanity check, ensure background tests are enabled!
        if(!PreferenceManager.getInstance().getBackgroundTestsEnabled(btsThis)){
            //If not, kill service.
            intent.putExtra(KEY_STOP, true);
        }

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL);

        if(intent.hasExtra(LocationClient.KEY_LOCATION_CHANGED)){
            updateStatus("GPS data updated. POSTing to API...");
            location = (Location)b.get(LocationClient.KEY_LOCATION_CHANGED);
            Log.d(Constants.LOG_TAG, location.getLatitude() + ", " + location.getLongitude());

            new HttpLogger().execute(PreferenceManager.getInstance().getServerIP(btsThis),
                    PreferenceManager.getInstance().getTrolleyNumber(btsThis),
                    PreferenceManager.getInstance().getUser(btsThis),
                    PreferenceManager.getInstance().getPassword(btsThis),
                    String.valueOf(location.getLatitude()),
                    String.valueOf(location.getLongitude()));
        }

        if(intent.hasExtra(KEY_STOP))
            stopSelf();

        if(mLocationClient.isConnected() || mInProgress){
            if(intent.hasExtra(KEY_STOP)) {
                //"stop" command received
                Log.d(Constants.LOG_TAG, "Stopping");
                mInProgress = false;
                mLocationClient.removeLocationUpdates(mLocationPendingIntent);
                updateStatus("Stopped.");
            } else if(!mInProgress){
                Log.d(Constants.LOG_TAG, "Requesting location updates");
                updateStatus("Requesting location updates...");
                mLocationClient.requestLocationUpdates(mLocationRequest, mLocationPendingIntent);
            }
            return START_NOT_STICKY;
        } else if (!mLocationClient.isConnecting() && !mInProgress && !intent.hasExtra(KEY_STOP)){
            //must first connect to service before we can use it.
            Log.d(Constants.LOG_TAG, "Initializing location client");
            mInProgress = true;
            mLocationClient.connect();
        }

        return START_NOT_STICKY;
    }

    public Location getLastLocation(){
        return this.location;
    }


    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current locationInfo or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        //start testing
        mLocationClient.requestLocationUpdates(mLocationRequest, mLocationPendingIntent);
    }

    /*
     * Called by Location Services if the connection to the
     * locationInfo client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Turn off the request flag
        mInProgress = false;
        // Destroy the current locationInfo client
        mLocationClient = null;
        // Display the connection status
        // Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        //appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected", Constants.LOG_FILE);
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;
    	
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services mService that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            // If no resolution is available, display an error dialog
        } else {

        }
    }
    public class HttpLogger extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params)
        {
            String server = params[0];
            String trolleyId = params[1];
            String user = params[2];
            String pass = params[3];
            String lat = params[4];
            String lng = params[5];

            try {
                JSONObject jsonObject = new JSONObject();
                String jsonString = "";
                jsonObject.accumulate("lat", lat);
                jsonObject.accumulate("lon", lng);
                jsonString = jsonObject.toString();

                HttpClient c = new DefaultHttpClient();
                String URL = "http://" + server + "/api/v1/trolly/" + trolleyId + "/location";
                HttpPost p = new HttpPost(URL);

                StringEntity se = new StringEntity(jsonString);
                p.setEntity(se);
                p.setHeader("Content-Type", "application/json");

                String credentials = user + ":" + pass;
                String base64 = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                p.setHeader("Authorization", "Basic " + base64);

                HttpResponse r = c.execute(p);

                StatusLine httpResponse = r.getStatusLine();
                String responseText = EntityUtils.toString(r.getEntity());
                if(httpResponse.getStatusCode() == HttpStatus.SC_OK || httpResponse.getStatusCode() == HttpStatus.SC_CREATED){
                    return "Location sent: " + lat + ", " + lng;
                } else {
                    Log.d(Constants.LOG_TAG, httpResponse.getStatusCode() + " " + httpResponse.getReasonPhrase());
                    Log.d(Constants.LOG_TAG, responseText);
                }
                return httpResponse.getStatusCode() + " " + httpResponse.getReasonPhrase();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            sendToClients(message);
            updateStatus("Waiting for next location update...");
        }
    }
}