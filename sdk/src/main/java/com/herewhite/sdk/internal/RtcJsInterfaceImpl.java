package com.herewhite.sdk.internal;

import android.webkit.JavascriptInterface;

import com.herewhite.sdk.AudioEffectBridge;
import com.herewhite.sdk.AudioMixerBridge;

import org.json.JSONException;
import org.json.JSONObject;

import wendu.dsbridge.CompletionHandler;

public class RtcJsInterfaceImpl {
    private AudioMixerBridge mixerBridge;
    private AudioEffectBridge audioEffectBridge;

    public RtcJsInterfaceImpl(AudioMixerBridge mixerBridge, AudioEffectBridge audioEffectBridge) {
        this.mixerBridge = mixerBridge;
        this.audioEffectBridge = audioEffectBridge;
    }

    @JavascriptInterface
    public void startAudioMixing(Object args) {
        if (this.mixerBridge != null && args instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) args;
            try {
                String filePath = jsonObject.getString("filePath");
                boolean loopback = jsonObject.getBoolean("loopback");
                boolean replace = jsonObject.getBoolean("replace");
                int cycle = jsonObject.getInt("cycle");
                this.mixerBridge.startAudioMixing(filePath, loopback, replace, cycle);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @JavascriptInterface
    public void stopAudioMixing(Object args) {
        if (this.mixerBridge != null) {
            this.mixerBridge.stopAudioMixing();
        }
    }

    @JavascriptInterface
    public void pauseAudioMixing(Object args) {
        if (this.mixerBridge != null) {
            this.mixerBridge.pauseAudioMixing();
        }
    }

    @JavascriptInterface
    public void resumeAudioMixing(Object args) {
        if (this.mixerBridge != null) {
            this.mixerBridge.resumeAudioMixing();
        }
    }

    @JavascriptInterface
    public void setAudioMixingPosition(Object args) {
        if (this.mixerBridge != null) {
            int pos = Integer.valueOf((Integer) args);
            this.mixerBridge.setAudioMixingPosition(pos);
        }
    }

    @JavascriptInterface
    public void getEffectsVolume(Object args, CompletionHandler<Double> handler) {
        if (audioEffectBridge != null) {
            handler.complete(audioEffectBridge.getEffectsVolume());
        }
    }

    @JavascriptInterface
    public void setEffectsVolume(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null) {
            int code = audioEffectBridge.setEffectsVolume(Double.parseDouble(args.toString()));
            handler.complete(code);
        }
    }

    @JavascriptInterface
    public void setVolumeOfEffect(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null && args instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) args;
            try {
                int soundId = jsonObject.getInt("soundId");
                double volume = jsonObject.getDouble("volume");
                int code = audioEffectBridge.setVolumeOfEffect(soundId, volume);
                handler.complete(code);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Play an audio effect with the specified parameters.
     *
     * @param args A {@link JSONObject} containing the parameters for the audio effect, including:
     *             - "soundId" (int): The unique identifier for the audio effect.
     *             - "filePath" (String): The file path of the audio effect.
     *             - "loopCount" (int, optional): The number of times to loop the audio (default is 0).
     *             - "pitch" (double, optional): The pitch of the audio effect (default is 1.0).
     *             - "pan" (double, optional): The stereo pan position of the audio effect (default is 0.0).
     *             - "gain" (double, optional): The gain/volume of the audio effect (default is 100).
     *             - "publish" (boolean, optional): Whether to publish the audio effect (default is false).
     *             - "startPos" (int, optional): The starting position of playback (default is 0).
     * @param handler A {@link CompletionHandler<Integer>} to receive the result code of the operation.
     * @see CompletionHandler
     */
    @JavascriptInterface
    public void playEffect(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null && args instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) args;
            try {
                int soundId = jsonObject.getInt("soundId");
                String filePath = jsonObject.getString("filePath");
                int loopCount = jsonObject.optInt("loopCount", 0);
                double pitch = jsonObject.optDouble("pitch", 1.0);
                double pan = jsonObject.optDouble("pan", 0.0);
                double gain = jsonObject.optDouble("gain", 100);
                boolean publish = jsonObject.optBoolean("publish", false);
                int startPos = jsonObject.optInt("startPos", 0);
                int code = audioEffectBridge.playEffect(soundId, filePath, loopCount, pitch, pan, gain, publish, startPos);
                handler.complete(code);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @JavascriptInterface
    public void stopEffect(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null) {
            int code = audioEffectBridge.stopEffect(Integer.parseInt(args.toString()));
            handler.complete(code);
        }
    }

    @JavascriptInterface
    public void stopAllEffects(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null) {
            int code = audioEffectBridge.stopAllEffects();
            handler.complete(code);
        }
    }

    @JavascriptInterface
    public void preloadEffect(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null && args instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) args;
            try {
                int soundId = jsonObject.getInt("soundId");
                String filePath = jsonObject.getString("filePath");
                int startPos = jsonObject.optInt("startPos", 0);
                int code = audioEffectBridge.preloadEffect(soundId, filePath, startPos);
                handler.complete(code);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @JavascriptInterface
    public void unloadEffect(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null) {
            int code = audioEffectBridge.unloadEffect(Integer.parseInt(args.toString()));
            handler.complete(code);
        }
    }

    @JavascriptInterface
    public void pauseEffect(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null) {
            int code = audioEffectBridge.pauseEffect(Integer.parseInt(args.toString()));
            handler.complete(code);
        }
    }

    @JavascriptInterface
    public void pauseAllEffects(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null) {
            int code = audioEffectBridge.pauseAllEffects();
            handler.complete(code);
        }
    }

    @JavascriptInterface
    public void resumeEffect(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null) {
            int code = audioEffectBridge.resumeEffect(Integer.parseInt(args.toString()));
            handler.complete(code);
        }
    }

    @JavascriptInterface
    public void resumeAllEffects(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null) {
            int code = audioEffectBridge.resumeAllEffects();
            handler.complete(code);
        }
    }

    @JavascriptInterface
    public void getEffectDuration(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null) {
            int code = audioEffectBridge.getEffectDuration(args.toString());
            handler.complete(code);
        }
    }

    @JavascriptInterface
    public void getEffectCurrentPosition(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null) {
            int code = audioEffectBridge.getEffectCurrentPosition(Integer.parseInt(args.toString()));
            handler.complete(code);
        }
    }

    @JavascriptInterface
    public void setEffectPosition(Object args, CompletionHandler<Integer> handler) {
        if (audioEffectBridge != null && args instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) args;
            try {
                int soundId = jsonObject.getInt("soundId");
                int pos = jsonObject.getInt("pos");
                int code = audioEffectBridge.setEffectPosition(soundId, pos);
                handler.complete(code);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
