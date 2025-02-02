package com.xrs.bluetooth_device;

/**
 * @ClassName MicrophoneTest
 * @Author kotlin
 * @Email 949390151@qq.com
 * @Date 2024/10/11 19:45
 * ^_^^_^^_^^_^^_^^_^^_^
 */
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

public class MicrophoneTest {
    private static final String TAG = "MicrophoneTest";
    private static final int SAMPLE_RATE_IN_HZ = 44100; // 采样率
    private static final int CHANNEL_CONFIG = 2; // 单声道
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT; // 音频格式
    private static final int BUFFER_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;

    public void start() {
        // 初始化 AudioRecord
        audioRecord = new AudioRecord(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE_IN_HZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);

        // 初始化 AudioTrack
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE_IN_HZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE, AudioTrack.MODE_STREAM);

        // 启动录音和播放
        audioRecord.startRecording();
        audioTrack.play();
        Log.d(TAG, "Microphone is recording and playing...");

        // 在一个新线程中读取和写入数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[BUFFER_SIZE];
                while (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    int result = audioRecord.read(buffer, 0, buffer.length);
                    if (result > 0) {
                        audioTrack.write(buffer, 0, result);
                    }
                }
                audioTrack.stop();
                audioTrack.release();
                audioRecord.stop();
                audioRecord.release();
            }
        }).start();
    }

    public void stop() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
        Log.d(TAG, "Microphone has stopped recording and playing.");
    }
}