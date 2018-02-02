package com.sabinetek.swisssample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sabinetek.ringbuffer.RingBuffer;
import com.sabinetek.swiss.Swiss;
import com.sabinetek.swiss.packet.parser.AudioDispatcher;
import com.sabinetek.swiss.resample.ReSample;

public class ReSampleActivity extends Activity {

    private Button mBtnStart;
    private Button mBtnStop;
    private ReSample mReSample;
    private RingBuffer mRingBuffer = new RingBuffer();
    private SabineAudioRecordThread mSabineAudioRecordThread = new SabineAudioRecordThread();

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
                mRingBuffer.create(1024 * 1024 * 10, 1);
                mReSample = new ReSample(44100, 16000, 2);
                Swiss.getSwiss().startRecord(new AudioDispatcher.OnAudioReceiver() {
                    @Override
                    public void onReceiveData(byte[] data) {
                        mRingBuffer.write(data, data.length);
                    }
                });
                mSabineAudioRecordThread.start();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Swiss.getSwiss().stopRecord();
                mSabineAudioRecordThread.stopThread();
                mReSample.close();
                mRingBuffer.release();
            }
        });

    }

    private class SabineAudioRecordThread extends Thread {

        private boolean keepAlive = true;

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            while (keepAlive) {
                if (mRingBuffer.availableRead() >= 3528) {
                    byte[] read = mRingBuffer.read(3528);
                    byte[] resample = mReSample.resample(read, read.length);
                    Log.d("fucking", resample.length + "");
                }
            }
        }

        public void stopThread() {
            keepAlive = false;
        }
    }
}
