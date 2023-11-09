package com.herewhite.sdk;

import android.content.Context;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.herewhite.sdk.domain.FontFace;
import com.herewhite.sdk.domain.PlayerConfiguration;
import com.herewhite.sdk.domain.PlayerTimeInfo;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.UrlInterrupter;
import com.herewhite.sdk.domain.WindowRegisterAppParams;
import com.herewhite.sdk.internal.PlayerJsInterfaceImpl;
import com.herewhite.sdk.internal.PostMessageCallback;
import com.herewhite.sdk.internal.RoomJsInterfaceImpl;
import com.herewhite.sdk.internal.RtcJsInterfaceImpl;
import com.herewhite.sdk.internal.SdkJsInterfaceImpl;
import com.herewhite.sdk.internal.StoreDelegate;
import com.herewhite.sdk.internal.StoreJsInterfaceImpl;
import com.herewhite.sdk.window.SlideListener;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

import wendu.dsbridge.OnReturnValue;

/**
 * `WhiteSdk` 类。
 */
public class WhiteSdk {
    private final static String SDK_VERSION = "2.16.73";

    private final static Gson gson = new Gson();
    private static AudioMixerBridge sAudioMixerBridge;
    private static AudioEffectBridge sAudioEffectBridge;
    private final JsBridgeInterface bridge;
    private final RoomJsInterfaceImpl roomJsInterface;
    private final PlayerJsInterfaceImpl playerJsInterface;
    private final SdkJsInterfaceImpl sdkJsInterface;
    private final StoreJsInterfaceImpl storeJsInterface;
    private final int densityDpi;
    private final boolean onlyCallbackRemoteStateModify;
    private RtcJsInterfaceImpl rtcJsInterface;
    @Nullable
    private AudioMixerImplement audioMixerImplement;
    private AudioEffectImplement audioEffectImplement;
    private SlideListener slideListener;

    /**
     * 初始化白板 SDK 实例。
     * <p>
     * 请确保在调用其他 API 前先调用 `WhiteSdk` 创建并初始化白板 SDK 实例。
     *
     * @param bridge                白板界面，详见 {@link WhiteboardView WhiteboardView}。
     * @param context               安卓活动 (Android Activity) 的上下文。
     * @param whiteSdkConfiguration SDK 实例的配置，详见 {@link WhiteSdkConfiguration WhiteSdkConfiguration}。
     *
     */
    public WhiteSdk(JsBridgeInterface bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration) {
        this(bridge, context, whiteSdkConfiguration, (CommonCallback) null);
    }

    /**
     * 初始化白板 SDK 实例。
     * <p>
     * 请确保在调用其他 API 前先调用 `WhiteSdk` 创建并初始化白板 SDK 实例。
     *
     * @param bridge                白板界面，详见 {@link WhiteboardView WhiteboardView}。
     * @param context               安卓活动 (Android Activity) 的上下文。
     * @param whiteSdkConfiguration SDK 实例的配置，详见 {@link WhiteSdkConfiguration WhiteSdkConfiguration}。
     * @param commonCallback        通用事件回调，详见 {@link CommonCallback CommonCallback}。
     */
    public WhiteSdk(JsBridgeInterface bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration, @Nullable CommonCallback commonCallback) {
        this(bridge, context, whiteSdkConfiguration, commonCallback, null);
    }

    /**
     * 初始化白板 SDK 实例。
     * <p>
     * 请确保在调用其他 API 前先调用 `WhiteSdk` 创建并初始化白板 SDK 实例。
     *
     * @param bridge                白板界面，详见 {@link com.herewhite.sdk.WhiteboardView WhiteboardView}。
     * @param context               安卓活动 (Android Activity) 的上下文。
     * @param whiteSdkConfiguration SDK 实例的配置，详见 {@link com.herewhite.sdk.WhiteSdkConfiguration WhiteSdkConfiguration}。
     * @param urlInterrupter        图片 URL 拦截设置，详见 {@link com.herewhite.sdk.domain.UrlInterrupter UrlInterrupter}。@deprecated 该参数已废弃。请改用 `CommonCallbacks` 接口中的 {@link CommonCallbacks#urlInterrupter(String) urlInterrupter} 方法。
     */
    public WhiteSdk(JsBridgeInterface bridge, Context context, WhiteSdkConfiguration whiteSdkConfiguration, UrlInterrupter urlInterrupter) {
        this(bridge, context, whiteSdkConfiguration);
        sdkJsInterface.setUrlInterrupter(urlInterrupter);
    }

