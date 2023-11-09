package com.herewhite.sdk;

/**
 * `AudioMixerImplement` 类，用于实现 `AudioMixerBridge` 接口。
 */
public class AudioEffectImplement {
    private final JsBridgeInterface bridge;

    AudioEffectImplement(JsBridgeInterface bridge) {
        this.bridge = bridge;
    }

    public void setEffectFinished(int soundId) {
        bridge.callHandler("rtc.setEffectFinished", new Object[]{soundId});
    }
}
