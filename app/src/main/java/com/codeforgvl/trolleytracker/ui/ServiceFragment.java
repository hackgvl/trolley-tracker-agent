package com.codeforgvl.trolleytracker.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.codeforgvl.trolleytracker.BackgroundLocationService;
import com.codeforgvl.trolleytracker.R;

/**
 * Created by Adam on 2/13/14.
 */
public class ServiceFragment extends Fragment{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateStatusLbl(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service, container, false);
        TextView textView = (TextView) view.findViewById(R.id.textStatus);
        textView.setText("Start and stop service");

        Button startButton = (Button) view.findViewById(R.id.buttonStart);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start(v);
            }
        });
        Button stopButton = (Button) view.findViewById(R.id.buttonStop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop(v);
            }
        });

        return view;
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.codeforgvl.trolleytracker.BackgroundLocationService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public void updateStatusLbl(boolean stopped){
        TextView runningLbl=(TextView)this.getView().findViewById(R.id.textStatus);
        if(stopped || !isMyServiceRunning()){
            runningLbl.setText("Stopped");
            runningLbl.setTextColor(Color.RED);
        }
        else{
            runningLbl.setText("Running");
            runningLbl.setTextColor(Color.GREEN);
        }
    }
    public void start(View view){
        if(!isMyServiceRunning()){
            Intent intent=new Intent(this.getActivity(), BackgroundLocationService.class);
            //intent.putExtra(BackgroundLocationService.KEY_ONE_SHOT, false);
            getActivity().startService(intent);
        }
        updateStatusLbl(false);
    }

    public void stop(View view){
        if(isMyServiceRunning()){
            Intent intent=new Intent(this.getActivity(), BackgroundLocationService.class);
            intent.putExtra(BackgroundLocationService.KEY_STOP, true);
            getActivity().startService(intent);
            //PendingIntent restartServiceIntent = PendingIntent.getService(getApplicationContext(), 0, serviceIntent,0);
            //AlarmManager alarms = (AlarmManager)getSystemService(ALARM_SERVICE);
            // cancel previous alarm
            //alarms.cancel(restartServiceIntent);
        }
        updateStatusLbl(true);
    }}
