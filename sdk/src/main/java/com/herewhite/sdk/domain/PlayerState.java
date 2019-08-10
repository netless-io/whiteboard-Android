package com.herewhite.sdk.domain;

public class PlayerState {
    private GlobalState globalState;
    /**
     * 房间用户状态
     */
    private RoomMember[] roomMembers;

    private SceneState sceneState;
    /**
     * 用户观察状态
     */
    private PlayerObserverMode observerMode;
    public GlobalState getGlobalState() {
        return globalState;
    }
    public RoomMember[] getRoomMembers() {
        return roomMembers;
    }

}