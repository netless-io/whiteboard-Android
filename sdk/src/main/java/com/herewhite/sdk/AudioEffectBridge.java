package com.herewhite.sdk;

public interface AudioEffectBridge {
    double getEffectsVolume();

    int setEffectsVolume(double volume);

    int setVolumeOfEffect(int soundId, double volume);

    int playEffect(int soundId, String filePath, int loopCount, double pitch, double pan, double gain, boolean publish, int startPos);

    int stopEffect(int soundId);

    int stopAllEffects();

    int preloadEffect(int soundId, String filePath, int startPos);

    int unloadEffect(int soundId);

    int pauseEffect(int soundId);

    int pauseAllEffects();

    int resumeEffect(int soundId);

    int resumeAllEffects();

    int getEffectDuration(String filePath);

    int setEffectPosition(int soundId, int pos);

    int getEffectCurrentPosition(int soundId);
}
