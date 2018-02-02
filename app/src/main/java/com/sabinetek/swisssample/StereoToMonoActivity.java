package com.sabinetek.swisssample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sabinetek.swiss.Swiss;
import com.sabinetek.swiss.beamformer.StereoToMono;
import com.sabinetek.swiss.packet.parser.AudioDispatcher;

public class StereoToMonoActivity extends Activity {

    private Button mBtnStart;
    private Button mBtnStop;
    private StereoToMono mStereoToMono;

    public static final String TAG = "ReSampleActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resample);
        initView();
    }

    private void initView() {
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStop = (Button) findViewById(R.id.btn_stop);

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStereoToMono = new StereoToMono();
                Swiss.getSwiss().startRecord(new AudioDispatcher.OnAudioReceiver() {
                    @Override
                    public void onReceiveData(byte[] data) {
                        byte[] resample = mStereoToMono.process(data, data.length);
                        Log.d(TAG, "resample length is " + resample.length);
                    }
                });
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Swiss.getSwiss().stopRecord();
                mStereoToMono.close();
            }
        });


    }
}