    WhiteSdk(JsBridgeInterface bridge,
                    Context context,
                    WhiteSdkConfiguration whiteSdkConfiguration,
                    CommonCallback commonCallback,
                    AudioMixerBridge audioMixerBridge) {
        this(bridge, context, whiteSdkConfiguration, commonCallback, audioMixerBridge, null);
    }

    /**
     * 初始化白板 SDK 实例。
     * <p>
     * 请确保在调用其他 API 前先调用 `WhiteSdk` 创建并初始化白板 SDK 实例。
     *
     * @param bridge                白板界面，详见 {@link com.herewhite.sdk.WhiteboardView WhiteboardView}。
     * @param context               安卓活动 (Android Activity) 的上下文。
     * @param whiteSdkConfiguration SDK 实例的配置，详见 {@link com.herewhite.sdk.WhiteSdkConfiguration WhiteSdkConfiguration}。
     * @param commonCallback        通用事件回调，详见 {@link com.herewhite.sdk.CommonCallback CommonCallback}。
     * @param audioMixerBridge      混音设置，详见 {@link com.herewhite.sdk.AudioMixerBridge AudioMixerBridge}。当你同时使用 Agora RTC SDK 和互动白板 SDK, 且白板中展示的动态 PPT 中包含音频文件时，你可以调用 `AudioMixerBridge` 接口，将动态 PPT 中的所有音频交给 Agora RTC SDK 进行混音播放。
     */
    public WhiteSdk(JsBridgeInterface bridge,
                    Context context,
                    WhiteSdkConfiguration whiteSdkConfiguration,
                    @Nullable CommonCallback commonCallback,
                    @Nullable AudioMixerBridge audioMixerBridge,
                    @Nullable AudioEffectBridge audioEffectBridge) {
        this.bridge = bridge;
        densityDpi = Utils.getDensityDpi(context);
        roomJsInterface = new RoomJsInterfaceImpl();
        playerJsInterface = new PlayerJsInterfaceImpl();
        sdkJsInterface = new SdkJsInterfaceImpl(commonCallback);
        storeJsInterface = new StoreJsInterfaceImpl();
        onlyCallbackRemoteStateModify = whiteSdkConfiguration.isOnlyCallbackRemoteStateModify();

        if (audioMixerBridge == null) {
            audioMixerBridge = sAudioMixerBridge;
        }

        if (audioEffectBridge == null) {
            audioEffectBridge = sAudioEffectBridge;
        }

        if (audioMixerBridge != null || audioEffectBridge != null) {
            audioMixerImplement = new AudioMixerImplement(bridge);
            audioEffectImplement = new AudioEffectImplement(bridge);

            rtcJsInterface = new RtcJsInterfaceImpl(audioMixerBridge, audioEffectBridge);
            bridge.addJavascriptObject(rtcJsInterface, "rtc");
            whiteSdkConfiguration.setEnableRtcIntercept(true);
        }

        bridge.addJavascriptObject(this.sdkJsInterface, "sdk");
        bridge.addJavascriptObject(this.roomJsInterface, "room");
        bridge.addJavascriptObject(this.playerJsInterface, "player");
        bridge.addJavascriptObject(this.storeJsInterface, "store");

        // JavaScript 必须将所有 state 变化回调提供给 native。
        // 该属性的实现在 native 代码中体现。
        WhiteSdkConfiguration copyConfig = Utils.deepCopy(whiteSdkConfiguration, WhiteSdkConfiguration.class);
        copyConfig.setOnlyCallbackRemoteStateModify(false);

        bridge.callHandler("sdk.newWhiteSdk", new Object[]{copyConfig});
    }

    /**
     * 查询 SDK 版本号。
     *
     * @return 当前的 SDK 版本号，格式为字符串，如 `"2.12.28"`。
     */
    public static String Version() {
        return SDK_VERSION;
    }

    public static void setAudioMixerBridge(AudioMixerBridge audioMixerBridge) {
        sAudioMixerBridge = audioMixerBridge;
    }

