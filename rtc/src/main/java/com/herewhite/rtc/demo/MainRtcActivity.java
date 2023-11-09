package com.herewhite.rtc.demo;

import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_ALL_LOOPS_COMPLETED;
import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_CAN_NOT_OPEN;
import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_INTERRUPTED_EOF;
import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_OK;
import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_ONE_LOOP_COMPLETED;
import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_STOPPED_BY_USER;
import static io.agora.rtc2.Constants.AUDIO_MIXING_REASON_TOO_FREQUENT_CALL;
import static io.agora.rtc2.Constants.AUDIO_MIXING_STATE_FAILED;
import static io.agora.rtc2.Constants.AUDIO_MIXING_STATE_PAUSED;
import static io.agora.rtc2.Constants.AUDIO_MIXING_STATE_PLAYING;
import static io.agora.rtc2.Constants.AUDIO_MIXING_STATE_STOPPED;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.herewhite.rtc.demo.databinding.ActivityMainRtcBinding;
import com.herewhite.sdk.AbstractRoomCallbacks;
import com.herewhite.sdk.CommonCallback;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkBuilder;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.SDKError;

import org.json.JSONObject;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class MainRtcActivity extends AppCompatActivity {
    private String RTC_APP_ID = "1e91eb01d5c147baafcbac407f9cb5d2";
    private String RTC_CHANNEL_ID = "demoChannel1";
    private String RTC_CHANNEL_TOKEN = "007eJxTYFhRsT5iX6KMcuDT+wq3pXwmJDWwc0+QPq1r/OZGTuhvpysKDIaploapSQaGKabJhibmSYmJaclJickmBuZplslJpilG2cE+qQ2BjAyNbxoYGKEQxOdhSEnNzXfOSMzLS80xZGAAADy1Img=";

    private static final int UID_LOCAL = 0x12356;
    private static final int UID_REMOTE = 0;

    private static final String APP_ID = "122123/123132";
    private static final String ROOM_UUID = "d4d4d0a073d111ee90cba7667d54b7f2";
    private static final String ROOM_TOKEN = "NETLESSROOM_YWs9eTBJOWsxeC1IVVo4VGh0NyZub25jZT0xNjk4MzA1NTU1NzIzMDAmcm9sZT0wJnNpZz1lNTQxNGM0OWQ0MDRlYWY2NmM2NTVlNmI4ODk3MWE5OTc4YjM3MDU3YTAyN2E5ZmI4NGIwMTNjOGY4MTk3MWQ1JnV1aWQ9ZDRkNGQwYTA3M2QxMTFlZTkwY2JhNzY2N2Q1NGI3ZjI";
    private static final String DEFAULT_UID = "1233124";

    private static final int PERMISSION_REQ_ID = 22;

    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
    };

    private ActivityMainRtcBinding binding;

    private SurfaceView localVideoView;
    private SurfaceView remoteVideoView;

    private RtcEngine rtcEngine;

    private WhiteboardView whiteboardView;
    private WhiteSdk whiteSdk;
    private Room room;

    private AgoraAudioMixerBridge audioMixerBridge;
    private AgoraAudioEffectBridge audioEffectBridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainRtcBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WebView.setWebContentsDebuggingEnabled(true);

        whiteboardView = binding.whiteboardView;
        binding.exit.setOnClickListener(v -> finish());
        binding.btnCall.setOnClickListener(v -> {
            onCallClicked(v);
        });

        binding.playEffect1.setOnClickListener(v -> {
            int code = rtcEngine.playEffect(
                    1,   // 设置音效 ID
                    "https://convertcdn.netless.group/test/dynamicConvert/d507bd99a85b4f14861edbce85ef30e0/jsonOutput/401f2fd866b025ba71f959eef4930819.mp3",   // 设置音效文件路径
                    1,  // 设置音效循环播放的次数。-1 表示无限循环
                    1,   // 设置音效的音调。1 表示原始音调
                    0.0, // 设置音效的空间位置。0.0 表示音效出现在正前方
                    100, // 设置音效音量。100 表示原始音量
                    false,// 设置是否将音效发布至远端
                    0    // 设置音效文件的播放位置。0 表示从音效文件的第 0 ms 开始播放
            );
            int code2 = rtcEngine.playEffect(2, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%201.mp3", 0, 1, 0.0, 100, false, 150000);
            int code3 = rtcEngine.playEffect(3, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%202.mp3", 0, 1, 0.0, 100, false, 160000);

            Log.d("AudioEffect", "code1: " + code + " code2: " + code2 + " code3: " + code3);
        });

        binding.playEffect2.setOnClickListener(v -> {
            rtcEngine.pauseEffect(1);
            int code = rtcEngine.playEffect(
                    2,   // 设置音效 ID
                    "https://convertcdn.netless.group/test/dynamicConvert/d507bd99a85b4f14861edbce85ef30e0/jsonOutput/5b456f5a519cde1c8b548537af499f98.mp3",   // 设置音效文件路径
                    1,  // 设置音效循环播放的次数。-1 表示无限循环
                    1,   // 设置音效的音调。1 表示原始音调
                    0.0, // 设置音效的空间位置。0.0 表示音效出现在正前方
                    100, // 设置音效音量。100 表示原始音量
                    false,// 设置是否将音效发布至远端
                    0    // 设置音效文件的播放位置。0 表示从音效文件的第 0 ms 开始播放
            );
            Log.i("AudioEffect", "playEffect code: " + code);
        });

        binding.playEffect3.setOnClickListener(v -> {
            int code1 = rtcEngine.pauseEffect(2);
            int code2 = rtcEngine.playEffect(
                    1,   // 设置音效 ID
                    "https://convertcdn.netless.group/test/dynamicConvert/d507bd99a85b4f14861edbce85ef30e0/jsonOutput/401f2fd866b025ba71f959eef4930819.mp3",   // 设置音效文件路径
                    1,  // 设置音效循环播放的次数。-1 表示无限循环
                    1,   // 设置音效的音调。1 表示原始音调
                    0.0, // 设置音效的空间位置。0.0 表示音效出现在正前方
                    100, // 设置音效音量。100 表示原始音量
                    false,// 设置是否将音效发布至远端
                    0    // 设置音效文件的播放位置。0 表示从音效文件的第 0 ms 开始播放
            );

            Log.i("AudioEffect", "playEffect code: " + code1 + " code2: " + code2);
        });

        binding.getEffectCurrentPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = audioEffectBridge.getEffectCurrentPosition(1);
                Log.d("AudioEffect", "getEffectCurrentPosition :" + position);
            }
        });

        binding.pauseEffect.setOnClickListener(v -> {
            int code = audioEffectBridge.pauseAllEffects();
            Log.d("AudioEffect", "pauseAllEffects code: " + code);
        });

        binding.resumeEffect.setOnClickListener(v -> {
            int code = audioEffectBridge.resumeAllEffects();
            Log.d("AudioEffect", "resumeAllEffects code: " + code);
        });

        binding.startAudioMixing.setOnClickListener(v -> {
            audioMixerBridge.startAudioMixing("https://white-pan.oss-cn-shanghai.aliyuncs.com/101/oceans.mp4", false, false, 1);
        });

        // 如果用户需要用到 rtc 混音功能来解决回声和声音抑制问题，那么必须要在 whiteSDK 之前初始化 rtcEngine
        checkAndInitRtcEngine();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkAndInitRtcEngine();
    }

    private void checkAndInitRtcEngine() {
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initializeEngine();
            setupVideoConfig();
            onCallClicked(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveChannel();
        RtcEngine.destroy();
        whiteboardView.removeAllViews();
        whiteboardView.destroy();
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // 注册 onJoinChannelSuccess 回调。
        // 本地用户成功加入频道时，会触发该回调。
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora", "Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora", "First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            Log.i("agora", "User offline, uid: " + (uid & 0xFFFFFFFFL));
        }

        @Override
        // 混音状态变化时的回调
        public void onAudioMixingStateChanged(int state, int reason) {
            Log.d(AgoraAudioMixerBridge.TAG, "rtcMix[RTC] onAudioMixingStateChanged " + readableState(state) + ":" + readableReason(reason));
            if (whiteSdk != null) {
                whiteSdk.getAudioMixerImplement().setMediaState(state, reason);
            }
        }

        @Override
        public void onAudioEffectFinished(int soundId) {
            Log.d("AudioEffect", "rtcMix[RTC] onAudioEffectFinished " + soundId);
            if (whiteSdk != null) {
                whiteSdk.getAudioEffectImplement().setEffectFinished(soundId);
            }
        }
    };

    private void initializeEngine() {
        try {
            rtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.rtc_app_id), mRtcEventHandler);
        } catch (Exception e) {
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        rtcEngine.enableVideo();
        rtcEngine.startPreview();
        rtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void preloadEffect() {
        // int code0 = rtcEngine.preloadEffect(1, "https://white-pan.oss-cn-shanghai.aliyuncs.com/101/oceans.mp4");
        // int code1 = rtcEngine.preloadEffect(2, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%201.mp3");
        // int code2 = rtcEngine.preloadEffect(3, "https://canvas-conversion-demo-dev.oss-cn-hangzhou.aliyuncs.com/assets/Jay%20demo%202.mp3");
        // Log.e("AudioEffect", "preloadEffect code0: " + code0 + " code1: " + code1 + " code2: " + code2);
        // rtcEngine.setEffectsVolume(50.0);
    }

    private void joinRoom(String uuid, String token) {
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(APP_ID, true);
        configuration.setUserCursor(true);
        configuration.setUseMultiViews(true);

        audioMixerBridge = new AgoraAudioMixerBridge(rtcEngine, (state, code) -> {
            if (whiteSdk.getAudioMixerImplement() != null) {
                whiteSdk.getAudioMixerImplement().setMediaState(state, code);
            }
        });
        audioEffectBridge = new AgoraAudioEffectBridge(rtcEngine);

        whiteSdk = new WhiteSdkBuilder(whiteboardView, configuration)
                .setAudioMixerBridge(audioMixerBridge)
                .setAudioEffectBridge(audioEffectBridge)
                .setCommonCallback(new CommonCallback() {
                    @Override
                    public void onLogger(JSONObject object) {
                        logAction(object.toString());
                    }
                })
                .build();

        RoomParams roomParams = new RoomParams(uuid, token, DEFAULT_UID);
        roomParams.setWritable(true);

        whiteSdk.joinRoom(roomParams, new AbstractRoomCallbacks() {

            @Override
            public void onPhaseChanged(RoomPhase phase) {
                showToast(phase.name());
            }
        }, new Promise<Room>() {
            @Override
            public void then(Room room) {
                MemberState memberState = new MemberState();
                memberState.setCurrentApplianceName(Appliance.CLICKER);
                room.setMemberState(memberState);
            }

            @Override
            public void catchEx(SDKError sdkError) {

            }
        });
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    private void setupRemoteVideo(int uid) {
        remoteVideoView = RtcEngine.CreateRendererView(getBaseContext());
        binding.remoteVideoViewContainer.addView(remoteVideoView);
        rtcEngine.setupRemoteVideo(new VideoCanvas(remoteVideoView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    private void setupLocalVideo() {
        localVideoView = RtcEngine.CreateRendererView(getBaseContext());
        localVideoView.setZOrderMediaOverlay(true);
        binding.localVideoViewContainer.addView(localVideoView);
        rtcEngine.setupLocalVideo(new VideoCanvas(localVideoView, VideoCanvas.RENDER_MODE_HIDDEN, UID_LOCAL));
    }

    public void onCallClicked(View view) {
        boolean targetCall = !binding.btnCall.isSelected();
        if (targetCall) {
            startCall();
        } else {
            endCall();
        }
        binding.btnCall.setSelected(targetCall);
    }

    private void startCall() {
        setupLocalVideo();
        joinChannel();
        joinRoom(ROOM_UUID, ROOM_TOKEN);
    }

    private void endCall() {
        removeLocalVideo();
        removeRemoteVideo();
        leaveChannel();
    }

    private void removeLocalVideo() {
        if (localVideoView != null) {
            binding.localVideoViewContainer.removeView(localVideoView);
            localVideoView = null;
        }
    }

    private void removeRemoteVideo() {
        if (remoteVideoView != null) {
            binding.remoteVideoViewContainer.removeView(remoteVideoView);
            remoteVideoView = null;
        }
    }

    /**
     * 加入 rtc 频道
     */
    private void joinChannel() {
        // 这里没有使用 token 加入频道，推荐使用 token 保证应用安全，详细设置参考 rtc 文档
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        rtcEngine.joinChannel(RTC_CHANNEL_TOKEN, RTC_CHANNEL_ID, UID_LOCAL, options);
    }

    private void leaveChannel() {
        rtcEngine.leaveChannel();
    }

    private static final String ROOM_INFO = "RoomInfo";
    private static final String ROOM_ACTION = "RoomAction";

    void logRoomInfo(String str) {
        Log.i(ROOM_INFO, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    void logAction(String str) {
        Log.i(ROOM_ACTION, Thread.currentThread().getStackTrace()[3].getMethodName() + " " + str);
    }

    void logAction() {
        Log.i(ROOM_ACTION, Thread.currentThread().getStackTrace()[3].getMethodName());
    }

    void showToast(Object o) {
        Toast.makeText(this, o.toString(), Toast.LENGTH_SHORT).show();
    }

    public String readableState(int state) {
        switch (state) {
            case AUDIO_MIXING_STATE_PLAYING:
                return "PLAYING";
            case AUDIO_MIXING_STATE_PAUSED:
                return "PAUSED";
            case AUDIO_MIXING_STATE_STOPPED:
                return "STOPPED";
            case AUDIO_MIXING_STATE_FAILED:
                return "FAILED";
            default:
                return state + "";
        }
    }

    public String readableReason(int reason) {
        switch (reason) {
            case AUDIO_MIXING_REASON_OK:
                return "OK";
            case AUDIO_MIXING_REASON_CAN_NOT_OPEN:
                return "CAN_NOT_OPEN";
            case AUDIO_MIXING_REASON_TOO_FREQUENT_CALL:
                return "TOO_FREQUENT_CALL";
            case AUDIO_MIXING_REASON_INTERRUPTED_EOF:
                return "INTERRUPTED_EOF";
            case AUDIO_MIXING_REASON_ONE_LOOP_COMPLETED:
                return "ONE_LOOP_COMPLETED";
            case AUDIO_MIXING_REASON_ALL_LOOPS_COMPLETED:
                return "ALL_LOOPS_COMPLETED";
            case AUDIO_MIXING_REASON_STOPPED_BY_USER:
                return "STOPPED_BY_USER";
            default:
                return reason + "";
        }
    }
}