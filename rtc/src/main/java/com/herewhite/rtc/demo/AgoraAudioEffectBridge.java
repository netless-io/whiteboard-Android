package com.herewhite.rtc.demo;

import android.util.Log;

import com.herewhite.sdk.AudioEffectBridge;

import io.agora.rtc2.RtcEngine;

public class AgoraAudioEffectBridge implements AudioEffectBridge {
    private final String TAG = "AgoraAudioEffectBridge";

    private final RtcEngine rtcEngine;

    public AgoraAudioEffectBridge(RtcEngine rtcEngine) {
        this.rtcEngine = rtcEngine;
    }

    @Override
    public double getEffectsVolume() {
        return rtcEngine.getEffectsVolume();
    }

    @Override
    public int setEffectsVolume(double volume) {
        Log.i(TAG, "setEffectsVolume: " + volume);
        return rtcEngine.setEffectsVolume(volume);
    }

    @Override
    public int setVolumeOfEffect(int soundId, double volume) {
        Log.i(TAG, "setVolumeOfEffect: " + soundId + " " + volume);
        return rtcEngine.setVolumeOfEffect(soundId, volume);
    }

    @Override
    public int playEffect(int soundId, String filePath, int loopCount, double pitch, double pan, double gain, boolean publish, int startPos) {
        Log.i(TAG, "playEffect: " + soundId + " " + filePath + " " + loopCount + " " + pitch + " " + pan + " " + gain + " " + publish + " " + startPos);
        return rtcEngine.playEffect(soundId, filePath, loopCount, pitch, pan, gain, publish, startPos);
    }

    @Override
    public int stopEffect(int soundId) {
        Log.i(TAG, "stopEffect: " + soundId);
        return rtcEngine.stopEffect(soundId);
    }

    @Override
    public int stopAllEffects() {
        Log.i(TAG, "stopAllEffects: ");
        return rtcEngine.stopAllEffects();
    }

    @Override
    public int preloadEffect(int soundId, String filePath, int startPos) {
        Log.i(TAG, "preloadEffect: " + soundId + " " + filePath + " " + startPos);
        return rtcEngine.preloadEffect(soundId, filePath, startPos);
    }

    @Override
    public int unloadEffect(int soundId) {
        Log.i(TAG, "unloadEffect: " + soundId);
        return rtcEngine.unloadEffect(soundId);
    }

    @Override
    public int pauseEffect(int soundId) {
        Log.i(TAG, "pauseEffect: " + soundId);
        return rtcEngine.pauseEffect(soundId);
    }

    @Override
    public int pauseAllEffects() {
        Log.i(TAG, "pauseAllEffects: ");
        return rtcEngine.pauseAllEffects();
    }

    @Override
    public int resumeEffect(int soundId) {
        Log.i(TAG, "resumeEffect: " + soundId);
        return rtcEngine.resumeEffect(soundId);
    }

    @Override
    public int resumeAllEffects() {
        Log.i(TAG, "resumeAllEffects: ");
        return rtcEngine.resumeAllEffects();
    }

    @Override
    public int getEffectDuration(String filePath) {
        Log.i(TAG, "getEffectDuration: " + filePath);
        return rtcEngine.getEffectDuration(filePath);
    }

    @Override
    public int setEffectPosition(int soundId, int pos) {
        Log.i(TAG, "setEffectPosition: " + soundId + " " + pos);
        return rtcEngine.setEffectPosition(soundId, pos);
    }

    @Override
    public int getEffectCurrentPosition(int soundId) {
        Log.i(TAG, "getEffectCurrentPosition: " + soundId);
        return rtcEngine.getEffectCurrentPosition(soundId);
    }
}