    public static void setAudioEffectBridge(AudioEffectBridge audioEffectBridge) {
        sAudioEffectBridge = audioEffectBridge;
    }

    /**
     * 设置通用事件回调。
     * <p>
     * SDK 通过 `CommonCallbacks` 类向 app 报告 SDK 运行时的各项事件。
     *
     * @param commonCallback 通用回调事件，详见 {@link CommonCallback CommonCallback}
     */
    public void setCommonCallbacks(CommonCallback commonCallback) {
        sdkJsInterface.setCommonCallbacks(commonCallback);
    }

    /**
     * 获取 {@link AudioMixerImplement} 实例。
     *
     * @return {@link AudioMixerImplement} 实例。
     */
    public AudioMixerImplement getAudioMixerImplement() {
        return audioMixerImplement;
    }

    /**
     * 获取 {@link AudioEffectImplement} 实例。
     * 此实例用于接收音效相关的回调。
     *
     * @since 2.16.73
     * @return
     */
    public AudioEffectImplement getAudioEffectImplement() {
        return audioEffectImplement;
    }

    /**
     * 加入互动白板实时房间。
     *
     * @param roomParams  互动白板实时房间的参数配置，详见 {@link RoomParams RoomParams}。
     * @param roomPromise `Promise<Room>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `joinRoom` 的调用结果：
     * - 如果方法调用成功，将返回房间对象，详见 {@link Room}。
     * - 如果方法调用失败，将返回错误信息。
     */
    public void joinRoom(final RoomParams roomParams, final Promise<Room> roomPromise) {
        this.joinRoom(roomParams, null, roomPromise);
    }

    /**
     * 加入互动白板实时房间。
     *
     * @param roomParams   互动白板实时房间的参数配置，详见 {@link RoomParams RoomParams}。
     * @param roomListener 房间事件回调，详见 {@link RoomListener RoomListener}。在重连时，如果不传 `roomListener` 参数，则会使用上一次设置的 `roomListener`。如果要释放 `roomListener`，可以调用 {@link #releaseRoom(String) releaseRoom}。
     * @param roomPromise  `Promise<Room>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `joinRoom` 的调用结果：
     * - 如果方法调用成功，则返回房间对象，详见 {@link Room}。
     * - 如果方法调用失败，则返回错误信息。
     */
    public void joinRoom(final RoomParams roomParams, final RoomListener roomListener, final Promise<Room> roomPromise) {
        Room room = new Room(roomParams.getUuid(), bridge, densityDpi, onlyCallbackRemoteStateModify);
        room.setRoomListener(roomListener);
        roomJsInterface.setRoom(room.getRoomDelegate());
        storeJsInterface.setStore(new StoreDelegate() {
            @Override
            public void fireSyncedStoreUpdate(String value) {
                room.getSyncedStore().fireStorageStateUpdate(value);
            }
        });

        try {
            bridge.callHandler("sdk.joinRoom", new Object[]{roomParams}, (OnReturnValue<String>) roomString -> {
                JsonObject jsonObject = gson.fromJson(roomString, JsonObject.class);
                SDKError promiseError = SDKError.promiseError(jsonObject);
                if (promiseError != null) {
                    roomPromise.catchEx(promiseError);
                } else {
                    JsonObject jsonState = jsonObject.getAsJsonObject("state");
                    Long observerId = jsonObject.get("observerId").getAsLong();
                    Boolean isWritable = jsonObject.get("isWritable").getAsBoolean();

                    room.setSyncRoomState(jsonState.toString(), !roomParams.isDisableInitialStateCallback());
                    room.setObserverId(observerId);
                    room.setWritable(isWritable);

                    roomPromise.then(room);
                }
            });
        } catch (AssertionError a) {
            throw a;
        } catch (Exception e) {
            roomPromise.catchEx(new SDKError(e.getMessage()));
        }
    }

    /**
     * 创建互动白板回放房间。
     *
     * @param playerConfiguration 白板回放的参数配置，详见 {@link com.herewhite.sdk.domain.PlayerConfiguration PlayerConfiguration}。
     * @param playerPromise       `Promise<Player>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `createPlayer` 的调用结果：
     * - 如果方法调用成功，将返回新创建的回放房间对象，详见 {@link com.herewhite.sdk.Player Player}。
     * - 如果方法调用失败，将返回错误信息。
     */
    public void createPlayer(final PlayerConfiguration playerConfiguration, final Promise<Player> playerPromise) {
        createPlayer(playerConfiguration, null, playerPromise);
    }

