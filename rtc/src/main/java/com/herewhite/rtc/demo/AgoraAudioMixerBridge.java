package com.herewhite.rtc.demo;

import static io.agora.rtc2.Constants.AUDIO_MIXING_STATE_FAILED;

import android.util.Log;

import com.herewhite.sdk.AudioMixerBridge;

import io.agora.rtc2.RtcEngine;

/**
 * 用户需要自己实现 rtc 混音逻辑
 */
public class AgoraAudioMixerBridge implements AudioMixerBridge {
    public static final String TAG = AgoraAudioMixerBridge.class.getSimpleName();

    private RtcEngine rtcEngine;
    private ResultCallback resultCallback;

    public AgoraAudioMixerBridge(RtcEngine rtcEngine, ResultCallback resultCallback) {
        this.rtcEngine = rtcEngine;
        this.resultCallback = resultCallback;
    }

    @Override
    public void startAudioMixing(String filepath, boolean loopback, boolean replace, int cycle) {
        int code = rtcEngine.startAudioMixing(filepath, loopback, cycle, 0);
        Log.d(TAG, "rtcMix startAudioMixing " + filepath + " " + code);
        if (code != 0) {
            onMediaStateChanged(AUDIO_MIXING_STATE_FAILED, code);
        }
    }

    @Override
    public void stopAudioMixing() {
        int code = rtcEngine.stopAudioMixing();
        Log.d(TAG, "rtcMix stopAudioMixing " + code);
        if (code != 0) {
            onMediaStateChanged(0, code);
        }
    }

    @Override
    public void setAudioMixingPosition(int position) {
        int code = rtcEngine.setAudioMixingPosition(position);
        Log.d(TAG, "rtcMix setAudioMixingPosition " + position + " " + code);
        if (code != 0) {
            onMediaStateChanged(0, code);
        }
    }

    @Override
    public void pauseAudioMixing() {
        int code = rtcEngine.pauseAudioMixing();
        Log.d(TAG, "rtcMix pauseAudioMixing " + code);
        if (code != 0) {
            onMediaStateChanged(0, code);
        }
    }

    @Override
    public void resumeAudioMixing() {
        int code = rtcEngine.resumeAudioMixing();
        Log.d(TAG, "rtcMix resumeAudioMixing " + code);
        if (code != 0) {
            onMediaStateChanged(0, code);
        }
    }

    private void onMediaStateChanged(int state, int code) {
        Log.d(TAG, "rtcMix onMediaStateChanged " + code);
        resultCallback.onMediaStateChanged(state, code);
    }

    public interface ResultCallback {
        void onMediaStateChanged(int state, int code);
    }
}
