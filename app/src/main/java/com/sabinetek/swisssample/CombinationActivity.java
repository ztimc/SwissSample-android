package com.sabinetek.swisssample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.sabinetek.ringbuffer.RingBuffer;
import com.sabinetek.sabinekit.utils.FileUtils;
import com.sabinetek.sabinekit.voice.wav.Wav;
import com.sabinetek.swiss.Swiss;
import com.sabinetek.swiss.beamformer.StereoToMono;
import com.sabinetek.swiss.packet.parser.AudioDispatcher;
import com.sabinetek.swiss.resample.ReSample;

import java.io.IOException;

public class CombinationActivity extends Activity {

    public static final String TAG = "CombinationActivity";

    private StereoToMono mStereoToMono;
    private ReSample mReSample;
    private RingBuffer mRingBuffer;
    private SabineAudioRecordThread mSabineAudioRecordThread;

    //双声道    44100采样率
    private Wav stereoPcm;
    //单声道    44100采样率
    private Wav monoPcm44;
    //单声道    16000采样率
    private Wav monoPcm16;

    private Button mBtnStart;
    private Button mBtnStop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combination);

        initView();
    }

    private void initView() {
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStop = (Button) findViewById(R.id.btn_stop);

        mRingBuffer = new RingBuffer();
        mSabineAudioRecordThread = new SabineAudioRecordThread();

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stereoPcm = new Wav(44100,
                        2,
                        16,
                        FileUtils.createTempFile(getApplicationContext(),
                                ".44_2_pcm"));

                monoPcm44 = new Wav(44100,
                        1,
                        16,
                        FileUtils.createTempFile(getApplicationContext(),
                                ".44_1_pcm"));

                monoPcm16 = new Wav(16000,
                        1,
                        16,
                        FileUtils.createTempFile(getApplicationContext(),
                                ".16_1_pcm"));
                mStereoToMono = new StereoToMono();
                mReSample = new ReSample(44100, 16000, 1);
                mRingBuffer.create(1024 * 1024 * 10, 1);
                Swiss.getSwiss().startRecord(new AudioDispatcher.OnAudioReceiver() {
                    @Override
                    public void onReceiveData(byte[] data) {
                        //分两部完成 1转换单声道 2重采样
                        try {
                            //原始音频存储
                            stereoPcm.write(data, data.length);
                            Log.d(TAG, "write 44100 2 length is " + data.length);
                            //转单声道
                            byte[] monoPcm = mStereoToMono.process(data, data.length);
                            monoPcm44.write(monoPcm);
                            Log.d(TAG, "write 44100 1 length is " + monoPcm.length);
                            //重采样,先存入ringBuff,再按照44100,20ms大小取，再重采样
                            mRingBuffer.write(monoPcm, monoPcm.length);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                mSabineAudioRecordThread.start();
            }
        });


        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSabineAudioRecordThread.stopThread();
                Swiss.getSwiss().stopRecord();
                mReSample.close();
                mStereoToMono.close();
            }
        });
    }


    private class SabineAudioRecordThread extends Thread {

        private boolean keepAlive = true;

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            while (keepAlive) {
                //1764 是44100采样率，单声道，每20毫米帧大小
                if (mRingBuffer.availableRead() >= 1764) {
                    byte[] read = mRingBuffer.read(1764);
                    byte[] resample = mReSample.resample(read, read.length);
                    Log.d(TAG, resample.length + "");
                    try {
                        monoPcm16.write(resample, resample.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void stopThread() {
            keepAlive = false;
        }
    }
}