    /**
     * 创建互动白板回放房间。
     *
     * @param playerConfiguration 白板回放的参数配置，详见 {@link com.herewhite.sdk.domain.PlayerConfiguration PlayerConfiguration}。
     * @param listener            白板回放事件的回调，详见 {@link com.herewhite.sdk.PlayerEventListener PlayerEventListener}。
     * @param playerPromise       `Promise<Player>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `createPlayer` 的调用结果：
     *  - 如果方法调用成功，将返回新创建的回放房间对象，详见 {@link com.herewhite.sdk.Player Player}。
     *  - 如果方法调用失败，将返回错误信息。
     */
    public void createPlayer(final PlayerConfiguration playerConfiguration, final PlayerListener listener, final Promise<Player> playerPromise) {
        Player player = new Player(playerConfiguration.getRoom(), bridge, densityDpi);
        if (listener != null) {
            player.addPlayerListener(listener);
        }
        playerJsInterface.setPlayer(player.getDelegate());
        storeJsInterface.setStore(new StoreDelegate() {
            @Override
            public void fireSyncedStoreUpdate(String value) {
                player.getSyncedStore().fireStorageStateUpdate(value);
            }
        });

        try {
            bridge.callHandler("sdk.replayRoom", new Object[]{
                    playerConfiguration
            }, (OnReturnValue<String>) playString -> {
                JsonObject jsonObject = gson.fromJson(playString, JsonObject.class);
                SDKError promiseError = SDKError.promiseError(jsonObject);
                if (promiseError != null) {
                    playerPromise.catchEx(promiseError);
                } else {
                    JsonObject timeInfo = jsonObject.getAsJsonObject("timeInfo");
                    PlayerTimeInfo playerTimeInfo = gson.fromJson(timeInfo.toString(), PlayerTimeInfo.class);

                    player.setPlayerTimeInfo(playerTimeInfo);
                    playerPromise.then(player);
                }
            });
        } catch (AssertionError a) {
            throw a;
        } catch (Exception e) {
            playerPromise.catchEx(new SDKError(e.getMessage()));
        }
    }

    /**
     * 查看房间是否有回放数据。
     *
     * @since 2.11.0
     *
     * @param playerConfiguration 白板回放的参数配置，详见 {@link com.herewhite.sdk.domain.PlayerConfiguration PlayerConfiguration}。
     * @param playablePromise     `Promise<Boolean>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `isPlayable` 的调用结果：
     *                            - 如果方法调用成功，则返回 `true`。
     *                            - 如果方法调用失败，则返回 `false`。
     */
    public void isPlayable(final PlayerConfiguration playerConfiguration, final Promise<Boolean> playablePromise) {
        bridge.callHandler("sdk.isPlayable", new Object[]{playerConfiguration}, new OnReturnValue<Boolean>() {
            @Override
            public void onValue(Boolean retValue) {
                playablePromise.then(retValue);
            }
        });
    }

    /**
     * 声明在本地白板中可用的字体。
     *
     * @since 2.11.2
     *
     * <p>
     * 调用该方法声明的字体可用于显示 PPT 中的文字和白板工具输入的文字。
     * <p>
     * 该方法和 {@link #loadFontFaces loadFontFaces} 都可以声明在本地白板中可用的字体，区别是 `setupFontFaces` 没有回调，因为无法判断字体声明是否正确；`loadFontFaces` 会触发回调，报告每一种的预加载结果。
     *
     * @note - 该方法只对本地白板生效，不影响远端白板的字体显示。
     * - 通过该方法声明的字体，只有当被使用时，才会触发下载。
     * - 不同的字体在不同设备上的渲染可能不同，例如，在某些设备上，要等字体加载完成后，才会渲染文字；而在另外一些设备上，会先使用默认的字体渲染文字，等指定的字体加载完毕后，再整体刷新。
     * - 每次调用该方法都会覆盖原来的字体声明。
     * - 请勿同时调用该方法和 `loadFontFaces` 方法。否则，无法预期行为。
     *
     * @param fontFaces `FontFace` 实例，详见 {@link com.herewhite.sdk.domain.FontFace FontFace}。
     */
    public void setupFontFaces(FontFace[] fontFaces) {
        bridge.callHandler("sdk.updateNativeFontFaceCSS", new Object[]{fontFaces});
    }

