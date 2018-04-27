package com.sabinetek.swisssample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.sabinetek.sabinekit.utils.FileUtils;
import com.sabinetek.sabinekit.voice.wav.Wav;
import com.sabinetek.swiss.Swiss;
import com.sabinetek.swiss.beamformer.StereoToMono;
import com.sabinetek.swiss.bluetooth.BluetoothHelper;
import com.sabinetek.swiss.bluetooth.Constant;
import com.sabinetek.swiss.bluetooth.State;
import com.sabinetek.swiss.devices.DeviceType;
import com.sabinetek.swiss.devices.DevicesInfo;
import com.sabinetek.swiss.packet.PacketFormat;
import com.sabinetek.swiss.packet.parser.AudioDispatcher;
import com.sabinetek.swiss.resample.ReSample;
import com.sabinetek.swiss.update.FirmwareUpdateListener;
import com.sabinetek.swiss.update.FirmwareUpdater;
import com.sabinetek.swiss.util.LogUtils;
import com.sabinetek.swiss.util.ResourcesUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {


    private TextView mDeviceStateTv;
    private Button mStartRecoderBt;
    private Button mStopRecoderBt;
    private Button mStartUpdate;
    private TextView mMicGianTv;
    private SeekBar mMicGainSb;


    private TextView mMonitorVolumeTv;
    private SeekBar mMonitorValueSb;

    private ToggleButton mAgcTb;
    private Button mAnsReduceBt;
    private TextView mAnsValueTv;
    private Button mAnsPlusBt;
    private TextView mRevbRatioTv;
    private SeekBar mRevbRatioSb;
    private TextView mMusicMixerTv;
    private Toolbar mToolbar;
    private Button mAutoClick;
    private CheckBox mDevocalOnOff;
    private TextView mDevocalTv;
    private ToggleButton mMusicMixTb;
    private static String STATE;

    //场景模式
    private RadioGroup mSceneModeRg;
    private RadioButton mSceneModeRb0;
    private RadioButton mSceneModeRb1;
    private RadioButton mSceneModeRb2;
    private RadioButton mSceneModeRb3;


    //音频信息
    private int channelCount = 2; //通道数
    private int sampleRate = 44100;//采样率
    private long audioPcmDataLen = 0;// 录制时音频pcm数据的总长度
    private long audioTimestamp; // 计算音频的时间队列

    private TextView textView;

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!"STATE_CONNECTED".equals(STATE)) Constant.reconnectDevice();
            }
        }, 500);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {

        textView = findViewById(R.id.undata_text);
        mSceneModeRg = findViewById(R.id.devocal_mode_rg);
        mSceneModeRb0 = findViewById(R.id.devocal_mode_rb0);
        mSceneModeRb1 = findViewById(R.id.devocal_mode_rb1);
        mSceneModeRb2 = findViewById(R.id.devocal_mode_rb2);
        mSceneModeRb3 = findViewById(R.id.devocal_mode_rb3);

        mMusicMixTb = findViewById(R.id.music_mixer_tb);
        mDevocalTv = findViewById(R.id.devocal_mode_tz);
        mDevocalOnOff = findViewById(R.id.btn_devocal_on_off);
        mAutoClick = findViewById(R.id.btn_auto_click);
        mStartUpdate = findViewById(R.id.start_update);
        mDeviceStateTv = (TextView) findViewById(R.id.device_state_tv);
        mStartRecoderBt = (Button) findViewById(R.id.start_recode_bt);
        mStopRecoderBt = (Button) findViewById(R.id.stop_recode_bt);

        mMicGianTv = (TextView) findViewById(R.id.mic_gian_tv);
        mMicGainSb = (SeekBar) findViewById(R.id.mic_gain_sb);
        mMonitorVolumeTv = (TextView) findViewById(R.id.monitor_volume_tv);
        mMonitorValueSb = (SeekBar) findViewById(R.id.monitor_value_sb);
        mAgcTb = (ToggleButton) findViewById(R.id.agc_tb);
        mAnsReduceBt = (Button) findViewById(R.id.ans_reduce_bt);
        mAnsValueTv = (TextView) findViewById(R.id.ans_value_tv);
        mAnsPlusBt = (Button) findViewById(R.id.ans_plus_bt);
        mRevbRatioTv = (TextView) findViewById(R.id.revb_ratio_tv);
        mRevbRatioSb = (SeekBar) findViewById(R.id.revb_ratio_sb);
        mMusicMixerTv = (TextView) findViewById(R.id.music_mixer_tv);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mAutoClick.setOnClickListener(this);
        mStartUpdate.setOnClickListener(this);

        mStartRecoderBt.setOnClickListener(this);
        mStopRecoderBt.setOnClickListener(this);

        mMicGainSb.setOnSeekBarChangeListener(this);
        mMonitorValueSb.setOnSeekBarChangeListener(this);
        mRevbRatioSb.setOnSeekBarChangeListener(this);
        findViewById(R.id.start_stereo_to_mono_bt).setOnClickListener(this);
        findViewById(R.id.start_resample_bt).setOnClickListener(this);

        mMusicMixTb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Swiss.getSwiss().setDevocalMusicMixer(isChecked);
            }
        });

        mDevocalOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Swiss.getSwiss().setVoiceOfPeople(isChecked);
            }
        });

        mAgcTb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Swiss.getSwiss().setAgcEnable(b);
                mMicGainSb.setEnabled(!b);
            }
        });

        mSceneModeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.devocal_mode_rb0:
                        Swiss.getSwiss().setDspSigFlowBitmap(0);
                        break;
                    case R.id.devocal_mode_rb1:
                        Swiss.getSwiss().setDspSigFlowBitmap(1);
                        break;
                    case R.id.devocal_mode_rb2:
                        Swiss.getSwiss().setDspSigFlowBitmap(2);
                        break;
                    case R.id.devocal_mode_rb3:
                        Swiss.getSwiss().setDspSigFlowBitmap(3);
                        break;
                }
            }
        });

        AnsClickListener ansClickListener = new AnsClickListener();
        mAnsPlusBt.setOnClickListener(ansClickListener);
        mAnsReduceBt.setOnClickListener(ansClickListener);


        Swiss.getSwiss().setOnStateChangeListener(new BluetoothHelper.OnStateChangeListener() {
            @Override
            public void onStateChange(final State state) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        STATE = state.toString();
                        mDeviceStateTv.setText(state.toString());
                    }
                });
                if (state == State.STATE_CONNECTED) {
                    mToolbar.post(new Runnable() {
                        @Override
                        public void run() {
                            mMicGianTv.setText(ResourcesUtils.getString(R.string.mic_gian) + Swiss.getSwiss().getMicVolume());
                            mMicGainSb.setProgress(Swiss.getSwiss().getMicVolume());
                            mMonitorVolumeTv.setText(ResourcesUtils.getString(R.string.monitor_volume) + Swiss.getSwiss().getMonitorVolume());
                            mMonitorValueSb.setProgress(Swiss.getSwiss().getMonitorVolume());
                            mRevbRatioTv.setText(ResourcesUtils.getString(R.string.revb_ratio_value) + Swiss.getSwiss().getReverberationRatio());
                            mRevbRatioSb.setProgress(Swiss.getSwiss().getReverberationRatio());
                            boolean auto = Swiss.getSwiss().getDevocalClick();
                            if (auto) {
                                mAutoClick.setBackgroundColor(Color.GREEN);
                            } else {
                                mAutoClick.setBackgroundColor(Color.WHITE);
                            }
                            mDevocalOnOff.setChecked(Swiss.getSwiss().getVoiceOfPeople() == 0);
                            PacketFormat.DevocalMode mode = Swiss.getSwiss().getDevocalMode();
                            switch (mode) {
                                case DSP_MODE_NORMAL:  //默认模式
                                    mSceneModeRb0.setChecked(true);
                                    mSceneModeRb1.setChecked(false);
                                    mSceneModeRb2.setChecked(false);
                                    mSceneModeRb3.setChecked(false);
                                    break;
                                case DSP_MODE_DRY:  //干音模式
                                    mSceneModeRb0.setChecked(false);
                                    mSceneModeRb1.setChecked(true);
                                    mSceneModeRb2.setChecked(false);
                                    mSceneModeRb3.setChecked(false);
                                    break;
                                case DSP_MODE_SING_ALONG:  //跟唱模式
                                    mSceneModeRb0.setChecked(false);
                                    mSceneModeRb1.setChecked(false);
                                    mSceneModeRb2.setChecked(true);
                                    mSceneModeRb3.setChecked(false);
                                    break;
                                case DSP_MODE_CANTATA:  //清唱模式
                                    mSceneModeRb0.setChecked(false);
                                    mSceneModeRb1.setChecked(false);
                                    mSceneModeRb2.setChecked(false);
                                    mSceneModeRb3.setChecked(true);
                                    break;

                            }


                            /*   mDevocalTv.setText(ResourcesUtils.getString(R.string.mode) + mode.name());
                            mDevocalSb.setProgress(mode.getCode());*/
                            mMusicMixTb.setChecked(Swiss.getSwiss().getMusicMixer());
                        }
                    });
                }
            }
        });

        DevicesInfo.getInstance().setOnDeviceInfoListener(new DevicesInfo.OnDeviceInfoListener() {
            @Override
            public void onSuccess(DeviceType type) {
                //这里回掉之后，就能获取设备信息
                String info = "";
                if (type == DeviceType.MEDIA_INFO) {
                    info = "device_protocol " + DevicesInfo.getInstance().getProtocolVersion() + "\n" +
                            "device_firmware  " + DevicesInfo.getInstance().getFirmwareVersion() + "\n" +
                            "device_hardware  " + DevicesInfo.getInstance().getHardwareVersion() + "\n" +
                            "device_codec " + DevicesInfo.getInstance().getCodecVersion() + "\n" +
                            "device_manufacture " + DevicesInfo.getInstance().getManufacture() + "\n" +
                            "device_licensed " + DevicesInfo.getInstance().getDeviceLicensed() + "\n";
                } else if (type == DeviceType.VERSION_INFO) {
                    info = "sampleRate " + DevicesInfo.getInstance().getSampleRates() + "\n" +
                            "channel  " + DevicesInfo.getInstance().getChannel() + "\n";
                }
                Log.d("swiss", info);
            }
        });

    }

    // 录制时间轴
    private void upDateTimestamp(int pcmLen) {
        audioPcmDataLen = audioPcmDataLen + pcmLen;
        audioTimestamp = (long) ((double) audioPcmDataLen / (channelCount * sampleRate * 2.0));
    }

    private String getTime() {
        String time = "";
        if (audioTimestamp < 10) {
            time = "00:00:0" + audioTimestamp;
        } else if (audioTimestamp < 60) {
            time = "00:00:" + audioTimestamp;
        } else if (audioTimestamp / 60 < 10) {
            if (audioTimestamp % 60 < 10) {
                time = "00:0" + audioTimestamp / 60 + ":0" + audioTimestamp % 60;
            } else {
                time = "00:0" + audioTimestamp / 60 + ":" + audioTimestamp % 60;
            }
        } else if (audioTimestamp / 60 < 60) {
            if (audioTimestamp % 60 < 10) {
                time = "00:" + audioTimestamp / 60 + ":0" + audioTimestamp % 60;
            } else {
                time = "00:" + audioTimestamp / 60 + ":" + audioTimestamp % 60;
            }
        } else if (audioTimestamp / 3600 < 10) {
            if ((audioTimestamp % 60) / 60 < 10) {
                if (audioTimestamp % 60 % 60 < 10) {
                    time = "0" + audioTimestamp / 3600 + ":0" + audioTimestamp % 60 + ":0" + audioTimestamp % 60 % 60;
                } else {
                    time = "0" + audioTimestamp / 3600 + ":0" + audioTimestamp % 60 + ":" + audioTimestamp % 60 % 60;
                }
            } else {
                if (audioTimestamp % 60 % 60 < 60) {
                    time = "0" + audioTimestamp / 3600 + ":" + audioTimestamp % 60 + ":0" + audioTimestamp % 60 % 60;
                } else {
                    time = "0" + audioTimestamp / 3600 + ":" + audioTimestamp % 60 + ":" + audioTimestamp % 60 % 60;
                }
            }
        } else if (audioTimestamp / 3600 < 24) {
            if ((audioTimestamp % 60) / 60 < 10) {
                if (audioTimestamp % 60 % 60 < 10) {
                    time = audioTimestamp / 3600 + ":0" + audioTimestamp % 60 + ":0" + audioTimestamp % 60 % 60;
                } else {
                    time = audioTimestamp / 3600 + ":0" + audioTimestamp % 60 + ":" + audioTimestamp % 60 % 60;
                }
            } else {
                if (audioTimestamp % 60 % 60 < 60) {
                    time = audioTimestamp / 3600 + ":" + audioTimestamp % 60 + ":0" + audioTimestamp % 60 % 60;
                } else {
                    time = audioTimestamp / 3600 + ":" + audioTimestamp % 60 + ":" + audioTimestamp % 60 % 60;
                }
            }
        } else {
            time = audioTimestamp / (3600 * 24) + "天" + audioTimestamp % 24 + "时";
        }

        return time;
    }


    private class AnsClickListener implements View.OnClickListener {
        private int ansValue = 0;

        @Override
        public void onClick(View view) {
            int tag = view.getId();
            switch (tag) {
                case R.id.ans_reduce_bt:
                    if (ansValue > 0) {
                        ansValue--;
                        mAnsValueTv.setText(ansValue + "");
                    }
                    Swiss.getSwiss().setAns(ansValue);
                    break;
                case R.id.ans_plus_bt:
                    if (ansValue < 3) {
                        ansValue++;
                        mAnsValueTv.setText(ansValue + "");
                    }
                    Swiss.getSwiss().setAns(ansValue);
                    break;

            }
        }
    }

    private StereoToMono mStereoToMono;
    private ReSample mReSample;

    //双声道    44100采样率
    private Wav stereoPcm;
    //单声道    44100采样率
    private Wav monoPcm44;
    //单声道    16000采样率
    private Wav monoPcm16;

    @Override
    public void onClick(final View view) {

        switch (view.getId()) {
            case R.id.start_stereo_to_mono_bt:
                Intent intent = new Intent(MainActivity.this, StereoToMonoActivity.class);
                startActivity(intent);
                break;
            case R.id.start_resample_bt:
                Intent intent2 = new Intent(MainActivity.this, ReSampleActivity.class);
                startActivity(intent2);
                break;
            case R.id.start_update:
                Toast.makeText(MainActivity.this, "开始升级", Toast.LENGTH_SHORT).show();

                File tempFile = FileUtils.createTempFile(getApplicationContext(),
                        ".file");
                copyFilesFassets(this,
                        "OTA_8670_0123_OTATEST.dfu",
                        tempFile.getAbsolutePath());

                FirmwareUpdater.getInstance().upFirmware(tempFile, new FirmwareUpdateListener() {
                    @Override
                    public void onUpdate(final int progress) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText("升级中" + progress + "%");
                            }
                        });
                        LogUtils.e("onUpdata..." + progress);
                    }

                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText("升级成功");
                                Toast.makeText(MainActivity.this, "升级成功", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                    @Override
                    public void onError() {

                    }
                });
                break;
            case R.id.start_recode_bt:
                audioPcmDataLen = 0;
                Swiss.getSwiss().startRecord(new AudioDispatcher.OnAudioReceiver() {
                    @Override
                    public void onReceiveData(byte[] data) {
                            if (data == null) return;
                            upDateTimestamp(data.length);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mStartRecoderBt.setText(getTime());
                                }
                            });
                    }
                });
                break;
            case R.id.stop_recode_bt:
                mStartRecoderBt.setText(getString(R.string.start_recode));
                Swiss.getSwiss().stopRecord();


                break;
            case R.id.btn_auto_click:
                boolean auto = !Swiss.getSwiss().getDevocalClick();
                Swiss.getSwiss().setDevocalClick(auto);
                if (auto) {
                    mAutoClick.setBackgroundColor(Color.GREEN);
                } else {
                    mAutoClick.setBackgroundColor(Color.WHITE);
                }
                break;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (seekBar == mMicGainSb) {
            mMicGianTv.setText(ResourcesUtils.getString(R.string.mic_gian) + i + "");
            Swiss.getSwiss().setMicVolume(i);
        } else if (seekBar == mMonitorValueSb) {
            mMonitorVolumeTv.setText(ResourcesUtils.getString(R.string.monitor_volume) + i + "");
            Swiss.getSwiss().setMonitorVolume(i);
        } else if (seekBar == mRevbRatioSb) {
            mRevbRatioTv.setText(ResourcesUtils.getString(R.string.revb_ratio_value) + i + "");
            Swiss.getSwiss().setReverberationRatio(i);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 从assets目录中复制整个文件夹内容
     *
     * @param context Context 使用CopyFiles类的Activity
     * @param oldPath String  原文件路径  如：/aa
     * @param newPath String  复制后路径  如：xx:/bb/cc
     */
    public void copyFilesFassets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFassets(context, oldPath + File.separator + fileName, newPath + File.separator + fileName);
                }
            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

