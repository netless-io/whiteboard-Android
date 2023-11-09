package com.herewhite.sdk;

import android.content.Context;

public class WhiteSdkBuilder {
    private JsBridgeInterface bridge;
    private Context context;
    private WhiteSdkConfiguration whiteSdkConfiguration;
    private CommonCallback commonCallback;
    private AudioMixerBridge audioMixerBridge;
    private AudioEffectBridge audioEffectBridge;

    public WhiteSdkBuilder(WhiteboardView whiteboardView, WhiteSdkConfiguration whiteSdkConfiguration) {
        this.bridge = whiteboardView;
        this.context = whiteboardView.getContext();
        this.whiteSdkConfiguration = whiteSdkConfiguration;
    }

    public WhiteSdkBuilder setCommonCallback(CommonCallback commonCallback) {
        this.commonCallback = commonCallback;
        return this;
    }

    public WhiteSdkBuilder setAudioMixerBridge(AudioMixerBridge audioMixerBridge) {
        this.audioMixerBridge = audioMixerBridge;
        return this;
    }

    public WhiteSdkBuilder setAudioEffectBridge(AudioEffectBridge audioEffectBridge) {
        this.audioEffectBridge = audioEffectBridge;
        return this;
    }

    public WhiteSdk build() {
        return new WhiteSdk(bridge, context, whiteSdkConfiguration, commonCallback, audioMixerBridge, audioEffectBridge);
    }
}