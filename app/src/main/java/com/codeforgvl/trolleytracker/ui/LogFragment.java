package com.codeforgvl.trolleytracker.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.codeforgvl.trolleytracker.PreferenceManager;
import com.codeforgvl.trolleytracker.R;

/**
 * Created by Adam on 2/13/14.
 */
public class LogFragment extends Fragment{
    private TextView outputText;
    private TextView statusText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        outputText = (TextView) view.findViewById(R.id.text_log);
        outputText.setMovementMethod(new ScrollingMovementMethod());

        statusText = (TextView) view.findViewById(R.id.text_status);
        return view;
    }

    public void onToggleClicked(View view){
        boolean on = ((ToggleButton) view).isChecked();
        PreferenceManager.getInstance().setBackgroundTestsEnabled(on, getActivity());
    }
    /**
     * Changes the contents of the displayed status text.
     * @param s String to set as status text
     */
    public void appendLine(final String t, final String s) {
        if (outputText == null)
            return;
        outputText.append("[" + t + "] " + s + '\n');

        Layout layout = outputText.getLayout();
        if(layout != null) {
            final int scrollAmount = layout.getLineTop(outputText.getLineCount()) - outputText.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0)
                outputText.scrollTo(0, scrollAmount);
            else
                outputText.scrollTo(0, 0);
        }
    }

    public void setStatus(final String t, final String s){
        if (statusText == null)
            return;
        statusText.setText("[" + t + "] " + s + '\n');
    }
}