    /**
     * 声明并预加载在本地白板中可用的字体。
     *
     * @since 2.11.2
     *
     * <p>
     * 调用该方法预加载的字体可以用于显示 PPT 中的文字和白板工具输入的文字。
     * <p>
     * 该方法和 {@link #setupFontFaces(FontFace[] fontFaces) setupFontFaces} 都可以声明在本地白板中可用的字体，区别是 `setupFontFaces` 没有回调，因为无法判断字体声明是否正确；`loadFontFaces` 会触发回调，报告每一种的预加载结果。
     *
     * @note
     * - 该方法只对本地白板生效，不影响远端白板的字体显示。
     * - 使用该方法预加载的字体，只有当该字体被使用时，才会触发下载。
     * - 不同的字体在不同设备上的渲染可能不同，例如，在某些设备上，要等字体加载完成后，才会渲染文字；而在另外一些设备上，会先使用默认的字体渲染文字，等指定的字体加载完毕后，再整体刷新。
     * - 通过该方法预加载的字体无法删除，每次调用都会在原来的基础上新增。
     * - 请勿同时调用该方法和 `setupFontFaces` 方法。否则，无法预期行为。
     *
     * @param fontFaces   指定的字体，详见 {@link com.herewhite.sdk.domain.FontFace FontFace}。
     * @param loadPromise `Promise<JSONObject>` 接口实例，详见 {@link com.herewhite.sdk.domain.Promise Promise}。你可以通过该接口获取 `loadFontFaces` 的调用结果：
     *                    - 如果方法调用成功，则返回 `FontFace` 对象。
     *                    - 如果方法调用失败，则返回错误信息。
     */
    public void loadFontFaces(FontFace[] fontFaces, final Promise<JSONObject> loadPromise) {
        bridge.callHandler("sdk.asyncInsertFontFaces", new Object[]{fontFaces}, new OnReturnValue<JSONObject>() {
            @Override
            public void onValue(JSONObject retValue) {
                loadPromise.then(retValue);
            }
        });
    }

    public void registerApp(WindowRegisterAppParams params, final Promise<Boolean> registerPromise) {
        bridge.callHandler("sdk.registerApp", new Object[]{params}, (OnReturnValue<String>) retValue -> {
            if (registerPromise == null) {
                return;
            }

            if (retValue == null) {
                registerPromise.then(true);
            } else {
                registerPromise.catchEx(SDKError.promiseError(retValue));
            }
        });
    }

    /**
     * 设置文字白板工具在本地白板中使用的字体。
     *
     * @since 2.11.2
     *
     * @note
     * - 该方法只对本地白板生效，不影响远端白板的字体显示。
     * - 该方法只能设置文字白板工具使用的字体，不能用于 PPT 中的文字显示。
     *
     * @param names 字体名称。如果用户系统中不存在该字体，则文字白板工具无法使用该字体。请确保你已经调用 `setupFontFaces` 或 `loadFontFaces` 将指定字体加载到本地白板中。
     *
     */
    public void updateTextFont(String[] names) {
        bridge.callHandler("sdk.updateNativeTextareaFont", new Object[]{names});
    }

    /**
     * 释放互动白板实时房间实例并删除 `RoomListener` 回调。
     *
     * @since 2.4.12
     */
    public void releaseRoom() {
        roomJsInterface.setRoom(null);
        storeJsInterface.setStore(null);
    }

    /**
     * 释放互动白板实时房间实例并删除 `RoomListener` 回调。
     *
     * @deprecated 该方法已废弃。请改用 {@link #releaseRoom() releaseRoom}。
     *
     * @param uuid 房间 UUID。该参数无实际意义，因为一个 `WhiteSdk` 实例只能对应一个实时房间，不需要使用 UUID 指定房间。
     *
     */
    @Deprecated
    public void releaseRoom(String uuid) {
        releaseRoom();
    }

