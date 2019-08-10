package com.herewhite.sdk;

import android.content.Context;

import com.google.gson.JsonSyntaxException;
import com.herewhite.sdk.domain.EventEntry;
import com.herewhite.sdk.domain.EventListener;
import com.herewhite.sdk.domain.PlayerObserverMode;
import com.herewhite.sdk.domain.PlayerPhase;
import com.herewhite.sdk.domain.PlayerState;
import com.herewhite.sdk.domain.PlayerTimeInfo;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.SDKError;

import java.util.concurrent.ConcurrentHashMap;

import wendu.dsbridge.OnReturnValue;

/**
 * 回放房间操作类
 */
public class Player extends Displayer {

    private final ConcurrentHashMap<String, EventListener> eventListenerConcurrentHashMap = new ConcurrentHashMap<>();
    private final SyncDisplayerState<PlayerState> syncPlayerState;

    private final long timeDuration;
    private final int framesCount;
    private final long beginTimestamp;

    private long scheduleTime = 0;

    private PlayerPhase playerPhase = PlayerPhase.waitingFirstFrame;

    /**
     * Instantiates a new Player.
     *
     * @param room            回放房间 uuid
     * @param bridge          the bridge
     * @param context         the context
     * @param whiteSdk        the white sdk
     * @param playerTimeInfo  the player time info
     * @param syncPlayerState the sync player state
     */
    public Player(String room, WhiteBroadView bridge, Context context, WhiteSdk whiteSdk, PlayerTimeInfo playerTimeInfo, SyncDisplayerState<PlayerState> syncPlayerState) {
        super(room, bridge, context, whiteSdk);
        this.syncPlayerState = syncPlayerState;
        this.timeDuration = playerTimeInfo.getTimeDuration();
        this.framesCount = playerTimeInfo.getFramesCount();
        this.beginTimestamp = playerTimeInfo.getBeginTimestamp();
    }

    SyncDisplayerState<PlayerState> getSyncPlayerState() {
        return syncPlayerState;
    }

    void setPlayerPhase(PlayerPhase playerPhase) {
        this.playerPhase = playerPhase;
    }

    void setScheduleTime(long scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public void play() {
        bridge.callHandler("player.play", new Object[]{});
    }

    public void pause() {
        bridge.callHandler("player.pause", new Object[]{});
    }

    /**
     * stop 后，player 资源会被释放。需要重新创建WhitePlayer实例，才可以重新播放
     */
    public void stop() {
        bridge.callHandler("player.stop", new Object[]{});
        this.sdk.releasePlayer(uuid);
    }

    /**
     * 跳转至特定时间，开始时间为 0，单位毫秒。
     *
     * @param seekTime 跳转时间
     */
    public void seekToScheduleTime(long seekTime) {
        bridge.callHandler("player.seekToScheduleTime", new Object[]{seekTime});
    }

    //region Event API
    void fireMagixEvent(EventEntry eventEntry) {
        EventListener eventListener = eventListenerConcurrentHashMap.get(eventEntry.getEventName());
        if (eventListener != null) {
            try {
                eventListener.onEvent(eventEntry);
            } catch (Throwable e) {
                Logger.error("An exception occurred while sending the event", e);
            }
        }
    }

    /**
     * 移除特定自定义事件监听
     *
     * @param eventName 自定义事件名称
     */
    public void removeMagixEventListener(String eventName) {
        this.eventListenerConcurrentHashMap.remove(eventName);
        bridge.callHandler("player.removeMagixEventListener", new Object[]{eventName});
    }

    /**
     * 添加自定义事件监听
     *
     * @param eventName 自定义事件名称
     * @param eventListener 自定义事件回调实现 @see EventListener
     */
    public void addMagixEventListener(String eventName, EventListener eventListener) {
        this.eventListenerConcurrentHashMap.put(eventName, eventListener);
        bridge.callHandler("player.addMagixEventListener", new Object[]{eventName});
    }
    //endregion

    /**
     * 设置查看模式
     *
     * @param mode 查看模式
     * @see PlayerObserverMode
     */
    public void setObserverMode(PlayerObserverMode mode) {
        bridge.callHandler("player.setObserverMode", new Object[]{mode.name()});
    }

    //region Get API

    /**
     * 同步 获取回放房间播放状态
     * 目前：初始状态为 WhitePlayerPhaseWaitingFirstFrame
     *
     * @return 回放房间状态
     */
    public PlayerPhase getPlayerPhase() {
        return this.playerPhase;
    }

    /**
     * 异步 获取回放房间播放状态
     *
     * @deprecated 使用 {@link #getPlayerPhase()} 同步 API，进行获取。
     * @param promise the promise
     */
    @Deprecated
    public void getPhase(final Promise<PlayerPhase> promise) {
        bridge.callHandler("player.getBroadcastState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    promise.then(PlayerPhase.valueOf(String.valueOf(o)));
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getPhase", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getPhase promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    /**
     * 同步 获取回放房间中所有状态
     *
     * 当 phase 状态为 WhitePlayerPhaseWaitingFirstFrame
     * 回调得到的数据是空的
     *
     * @return 回放房间状态 {@link PlayerState}，与 {@link com.herewhite.sdk.domain.RoomState 有接近的结构}
     */
    public PlayerState getPlayerState() {
        return this.syncPlayerState.getDisplayerState();
    }

    /**
     * 异步 获取回放房间中状态
     *
     * @deprecated 请使用 {@link #getPlayerState()} 同步 API，进行获取。
     * @param promise 完成回调
     */
    @Deprecated
    public void getPlayerState(final Promise<PlayerState> promise) {
        bridge.callHandler("player.state.playerState", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                try {
                    PlayerState playerState = gson.fromJson(String.valueOf(o), PlayerState.class);
                    promise.then(playerState);
                } catch (AssertionError a) {
                    throw a;
                } catch (JsonSyntaxException e) {
                    Logger.error("An JsonSyntaxException occurred while parse json from getPlayerState", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                } catch (Throwable e) {
                    Logger.error("An exception occurred in getPlayerState promise then method", e);
                    promise.catchEx(new SDKError(e.getMessage()));
                }
            }
        });
    }

    /**
     * 同步 获取播放文件信息
     * 包括：当前时间，总时长，开始 UTC 时间戳。单位：毫秒
     * @return {@link PlayerTimeInfo}
     */
    public PlayerTimeInfo getPlayerTimeInfo() {
        return new PlayerTimeInfo(this.scheduleTime, this.timeDuration, this.framesCount, this.beginTimestamp);
    }

    /**
     * 异步 获取播放文件信息
     *
     * @deprecated 请使用 {@link #getPlayerTimeInfo()} 同步 API，进行获取。
     * @param promise 完成回调
     */
    @Deprecated
    public void getPlayerTimeInfo(final Promise<PlayerTimeInfo> promise) {
        bridge.callHandler("player.state.timeInfo", new Object[]{}, new OnReturnValue<Object>() {
            @Override
            public void onValue(Object o) {
                promise.then(getPlayerTimeInfo());
            }
        });
    }
    //endregion
}