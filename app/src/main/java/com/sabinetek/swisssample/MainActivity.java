package com.sabinetek.swisssample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sabinetek.sabinekit.utils.FileUtils;
import com.sabinetek.swiss.Swiss;
import com.sabinetek.swiss.bluetooth.BluetoothHelper;
import com.sabinetek.swiss.bluetooth.State;
import com.sabinetek.swiss.devices.DeviceType;
import com.sabinetek.swiss.devices.DevicesInfo;
import com.sabinetek.swiss.packet.parser.AudioDispatcher;
import com.sabinetek.swiss.update.FirmwareUpdateListener;
import com.sabinetek.swiss.update.FirmwareUpdater;
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

    private TextView mSpeakerVolumeTv;
    private SeekBar mSpeakerVolumeSb;

    private TextView mMonitorVolumeTv;
    private SeekBar mMonitorValueSb;

    private ToggleButton mAgcTb;
    private Button mAnsReduceBt;
    private TextView mAnsValueTv;
    private Button mAnsPlusBt;
    private Button mRevbTypeSubBt;
    private TextView mRevbValueTv;
    private Button mRevbTypeAddBt;
    private TextView mRevbRatioTv;
    private SeekBar mRevbRatioSb;
    private TextView mMusicMixerTv;
    private SeekBar mMusicMixerSb;
    private TextView mTextDevocal;
    private Button mBtnClose;
    private Button mBtnOpen;
    private Button mBtnDevocalLeft;
    private Button mBtnDevocalRight;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {

        mStartUpdate = findViewById(R.id.start_update);
        mDeviceStateTv = (TextView) findViewById(R.id.device_state_tv);
        mStartRecoderBt = (Button) findViewById(R.id.start_recode_bt);
        mStopRecoderBt = (Button) findViewById(R.id.stop_recode_bt);

        mMicGianTv = (TextView) findViewById(R.id.mic_gian_tv);
        mMicGainSb = (SeekBar) findViewById(R.id.mic_gain_sb);
        mSpeakerVolumeTv = (TextView) findViewById(R.id.speaker_volume_tv);
        mSpeakerVolumeSb = (SeekBar) findViewById(R.id.speaker_volume_sb);
        mMonitorVolumeTv = (TextView) findViewById(R.id.monitor_volume_tv);
        mMonitorValueSb = (SeekBar) findViewById(R.id.monitor_value_sb);
        mAgcTb = (ToggleButton) findViewById(R.id.agc_tb);
        mAnsReduceBt = (Button) findViewById(R.id.ans_reduce_bt);
        mAnsValueTv = (TextView) findViewById(R.id.ans_value_tv);
        mAnsPlusBt = (Button) findViewById(R.id.ans_plus_bt);
        mRevbTypeSubBt = (Button) findViewById(R.id.revb_type_sub_bt);
        mRevbValueTv = (TextView) findViewById(R.id.revb_value_tv);
        mRevbTypeAddBt = (Button) findViewById(R.id.revb_type_add_bt);
        mRevbRatioTv = (TextView) findViewById(R.id.revb_ratio_tv);
        mRevbRatioSb = (SeekBar) findViewById(R.id.revb_ratio_sb);
        mMusicMixerTv = (TextView) findViewById(R.id.music_mixer_tv);
        mMusicMixerSb = (SeekBar) findViewById(R.id.music_mixer_sb);
        mTextDevocal = (TextView) findViewById(R.id.text_devocal);
        mBtnClose = (Button) findViewById(R.id.btn_close);
        mBtnOpen = (Button) findViewById(R.id.btn_open);
        mBtnDevocalLeft = (Button) findViewById(R.id.btn_devocal_left);
        mBtnDevocalRight = (Button) findViewById(R.id.btn_devocal_right);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);


        mStartUpdate.setOnClickListener(this);

        mStartRecoderBt.setOnClickListener(this);
        mStopRecoderBt.setOnClickListener(this);

        mMicGainSb.setOnSeekBarChangeListener(this);
        mSpeakerVolumeSb.setOnSeekBarChangeListener(this);
        mMonitorValueSb.setOnSeekBarChangeListener(this);
        mRevbRatioSb.setOnSeekBarChangeListener(this);
        mMusicMixerSb.setOnSeekBarChangeListener(this);

        mBtnClose.setOnClickListener(this);
        mBtnOpen.setOnClickListener(this);
        mBtnDevocalLeft.setOnClickListener(this);
        mBtnDevocalRight.setOnClickListener(this);
        findViewById(R.id.start_stereo_to_mono_bt).setOnClickListener(this);
        findViewById(R.id.start_resample_bt).setOnClickListener(this);
        findViewById(R.id.start_combination_bt).setOnClickListener(this);


        mAgcTb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Swiss.getSwiss().setAgcEnable(b);
                mMicGainSb.setEnabled(!b);
            }
        });


        AnsClickListener ansClickListener = new AnsClickListener();
        mAnsPlusBt.setOnClickListener(ansClickListener);
        mAnsReduceBt.setOnClickListener(ansClickListener);
        ReverberationClickListener reverberationClickListener = new ReverberationClickListener();
        mRevbTypeSubBt.setOnClickListener(reverberationClickListener);
        mRevbTypeAddBt.setOnClickListener(reverberationClickListener);


        Swiss.getSwiss().setOnStateChangeListener(new BluetoothHelper.OnStateChangeListener() {
            @Override
            public void onStateChange(final State state) {
                if (state == State.STATE_CONNECTED) {
                    mToolbar.post(new Runnable() {
                        @Override
                        public void run() {
                            mDeviceStateTv.setText(state.toString());
                            mMicGianTv.setText(ResourcesUtils.getString(R.string.mic_gian) + Swiss.getSwiss().getMicVolume());
                            mMicGainSb.setProgress(Swiss.getSwiss().getMicVolume());
                            mSpeakerVolumeTv.setText(ResourcesUtils.getString(R.string.speaker_volume) + Swiss.getSwiss().getSpeakerVolume());
                            mSpeakerVolumeSb.setProgress(Swiss.getSwiss().getSpeakerVolume());
                            mMonitorVolumeTv.setText(ResourcesUtils.getString(R.string.monitor_volume) + Swiss.getSwiss().getMonitorVolume());
                            mMonitorValueSb.setProgress(Swiss.getSwiss().getMonitorVolume());
                            mRevbRatioTv.setText(ResourcesUtils.getString(R.string.revb_ratio_value) + Swiss.getSwiss().getReverberationRatio());
                            mRevbRatioSb.setProgress(Swiss.getSwiss().getReverberationRatio());
                            mMusicMixerTv.setText(ResourcesUtils.getString(R.string.music_mixer_value) + Swiss.getSwiss().getMusicMixer());
                            mMusicMixerSb.setProgress(Swiss.getSwiss().getMusicMixer());
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


    private class ReverberationClickListener implements View.OnClickListener {
        private int reverberationType = 0;

        @Override
        public void onClick(View view) {
            int tag = view.getId();
            switch (tag) {
                case R.id.revb_type_sub_bt:
                    if (reverberationType > 0) {
                        reverberationType--;
                        mRevbValueTv.setText(reverberationType + "");
                        Swiss.getSwiss().setReverberationType(reverberationType);
                    }

                    break;
                case R.id.revb_type_add_bt:
                    if (reverberationType < 13) {
                        reverberationType++;
                        mRevbValueTv.setText(reverberationType + "");
                        Swiss.getSwiss().setReverberationType(reverberationType);
                    }
                    break;

            }
        }
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
            case R.id.start_combination_bt:
                Intent intent3 = new Intent(MainActivity.this, CombinationActivity.class);
                startActivity(intent3);
                break;
            case R.id.start_update:
                File tempFile = FileUtils.createTempFile(getApplicationContext(),
                        ".file");
                copyFilesFassets(this,
                        "OTA_8670_0123_OTATEST.dfu",
                        tempFile.getAbsolutePath());

                FirmwareUpdater.getInstance().upFirmware(tempFile, new FirmwareUpdateListener() {
                    @Override
                    public void onUpdate(int progress) {

                    }

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });
                break;
            case R.id.start_recode_bt:
                Swiss.getSwiss().startRecord(new AudioDispatcher.OnAudioReceiver() {
                    @Override
                    public void onReceiveData(byte[] data) {
                        Log.d("swiss", "get pcm length is " + data.length);
                    }
                });
                break;
            case R.id.stop_recode_bt:
                Swiss.getSwiss().stopRecord();
                break;
            case R.id.btn_close:
                Swiss.getSwiss().setMusicDevocal(0);
                break;
            case R.id.btn_open:
                Swiss.getSwiss().setMusicDevocal(1);
                break;
            case R.id.btn_devocal_left:
                Swiss.getSwiss().setMusicDevocal(2);
                break;
            case R.id.btn_devocal_right:
                Swiss.getSwiss().setMusicDevocal(3);
                break;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (seekBar == mMicGainSb) {
            mMicGianTv.setText(ResourcesUtils.getString(R.string.mic_gian) + i + "");
            Swiss.getSwiss().setMicVolume(i);
        } else if (seekBar == mSpeakerVolumeSb) {
            mSpeakerVolumeTv.setText(ResourcesUtils.getString(R.string.speaker_volume) + i + "");
            Swiss.getSwiss().setSpeakerVolume(i);
        } else if (seekBar == mMonitorValueSb) {
            mMonitorVolumeTv.setText(ResourcesUtils.getString(R.string.monitor_volume) + i + "");
            Swiss.getSwiss().setMonitorVolume(i);
        } else if (seekBar == mRevbRatioSb) {
            mRevbRatioTv.setText(ResourcesUtils.getString(R.string.revb_ratio_value) + i + "");
            Swiss.getSwiss().setReverberationRatio(i);
        } else if (seekBar == mMusicMixerSb) {
            mMusicMixerTv.setText(ResourcesUtils.getString(R.string.music_mixer_value) + i + "");
            Swiss.getSwiss().setMusicMixer(i);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


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