    /**
     * 释放回放房间实例并删除 `PlayerListener` 回调。
     *
     * @since 2.4.12
     */
    public void releasePlayer() {
        playerJsInterface.setPlayer(null);
        storeJsInterface.setStore(null);
    }

    /**
     * 释放回放房间实例并删除 `PlayerEventListener` 回调。
     *
     * @deprecated 该方法已废弃。请改用 {@link #releasePlayer() releasePlayer}。
     *
     * @param uuid 回放房间的 UUID。该参数无实际意义，由于一个 `WhiteSdk` 实例只能对应一个回放房间，不需要使用 UUID 指定回放房间。
     *
     */
    @Deprecated
    public void releasePlayer(String uuid) {
        releasePlayer();
    }

    private static final AtomicInteger sSessionId = new AtomicInteger(1);

    /**
     * 请求 Slide 的日志
     *
     * @param logFile 用于存储 slide 日志的日志文件
     * @param promise 请求调用回调结果
     */
    public void requestSlideLog(File logFile, final Promise<Boolean> promise) {
        try {
            RequestSlideLogHandler handler = new RequestSlideLogHandler(logFile, promise);
            handler.request();
        } catch (Exception e) {
            promise.catchEx(new SDKError(e.getMessage()));
        }
    }

    class RequestSlideLogHandler implements PostMessageCallback {
        private static final String SLIDE_REPORT_LOG_KEY = "@slide/_report_log_";

        private final Promise<Boolean> promise;
        private final PrintWriter writer;
        private final int sessionId;

        public RequestSlideLogHandler(File logFile, final Promise<Boolean> promise) throws Exception {
            this.promise = promise;
            writer = new PrintWriter(new FileWriter(logFile, true));
            sessionId = sSessionId.getAndIncrement();
        }

        @Override
        public void onMessage(JSONObject jsonObject) {
            try {
                String type = jsonObject.optString("type");
                int id = jsonObject.optInt("sessionId");
                if (SLIDE_REPORT_LOG_KEY.equals(type) && id == sessionId) {
                    String log = jsonObject.getString("log");
                    writer.write(log);

                    int total = jsonObject.getInt("total");
                    int index = jsonObject.getInt("index");
                    if (index == total) {
                        success();
                    }
                }
            } catch (Exception e) {
                failure(e);
            }
        }

        private void success() {
            writer.close();
            sdkJsInterface.setPostMessageCallback(null);
            promise.then(true);
        }

        private void failure(Exception e) {
            writer.close();
            sdkJsInterface.setPostMessageCallback(null);
            promise.catchEx(new SDKError(e.getMessage()));
        }

        public void request() {
            sdkJsInterface.setPostMessageCallback(this);
            WhiteboardView whiteboardView = (WhiteboardView) bridge;
            whiteboardView.evaluateJavascript("window.postMessage({'type': \"@slide/_request_log_\",'sessionId': " + sessionId + "});");
        }
    }

    /**
     * 更新当前 Slide 音量
     * @param volume
     */
    public void updateSlideVolume(float volume) {
        bridge.evaluateJavascript("window.postMessage({'type': \"@slide/_update_volume_\",'volume': " + volume + "});");
    }

    /**
     * 获取当前 Slide 音量
     *
     * @param promise 成功返回音量信息，失败返回错误信息
     */
    public void getSlideVolume(Promise<Double> promise) {
        sdkJsInterface.setPostMessageCallback(jsonObject -> {
            try {
                String type = jsonObject.optString("type");
                if ("@slide/_report_volume_".equals(type)) {
                    sdkJsInterface.setPostMessageCallback(null);
                    promise.then(jsonObject.getDouble("volume"));
                }
            } catch (Exception e) {
                sdkJsInterface.setPostMessageCallback(null);
                promise.catchEx(SDKError.parseError(jsonObject));
            }
        });
        bridge.evaluateJavascript("window.postMessage({'type': \"@slide/_get_volume_\"});");
    }

    /**
     * 设置 SlideApp 回调。
     * <p>
     * SlideApp 通过 `SlideListener` 类向 app 报告内部运行时的各项事件。
     *
     * @param slideListener 通用回调事件，详见 {@link SlideListener SlideListener}
     */
    public void setSlideListener(SlideListener slideListener) {
        sdkJsInterface.setSlideListener(slideListener);
    }
}
