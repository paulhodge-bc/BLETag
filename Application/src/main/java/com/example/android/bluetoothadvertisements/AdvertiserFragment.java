/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothadvertisements;

import android.bluetooth.le.AdvertiseCallback;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Allows user to start & stop Bluetooth LE Advertising of their device.
 */
public class AdvertiserFragment extends Fragment implements View.OnClickListener {

    /**
     * Lets user toggle BLE Advertising.
     */
    private Switch mSwitch;

    /**
     * Listens for notifications that the {@code AdvertiserService} has failed to start advertising.
     * This Receiver deals with Fragment UI elements and only needs to be active when the Fragment
     * is on-screen, so it's defined and registered in code instead of the Manifest.
     */
    private AdvertiserService bleService;
    private BroadcastReceiver advertisingFailureReceiver;
    private Timer buttonTimer = new Timer();
    private boolean isUpHeld = false;
    private boolean isDownHeld = false;
    private boolean isAdvertising = false;
    private boolean isBound = false;
    private boolean isBothFinished = false;
    private int btnBothCnt = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        advertisingFailureReceiver = new BroadcastReceiver() {

            /**
             * Receives Advertising error codes from {@code AdvertiserService} and displays error messages
             * to the user. Sets the advertising toggle to 'false.'
             */
            @Override
            public void onReceive(Context context, Intent intent) {

                int errorCode = intent.getIntExtra(AdvertiserService.ADVERTISING_FAILED_EXTRA_CODE, -1);

                mSwitch.setChecked(false);

                String errorMessage = getString(R.string.start_error_prefix);
                switch (errorCode) {
                    case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                        errorMessage += " " + getString(R.string.start_error_already_started);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                        errorMessage += " " + getString(R.string.start_error_too_large);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                        errorMessage += " " + getString(R.string.start_error_unsupported);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                        errorMessage += " " + getString(R.string.start_error_internal);
                        break;
                    case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                        errorMessage += " " + getString(R.string.start_error_too_many);
                        break;
                    case AdvertiserService.ADVERTISING_TIMED_OUT:
                        errorMessage = " " + getString(R.string.advertising_timedout);
                        break;
                    default:
                        errorMessage += " " + getString(R.string.start_error_unknown);
                }

                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
            }
        };


        ActionBar BLEActionBar = ((MainActivity)getActivity()).getSupportActionBar();
        BLEActionBar.setTitle("Proximity Tag");
    }

    private class buttonTask extends TimerTask {
        @Override
        public void run() {
            if (!isAdvertising && !bleService.busy) {
                if (isUpHeld && isDownHeld) {
                    // Wait until both buttons have been held for 2 seconds
                    if (btnBothCnt > 4 && !isBothFinished) {
                        dim(3);
                        btnBothCnt = 0;
                        isBothFinished = true;
                    } else if (!isBothFinished) {
                        btnBothCnt++;
                    }
                } else {
                    if (isUpHeld) {
                        dim(1);
                    } else if (isDownHeld) {
                        dim(2);
                    }
                    btnBothCnt = 0;
                    isBothFinished = false;
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_advertiser, container, false);

        mSwitch = (Switch) view.findViewById(R.id.advertise_switch);
        mSwitch.setOnClickListener(this);
        TextView tagText = (TextView) view.findViewById(R.id.txt_tag_id);
        String tagIDString = Constants.tagID_MSB + Integer.toString(Constants.tagID_LSB);
        tagText.setText(tagIDString);

        final Button btn_up = (Button) view.findViewById(R.id.btn_up);
//        btn_up.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
////                dim(1);
////                isUpHeld = true;
//
//            }
//        });
        btn_up.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP ||
                    e.getAction() == MotionEvent.ACTION_CANCEL) {
                    isUpHeld = false;
                }
                else if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    dim(1);
                    isUpHeld = true;
                }
//                dim(1);
                return isUpHeld;
            }
        });

        final Button btn_down = (Button) view.findViewById(R.id.btn_down);
//        btn_down.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
////                dim(-1);
//                isDownHeld = true;
//            }
//        });
        btn_down.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP ||
                        e.getAction() == MotionEvent.ACTION_CANCEL) {
                    isDownHeld = false;
                }
                else if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    dim(2);
                    isDownHeld = true;
                }
//                dim(-1);
                return isDownHeld;
            }
        });

        return view;
    }

    /**
     * When app comes on screen, check if BLE Advertisements are running, set switch accordingly,
     * and register the Receiver to be notified if Advertising fails.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (AdvertiserService.running) {
            Context c = getContext();
            Intent ad_intent = getServiceIntent(c);
            c.bindService(ad_intent, bleConnection, Context.BIND_AUTO_CREATE);
            mSwitch.setChecked(true);
            this.getView().findViewById(R.id.btn_up).setEnabled(true);
            this.getView().findViewById(R.id.btn_down).setEnabled(true);
        } else {
            mSwitch.setChecked(false);
            this.getView().findViewById(R.id.btn_up).setEnabled(false);
            this.getView().findViewById(R.id.btn_down).setEnabled(false);
        }

        IntentFilter failureFilter = new IntentFilter(AdvertiserService.ADVERTISING_FAILED);
        getActivity().registerReceiver(advertisingFailureReceiver, failureFilter);

    }

    /**
     * When app goes off screen, unregister the Advertising failure Receiver to stop memory leaks.
     * (and because the app doesn't care if Advertising fails while the UI isn't active)
     */
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(advertisingFailureReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        getContext().unbindService(bleConnection);
        isBound = false;
    }

    /**
     * Returns Intent addressed to the {@code AdvertiserService} class.
     */
    private static Intent getServiceIntent(Context c) {
        Intent tempIntent = new Intent(c, AdvertiserService.class);
        tempIntent.putExtra("btn","00");
        return tempIntent;
    }

    /**
     * Called when switch is toggled - starts or stops advertising.
     */
    @Override
    public void onClick(View v) {
        // Is the toggle on?
        boolean on = ((Switch) v).isChecked();

        if (on) {
            startAdvertising();
            this.getView().findViewById(R.id.btn_up).setEnabled(true);
            this.getView().findViewById(R.id.btn_down).setEnabled(true);
        } else {
            stopAdvertising();
            this.getView().findViewById(R.id.btn_up).setEnabled(false);
            this.getView().findViewById(R.id.btn_down).setEnabled(false);
        }
    }

    /**
     * Starts BLE Advertising by starting {@code AdvertiserService}.
     */
    private void startAdvertising() {
        Context c = getContext();
        Intent ad_intent = getServiceIntent(c);
        c.startService(ad_intent);
        c.bindService(ad_intent, bleConnection, Context.BIND_AUTO_CREATE);
        mSwitch.setChecked(true);
    }

    /**
     * Stops BLE Advertising by stopping {@code AdvertiserService}.
     */
    private void stopAdvertising() {
        Context c = getContext();
        c.unbindService(bleConnection);
        c.stopService(getServiceIntent(c));
    }


    private void dim(int dim) {
        isAdvertising = true;

        if (!isBound) {
//            Context c = getContext();
//            Intent ad_intent = getServiceIntent(c);
//            c.bindService(ad_intent, bleConnection, Context.BIND_AUTO_CREATE);
        } else {
//        if (isBound) {
            bleService.dim(dim);
//        }
        }

        isAdvertising = false;
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection bleConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AdvertiserService.BLEBinder binder = (AdvertiserService.BLEBinder) service;
            bleService = binder.getService();
            isBound = true;
            buttonTimer.scheduleAtFixedRate(new buttonTask(), Constants.buttonTimerDelay, Constants.buttonTimerPeriod);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };
}